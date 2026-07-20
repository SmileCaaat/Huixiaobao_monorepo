package com.ruoyi.quartz.task;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.system.service.IMaintenanceReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 维保报告定时任务
 */
@Component("maintenanceReportTask")
public class MaintenanceReportTask {

    @Autowired
    private IMaintenanceReportService reportService;

    public void generateReport() {
        System.out.println("开始执行维保报告生成任务");
        // 模拟数据
        Map<String, Object> data = new HashMap<>();
        data.put("title", "定时任务生成报告");
        data.put("date", new java.util.Date().toString());
        data.put("author", "系统自动生成");
        data.put("projectName", "自动维保项目");

        String templatePath = System.getProperty("user.dir") + "/doc/空白模板维保报告.docx";
        String fileName = "Auto_Report_" + System.currentTimeMillis() + ".docx";
        String downloadPath = RuoYiConfig.getDownloadPath();

        File desc = new File(downloadPath);
        if (!desc.exists()) {
            if (!desc.getParentFile().exists()) {
                desc.getParentFile().mkdirs();
            }
            desc.mkdirs();
        }

        String outputPath = downloadPath + fileName;

        try {
            reportService.generateReport(data, templatePath, outputPath);
            System.out.println("定时任务报告生成成功: " + outputPath);
        } catch (Exception e) {
            System.err.println("定时任务报告生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
