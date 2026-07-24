package com.ruoyi.system.service.report;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * DOCX �� PDF��ͨ�� LibreOffice �޽���ת������֤��ʽ�����Ŀ����ԡ�
 * <p>
 * ����Ҫ�󣺷�������װ LibreOffice������ {@code ruoyi.report.libreOfficePath} ���� soffice ·����
 */
@Component
public class DocxToPdfConverter {

    private static final Logger log = LoggerFactory.getLogger(DocxToPdfConverter.class);

    private static final long CONVERT_TIMEOUT_SECONDS = 180L;

    @Value("${ruoyi.report.libreOfficePath:}")
    private String configuredLibreOfficePath;

    /**
     * �� DOCX תΪ PDF���ɹ��� pdfPath ����������ļ�ͷΪ %PDF��
     */
    public void convert(Path docxPath, Path pdfPath) {
        if (docxPath == null || !Files.isRegularFile(docxPath)) {
            throw new ServiceException("DOCX Դ�ļ������ڣ��޷�ת��Ϊ PDF");
        }
        try {
            Files.createDirectories(pdfPath.getParent());
        } catch (Exception e) {
            throw new ServiceException("���� PDF ���Ŀ¼ʧ��: " + e.getMessage());
        }

        Path soffice = resolveSofficeExecutable();
        if (soffice == null) {
            throw new ServiceException(
                    "������δ��װ LibreOffice���޷����� PDF���밲װ LibreOffice��"
                            + "���������� ruoyi.report.libreOfficePath ��ָ�� soffice ��ִ���ļ�·����");
        }

        try {
            convertWithLibreOffice(soffice, docxPath, pdfPath);
            assertValidPdf(pdfPath);
            log.info("LibreOffice ת�� PDF �ɹ�: {}", pdfPath);
        } catch (ServiceException e) {
            deleteQuietly(pdfPath);
            throw e;
        } catch (Exception e) {
            deleteQuietly(pdfPath);
            throw new ServiceException("DOCX ת PDF ʧ��: " + e.getMessage());
        }
    }

    private void convertWithLibreOffice(Path soffice, Path docxPath, Path pdfPath) throws Exception {
        Path workDir = Files.createTempDirectory("fire-report-lo-");
        try {
            Path inputCopy = workDir.resolve("source.docx");
            Files.copy(docxPath, inputCopy);

            List<String> command = new ArrayList<>();
            command.add(soffice.toAbsolutePath().toString());
            command.add("--headless");
            command.add("--nologo");
            command.add("--nolockcheck");
            command.add("--nodefault");
            command.add("--nofirststartwizard");
            command.add("--convert-to");
            command.add("pdf:writer_pdf_Export");
            command.add("--outdir");
            command.add(workDir.toAbsolutePath().toString());
            command.add(inputCopy.toAbsolutePath().toString());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.directory(workDir.toFile());
            Process process = pb.start();
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }
            boolean finished = process.waitFor(CONVERT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new ServiceException("LibreOffice ת����ʱ");
            }
            if (process.exitValue() != 0) {
                throw new ServiceException("LibreOffice �˳���=" + process.exitValue() + ", ���=" + output);
            }

            Path produced = workDir.resolve("source.pdf");
            if (!Files.isRegularFile(produced)) {
                throw new ServiceException("LibreOffice δ���� PDF �ļ�, ���=" + output);
            }
            Files.copy(produced, pdfPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } finally {
            deleteDirectoryQuietly(workDir);
        }
    }

    private Path resolveSofficeExecutable() {
        List<String> candidates = new ArrayList<>();
        if (StringUtils.isNotEmpty(configuredLibreOfficePath)) {
            candidates.add(configuredLibreOfficePath.trim());
        }
        candidates.add("soffice");
        candidates.add("soffice.exe");
        candidates.add("C:\\Program Files\\LibreOffice\\program\\soffice.exe");
        candidates.add("C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe");
        candidates.add("/usr/bin/soffice");
        candidates.add("/usr/lib/libreoffice/program/soffice");
        candidates.add("/opt/libreoffice/program/soffice");
        candidates.add("/Applications/LibreOffice.app/Contents/MacOS/soffice");

        for (String candidate : candidates) {
            Path path = Paths.get(candidate);
            if (path.isAbsolute() && Files.isRegularFile(path)) {
                return path;
            }
            if (!path.isAbsolute()) {
                Path found = findOnPath(candidate);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
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
            Path candidate = Paths.get(part, executable);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static void assertValidPdf(Path pdfPath) throws Exception {
        if (pdfPath == null || !Files.isRegularFile(pdfPath) || Files.size(pdfPath) < 5) {
            throw new ServiceException("PDF �ļ���Ч��Ϊ��");
        }
        byte[] header = new byte[5];
        try (InputStream in = Files.newInputStream(pdfPath)) {
            int read = in.read(header);
            if (read < 5 || header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F') {
                throw new ServiceException("���ɵ��ļ�������Ч PDF");
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
