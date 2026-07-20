package com.ruoyi.web.controller.fire;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.service.ISysJobService;
import com.ruoyi.system.domain.FireReportRecord;
import com.ruoyi.system.service.IFireReportRecordService;

/**
 * 维保报告Controller
 * 
 * @author ruoyi
 * @date 2025-01-05
 */
@Controller
@RequestMapping("/fire/report")
public class FireReportController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(FireReportController.class);

    private String prefix = "fire/report";

    @Autowired
    private IFireReportRecordService fireReportRecordService;

    @Autowired
    private ISysJobService jobService;

    @Autowired
    private IFireMaintenanceTaskService fireMaintenanceTaskService;

    @RequiresPermissions("fire:report:view")
    @GetMapping()
    public String report() {
        return prefix + "/report";
    }

    /**
     * 查询维保报告记录列表
     */
    @RequiresPermissions("fire:report:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireReportRecord fireReportRecord) {
        startPage();
        List<FireReportRecord> list = fireReportRecordService.selectFireReportRecordList(fireReportRecord);
        return getDataTable(list);
    }

    /**
     * 导出维保报告记录列表
     */
    @RequiresPermissions("fire:report:export")
    @Log(title = "维保报告记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireReportRecord fireReportRecord) {
        List<FireReportRecord> list = fireReportRecordService.selectFireReportRecordList(fireReportRecord);
        ExcelUtil<FireReportRecord> util = new ExcelUtil<FireReportRecord>(FireReportRecord.class);
        return util.exportExcel(list, "report");
    }

    /**
     * 删除维保报告记录
     */
    @RequiresPermissions("fire:report:remove")
    @Log(title = "维保报告记录", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(fireReportRecordService.deleteFireReportRecordByIds(ids));
    }

    /**
     * 手动生成报告
     */
    @RequiresPermissions("fire:report:generate")
    @Log(title = "维保报告生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    @ResponseBody
    public AjaxResult generate(Long taskId) {
        try {
            FireReportRecord record = fireReportRecordService.generateReportForTask(taskId);
            return AjaxResult.success("报告生成成功", record);
        } catch (Exception e) {
            log.error("报告生成失败", e);
            return AjaxResult.error("报告生成失败: " + e.getMessage());
        }
    }

    /**
     * 下载报告
     */
    @GetMapping("/download/{reportId}")
    public void download(@PathVariable("reportId") Long reportId, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            FireReportRecord record = fireReportRecordService.selectFireReportRecordById(reportId);
            if (record == null || record.getFilePath() == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Path filePath = getReportFilePath(record.getFilePath());
            if (!Files.exists(filePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String fileName = record.getReportName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentLengthLong(Files.size(filePath));

            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                    OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("下载报告失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 预览报告（直接在浏览器打开）
     */
    @GetMapping("/preview/{reportId}")
    public void preview(@PathVariable("reportId") Long reportId, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            FireReportRecord record = fireReportRecordService.selectFireReportRecordById(reportId);
            if (record == null || record.getFilePath() == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Path filePath = getReportFilePath(record.getFilePath());
            if (!Files.exists(filePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String fileName = record.getReportName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition",
                    "inline; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentLengthLong(Files.size(filePath));

            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                    OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("预览报告失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 新增定时任务页面
     */
    @GetMapping("/addJob")
    public String addJob(ModelMap mmap) {
        // 获取所有维保任务供选择
        List<FireMaintenanceTask> tasks = fireMaintenanceTaskService
                .selectFireMaintenanceTaskList(new FireMaintenanceTask());
        mmap.put("tasks", tasks);
        return prefix + "/addJob";
    }

    /**
     * 保存定时任务
     */
    @RequiresPermissions("fire:report:add")
    @Log(title = "维保报告定时任务", businessType = BusinessType.INSERT)
    @PostMapping("/addJob")
    @ResponseBody
    public AjaxResult addJobSave(Long taskId, String cronExpression, String jobName) {
        try {
            SysJob job = new SysJob();
            job.setJobName(jobName);
            job.setJobGroup("FIRE_REPORT");
            job.setInvokeTarget("fireReportTask.execute('" + taskId + "')");
            job.setCronExpression(cronExpression);
            job.setMisfirePolicy("1"); // 立即执行
            job.setConcurrent("0"); // 禁止并发
            job.setStatus("0"); // 正常
            return toAjax(jobService.insertJob(job));
        } catch (Exception e) {
            return AjaxResult.error("创建定时任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取报告文件路径
     */
    private Path getReportFilePath(String fileName) {
        try {
            // 尝试获取 classpath:static 目录（开发环境）
            File staticDir = ResourceUtils.getFile("classpath:static");
            return Paths.get(staticDir.getAbsolutePath(), "report", fileName);
        } catch (Exception e) {
            // 生产环境：使用应用运行目录
            return Paths.get(System.getProperty("user.dir"), "report", fileName);
        }
    }
}
