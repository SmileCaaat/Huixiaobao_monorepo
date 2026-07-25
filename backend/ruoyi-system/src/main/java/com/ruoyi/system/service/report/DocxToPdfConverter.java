package com.ruoyi.system.service.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * DOCX 转 PDF：通过 LibreOffice 无界面转换。
 * <p>
 * 用户可见错误统一为 UTF-8 中文，不把命令行原始输出直接回传前端。
 */
@Component
public class DocxToPdfConverter {

    private static final Logger log = LoggerFactory.getLogger(DocxToPdfConverter.class);

    private static final long CONVERT_TIMEOUT_SECONDS = 180L;

    private static final String NOT_FOUND_MSG =
            "未找到LibreOffice，无法生成PDF预览，请检查ruoyi.report.libreOfficePath配置。";

    @Value("${ruoyi.report.libreOfficePath:}")
    private String configuredLibreOfficePath;

    /**
     * 将 DOCX 转为 PDF。成功后 pdfPath 必须存在、大小大于 0，且文件头为 %PDF。
     */
    public void convert(Path docxPath, Path pdfPath) {
        if (docxPath == null || !Files.isRegularFile(docxPath)) {
            throw new ServiceException("DOCX源文件不存在，无法转换为PDF");
        }
        try {
            if (Files.size(docxPath) <= 0) {
                throw new ServiceException("DOCX源文件为空，无法转换为PDF");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("读取DOCX文件失败");
        }

        try {
            if (pdfPath.getParent() != null) {
                Files.createDirectories(pdfPath.getParent());
            }
        } catch (Exception e) {
            throw new ServiceException("创建PDF输出目录失败");
        }

        Path soffice = resolveSofficeExecutable();
        if (soffice == null) {
            throw new ServiceException(NOT_FOUND_MSG);
        }

        Path workDir = null;
        Path userInstallDir = null;
        try {
            workDir = Files.createTempDirectory("fire-report-lo-work-");
            userInstallDir = Files.createTempDirectory("fire-report-lo-user-");
            convertWithLibreOffice(soffice, docxPath, pdfPath, workDir, userInstallDir);
            assertValidPdf(pdfPath);
            log.info("LibreOffice 转换 PDF 成功: {}", pdfPath.toAbsolutePath());
        } catch (ServiceException e) {
            deleteQuietly(pdfPath);
            throw e;
        } catch (Exception e) {
            deleteQuietly(pdfPath);
            log.error("DOCX 转 PDF 异常", e);
            throw new ServiceException("DOCX转PDF失败，请查看服务器日志");
        } finally {
            deleteDirectoryQuietly(workDir);
            deleteDirectoryQuietly(userInstallDir);
        }
    }

    /**
     * 当前是否能解析到可用的 LibreOffice 可执行文件（供诊断/测试）。
     */
    public boolean isLibreOfficeAvailable() {
        return resolveSofficeExecutable() != null;
    }

    public Path resolveSofficeExecutablePublic() {
        return resolveSofficeExecutable();
    }

    private void convertWithLibreOffice(Path soffice, Path docxPath, Path pdfPath,
            Path workDir, Path userInstallDir) throws Exception {
        Path inputCopy = workDir.resolve("source.docx");
        Files.copy(docxPath, inputCopy);

        List<String> command = new ArrayList<String>();
        command.add(soffice.toAbsolutePath().toString());
        command.add("-env:UserInstallation=" + userInstallDir.toAbsolutePath().toUri().toString());
        command.add("--headless");
        command.add("--nologo");
        command.add("--nolockcheck");
        command.add("--nodefault");
        command.add("--nofirststartwizard");
        command.add("--convert-to");
        command.add("pdf");
        command.add("--outdir");
        command.add(workDir.toAbsolutePath().toString());
        command.add(inputCopy.toAbsolutePath().toString());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.directory(workDir.toFile());

        log.info("LibreOffice 命令: {}", command);
        Process process = pb.start();

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        Thread gobbler = new Thread(new Runnable() {
            @Override
            public void run() {
                try (InputStream in = process.getInputStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) >= 0) {
                        outputBuffer.write(buf, 0, n);
                    }
                } catch (Exception e) {
                    log.warn("读取 LibreOffice 输出失败: {}", e.getMessage());
                }
            }
        }, "libreoffice-output-gobbler");
        gobbler.setDaemon(true);
        gobbler.start();

        boolean finished = process.waitFor(CONVERT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            gobbler.join(2000L);
            String output = toLogText(outputBuffer);
            log.error("LibreOffice 转换超时, 输出=\n{}", output);
            throw new ServiceException("LibreOffice转换超时，请稍后重试");
        }
        gobbler.join(5000L);

        String output = toLogText(outputBuffer);
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("LibreOffice 退出码={}, 输出=\n{}", exitCode, output);
            throw new ServiceException("LibreOffice转换失败（退出码=" + exitCode + "），请查看服务器日志");
        }

        Path produced = workDir.resolve("source.pdf");
        if (!Files.isRegularFile(produced) || Files.size(produced) <= 0) {
            log.error("LibreOffice 未产出有效 PDF, 输出=\n{}", output);
            throw new ServiceException("LibreOffice未生成有效PDF文件，请查看服务器日志");
        }
        Files.copy(produced, pdfPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private String toLogText(ByteArrayOutputStream buffer) {
        if (buffer == null) {
            return "";
        }
        // 仅用于日志；不回传前端，避免 Windows 本地编码显示为乱码
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private Path resolveSofficeExecutable() {
        List<Path> candidates = new ArrayList<Path>();

        addConfiguredCandidates(candidates, configuredLibreOfficePath);
        addConfiguredCandidates(candidates, System.getenv("LIBREOFFICE_PATH"));

        candidates.add(Paths.get("C:\\Program Files\\LibreOffice\\program\\soffice.exe"));
        candidates.add(Paths.get("C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe"));
        candidates.add(Paths.get("/usr/bin/soffice"));
        candidates.add(Paths.get("/usr/bin/libreoffice"));
        candidates.add(Paths.get("/usr/lib/libreoffice/program/soffice"));
        candidates.add(Paths.get("/opt/libreoffice/program/soffice"));
        candidates.add(Paths.get("/Applications/LibreOffice.app/Contents/MacOS/soffice"));

        for (Path candidate : candidates) {
            if (isUsableExecutable(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }

        Path fromPath = findOnPath("soffice.exe");
        if (fromPath == null) {
            fromPath = findOnPath("soffice");
        }
        if (fromPath == null) {
            fromPath = findOnPath("libreoffice");
        }
        return fromPath;
    }

    private void addConfiguredCandidates(List<Path> candidates, String raw) {
        if (StringUtils.isEmpty(raw)) {
            return;
        }
        String trimmed = raw.trim();
        // 去掉误加的首尾引号（ProcessBuilder 不需要额外引号）
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (StringUtils.isEmpty(trimmed)) {
            return;
        }

        Path path = Paths.get(trimmed);
        if (Files.isDirectory(path)) {
            candidates.add(path.resolve("program").resolve("soffice.exe"));
            candidates.add(path.resolve("program").resolve("soffice"));
            candidates.add(path.resolve("soffice.exe"));
            candidates.add(path.resolve("soffice"));
            candidates.add(path.resolve("libreoffice"));
        } else {
            candidates.add(path);
            // 若配置到了 LibreOffice 根目录下的错误文件名，再尝试拼接 program
            Path parent = path.getParent();
            if (parent != null) {
                candidates.add(parent.resolve("soffice.exe"));
                candidates.add(parent.resolve("soffice"));
            }
        }
    }

    private boolean isUsableExecutable(Path path) {
        if (path == null) {
            return false;
        }
        try {
            if (!Files.isRegularFile(path)) {
                return false;
            }
            // Windows 上 Files.isExecutable 对 .exe 可能不准，文件存在即接受
            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".exe") || name.endsWith(".cmd") || name.endsWith(".bat")
                    || name.equals("soffice") || name.equals("libreoffice")) {
                return true;
            }
            return Files.isExecutable(path);
        } catch (Exception e) {
            return false;
        }
    }

    private Path findOnPath(String executable) {
        String pathEnv = System.getenv("PATH");
        if (StringUtils.isEmpty(pathEnv)) {
            return null;
        }
        String[] parts = pathEnv.split(java.io.File.pathSeparator);
        for (String part : parts) {
            if (StringUtils.isEmpty(part)) {
                continue;
            }
            Path candidate = Paths.get(part.trim(), executable);
            if (isUsableExecutable(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    public static void assertValidPdf(Path pdfPath) throws Exception {
        if (pdfPath == null || !Files.isRegularFile(pdfPath) || Files.size(pdfPath) <= 0) {
            throw new ServiceException("PDF文件无效或为空");
        }
        byte[] header = new byte[5];
        try (InputStream in = Files.newInputStream(pdfPath)) {
            int read = in.read(header);
            if (read < 5 || header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F') {
                throw new ServiceException("生成的文件不是有效PDF");
            }
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void deleteDirectoryQuietly(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {
                            // ignore
                        }
                    });
        } catch (Exception ignored) {
            // ignore
        }
    }
}
