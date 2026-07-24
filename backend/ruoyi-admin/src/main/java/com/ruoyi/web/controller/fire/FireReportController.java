package com.ruoyi.web.controller.fire;

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
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireCompanyService;
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

    @Autowired
    private IFireCompanyService fireCompanyService;

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
     * 手动生成报告（需同时提交客户ID与任务ID，后端校验归属）
     */
    @RequiresPermissions("fire:report:generate")
    @Log(title = "维保报告生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    @ResponseBody
    public AjaxResult generate(Long companyId, Long taskId) {
        try {
            if (companyId == null) {
                return AjaxResult.error("请选择客户名称");
            }
            if (taskId == null) {
                return AjaxResult.error("请选择维保任务");
            }
            FireReportRecord record = fireReportRecordService.generateReportForTask(companyId, taskId);
            return AjaxResult.success("报告生成成功", record);
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("报告生成失败", e);
            return AjaxResult.error("报告生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成报告可选客户列表（当前用户有权查看）
     */
    @RequiresPermissions("fire:report:generate")
    @GetMapping("/companies")
    @ResponseBody
    public AjaxResult companies() {
        if (ShiroUtils.getSysUser() != null && ShiroUtils.getSysUser().isAdmin()) {
            return AjaxResult.success(fireCompanyService.selectCompanyAll());
        }
        Long userId = ShiroUtils.getUserId();
        List<FireCompany> bound = fireCompanyService.selectCompanyListByUserId(userId);
        if (bound != null && !bound.isEmpty()) {
            return AjaxResult.success(bound);
        }
        // 未绑定客户的后台账号：与维保任务页 company/all 一致
        return AjaxResult.success(fireCompanyService.selectCompanyAll());
    }

    /**
     * 按客户ID查询可生成报告的维保任务
     */
    @RequiresPermissions("fire:report:generate")
    @GetMapping("/tasks")
    @ResponseBody
    public AjaxResult tasksByCompany(Long companyId) {
        if (companyId == null) {
            return AjaxResult.error("请先选择客户");
        }
        FireCompany company = fireCompanyService.selectFireCompanyById(companyId);
        if (company == null || !"0".equals(company.getStatus())) {
            return AjaxResult.error("客户不存在或无权访问");
        }
        if (!canAccessCompany(companyId)) {
            return AjaxResult.error("客户不存在或无权访问");
        }
        FireMaintenanceTask query = new FireMaintenanceTask();
        query.setCompanyId(companyId);
        List<FireMaintenanceTask> tasks = fireMaintenanceTaskService.selectFireMaintenanceTaskList(query);
        return AjaxResult.success(tasks != null ? tasks : java.util.Collections.emptyList());
    }

    /** 与生成接口一致的客户访问判断 */
    private boolean canAccessCompany(Long companyId) {
        if (ShiroUtils.getSysUser() != null && ShiroUtils.getSysUser().isAdmin()) {
            return true;
        }
        Long userId = ShiroUtils.getUserId();
        List<FireCompany> bound = fireCompanyService.selectCompanyListByUserId(userId);
        if (bound != null && !bound.isEmpty()) {
            return bound.stream().anyMatch(item -> companyId.equals(item.getCompanyId()));
        }
        List<FireCompany> all = fireCompanyService.selectCompanyAll();
        return all != null && all.stream().anyMatch(item -> companyId.equals(item.getCompanyId()));
    }

    /**
     * 下载报告（PDF；历史 DOCX 仍可下载但不可在线预览）
     */
    @RequiresPermissions("fire:report:list")
    @GetMapping("/download/{reportId}")
    public void download(@PathVariable("reportId") Long reportId, HttpServletRequest request,
            HttpServletResponse response) {
        writeReport(reportId, response, true);
    }

    /**
     * 预览报告（inline PDF 流）
     */
    @RequiresPermissions("fire:report:list")
    @GetMapping("/preview/{reportId}")
    public void preview(@PathVariable("reportId") Long reportId, HttpServletRequest request,
            HttpServletResponse response) {
        writeReport(reportId, response, false);
    }

    /**
     * 报告预览页（系统内相对路由，无写死域名/端口）
     */
    @RequiresPermissions("fire:report:list")
    @GetMapping("/view/{reportId}")
    public String viewPage(@PathVariable("reportId") Long reportId, ModelMap mmap) {
        mmap.put("reportId", reportId);
        return prefix + "/preview";
    }

    /**
     * 预览前校验
     */
    @RequiresPermissions("fire:report:list")
    @GetMapping("/check/{reportId}")
    @ResponseBody
    public AjaxResult check(@PathVariable("reportId") Long reportId) {
        try {
            FireReportRecord record = fireReportRecordService.selectFireReportRecordById(reportId);
            fireReportRecordService.assertReportFileReady(record);
            return AjaxResult.success("ok");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    private void writeReport(Long reportId, HttpServletResponse response, boolean attachment) {
        try {
            FireReportRecord record = fireReportRecordService.selectFireReportRecordById(reportId);
            if (record == null) {
                writeHtmlError(response, HttpServletResponse.SC_NOT_FOUND, "报告不存在");
                return;
            }
            if (!attachment) {
                try {
                    fireReportRecordService.assertReportFileReady(record);
                } catch (ServiceException e) {
                    writeHtmlError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return;
                }
            }

            Path filePath = fireReportRecordService.resolveReportFile(record);
            if (filePath == null || !Files.exists(filePath)) {
                writeHtmlError(response, HttpServletResponse.SC_NOT_FOUND, "报告文件不存在或已被删除");
                return;
            }

            String fileName = record.getReportName() != null ? record.getReportName() : filePath.getFileName().toString();
            String lower = filePath.getFileName().toString().toLowerCase();
            String contentType;
            if (lower.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (lower.endsWith(".docx")) {
                if (!attachment) {
                    writeHtmlError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "历史报告为 Word 格式，无法在线预览，请重新生成 PDF 报告");
                    return;
                }
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else {
                writeHtmlError(response, HttpServletResponse.SC_BAD_REQUEST, "不支持的报告文件格式");
                return;
            }

            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            String dispositionType = attachment ? "attachment" : "inline";
            response.setContentType(contentType);
            response.setHeader("Content-Disposition",
                    dispositionType + "; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentLengthLong(Files.size(filePath));

            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                    OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error(attachment ? "下载报告失败" : "预览报告失败", e);
            try {
                writeHtmlError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "报告读取失败，请稍后重试");
            } catch (Exception ignored) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void writeHtmlError(HttpServletResponse response, int status, String message) throws Exception {
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType("text/html;charset=UTF-8");
        String safe = message == null ? "操作失败" : message.replace("<", "&lt;").replace(">", "&gt;");
        String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>报告提示</title></head>"
                + "<body style=\"font-family:Microsoft YaHei,sans-serif;padding:40px;color:#333;\">"
                + "<h3>" + safe + "</h3></body></html>";
        response.getWriter().write(html);
        response.getWriter().flush();
    }

    /**
     * 新增定时任务 / 手动生成 页面
     */
    @GetMapping("/addJob")
    public String addJob(String type, ModelMap mmap) {
        mmap.put("type", type);
        // 定时任务仍保留原全部任务下拉；手动生成改为前端按客户联动加载
        if (!"manual".equals(type)) {
            List<FireMaintenanceTask> tasks = fireMaintenanceTaskService
                    .selectFireMaintenanceTaskList(new FireMaintenanceTask());
            mmap.put("tasks", tasks);
        } else {
            mmap.put("tasks", java.util.Collections.emptyList());
        }
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
     * 获取报告文件路径（已废弃，改由 Service.resolveReportFile）
     */
    @Deprecated
    private Path getReportFilePath(String fileName) {
        FireReportRecord probe = new FireReportRecord();
        probe.setFilePath(fileName);
        Path resolved = fireReportRecordService.resolveReportFile(probe);
        return resolved != null ? resolved : Paths.get(System.getProperty("user.dir"), "report", fileName);
    }
}
