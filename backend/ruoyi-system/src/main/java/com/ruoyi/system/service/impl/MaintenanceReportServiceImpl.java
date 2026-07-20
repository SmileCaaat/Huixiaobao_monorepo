package com.ruoyi.system.service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.IMaintenanceReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * 维保报告服务实现
 */
@Service
public class MaintenanceReportServiceImpl implements IMaintenanceReportService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceReportServiceImpl.class);

    @Override
    public String generateReport(Map<String, Object> data, String templatePath, String outputPath) {
        log.info("Starting to generate report. Template: {}, Output: {}", templatePath, outputPath);
        // 配置表格渲染策略
        com.deepoove.poi.config.Configure config = com.deepoove.poi.config.Configure.builder()
            .bind("buildingTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("checkTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("testTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("footerTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("testRecordTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("repairRecordTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("maintenanceRecordTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .build();
            
        try (XWPFTemplate template = XWPFTemplate.compile(templatePath, config).render(data)) {
            template.writeToFile(outputPath);
            log.info("Report generated successfully: {}", outputPath);
            return outputPath;
        } catch (IOException e) {
            log.error("Failed to generate report", e);
            throw new ServiceException("生成维保报告失败: " + e.getMessage());
        }
    }

    @Override
    public void generateReport(Map<String, Object> data, String templatePath, java.io.OutputStream outputStream) {
        log.info("Starting to generate report to OutputStream. Template: {}", templatePath);
        // 配置表格渲染策略
        com.deepoove.poi.config.Configure config = com.deepoove.poi.config.Configure.builder()
            .bind("buildingTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("checkTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("testTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("footerTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("testRecordTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("repairRecordTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .bind("maintenanceRecordTable", new com.deepoove.poi.policy.TableRenderPolicy())
            .build();
            
        try (XWPFTemplate template = XWPFTemplate.compile(templatePath, config).render(data)) {
            template.write(outputStream);
            log.info("Report generated to OutputStream successfully");
        } catch (IOException e) {
            log.error("Failed to generate report to OutputStream", e);
            throw new ServiceException("生成维保报告失败: " + e.getMessage());
        }
    }
}
