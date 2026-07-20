package com.ruoyi.web.controller.system;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.service.IMaintenanceReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.*;

/**
 * 维保报告控制器
 */
@Controller
@RequestMapping("/system/report")
public class MaintenanceReportController extends BaseController {

    @Autowired
    private IMaintenanceReportService reportService;

    @GetMapping()
    public String report() {
        return "system/report/report";
    }

    @GetMapping("/generate")
    @ResponseBody
    public AjaxResult generate() {
        // 模拟数据
        Map<String, Object> data = new HashMap<>();
        data.put("title", "2023年度维保报告");
        data.put("date", "2023-10-27");
        data.put("author", "系统管理员");
        data.put("projectName", "某某项目");
        // TODO: 根据实际模板占位符添加更多数据

        String templatePath = System.getProperty("user.dir") + "/doc/空白模板维保报告.docx";
        String fileName = "维保报告_" + System.currentTimeMillis() + ".docx";
        String downloadPath = RuoYiConfig.getDownloadPath();

        File desc = new File(downloadPath);
        if (!desc.exists()) {
            if (!desc.getParentFile().exists()) {
                desc.getParentFile().mkdirs();
            }
        }

        String outputPath = downloadPath + fileName;

        try {
            reportService.generateReport(data, templatePath, outputPath);
            return AjaxResult.success("报告生成成功", fileName);
        } catch (Exception e) {
            return AjaxResult.error("报告生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public void download(javax.servlet.http.HttpServletResponse response) {
        // 模拟数据
        Map<String, Object> data = new HashMap<>();
        data.put("title", "2023年度维保报告");
        data.put("date", "2023-10-27");
        data.put("author", "系统管理员");
        data.put("projectName", "某某项目");
        // TODO: 根据实际模板占位符添加更多数据

        String templatePath = System.getProperty("user.dir") + "/doc/空白模板维保报告.docx";
        String fileName = "维保报告_" + System.currentTimeMillis() + ".docx";

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            reportService.generateReport(data, templatePath, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list() {
        List<Map<String, Object>> list = new ArrayList<>();
        String downloadPath = RuoYiConfig.getDownloadPath();
        File dir = new File(downloadPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".docx") || name.endsWith(".doc"));
            if (files != null) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                for (File file : files) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fileName", file.getName());
                    map.put("createTime", file.lastModified()); // Frontend can format timestamp
                    map.put("size", String.format("%.2f KB", file.length() / 1024.0));
                    list.add(map);
                }
            }
        }
        return getDataTable(list);
    }

    @GetMapping("/downloadFile")
    public void downloadFile(String fileName, javax.servlet.http.HttpServletResponse response) {
        try {
            String downloadPath = RuoYiConfig.getDownloadPath();
            String filePath = downloadPath + fileName;
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));

            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String fileName) {
        String downloadPath = RuoYiConfig.getDownloadPath();
        File file = new File(downloadPath + fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return AjaxResult.success();
            }
        }
        return AjaxResult.error("删除失败");
    }
}
