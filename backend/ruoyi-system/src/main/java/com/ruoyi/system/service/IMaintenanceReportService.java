package com.ruoyi.system.service;

import java.util.Map;

/**
 * 维保报告服务接口
 */
public interface IMaintenanceReportService {

    /**
     * 生成维保报告
     * 
     * @param data         填充数据
     * @param templatePath 模板路径
     * @param outputPath   输出路径
     * @return 生成的文件路径
     */
    String generateReport(Map<String, Object> data, String templatePath, String outputPath);

    /**
     * 生成维保报告到输出流
     *
     * @param data         填充数据
     * @param templatePath 模板路径
     * @param outputStream 输出流
     */
    void generateReport(Map<String, Object> data, String templatePath, java.io.OutputStream outputStream);
}
