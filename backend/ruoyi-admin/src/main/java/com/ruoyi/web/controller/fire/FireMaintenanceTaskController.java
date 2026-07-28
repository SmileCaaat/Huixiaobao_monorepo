package com.ruoyi.web.controller.fire;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.domain.FireMaintenanceTemplate;
import com.ruoyi.fire.service.IFireDataPermissionService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 维保任务Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Controller
@RequestMapping("/fire/task")
public class FireMaintenanceTaskController extends BaseController {
    private String prefix = "fire/task";

    @Autowired
    private IFireMaintenanceTaskService fireMaintenanceTaskService;

    @Autowired
    private IFireDataPermissionService fireDataPermissionService;

    /**
     * 维保任务页面
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping()
    public String task() {
        return prefix + "/task";
    }

    /**
     * 新增维保任务页面
     */
    @GetMapping("/add")
    public String add(org.springframework.ui.ModelMap mmap) {
        // 加载用户列表
        com.ruoyi.common.core.domain.entity.SysUser userQuery = new com.ruoyi.common.core.domain.entity.SysUser();
        java.util.List<com.ruoyi.common.core.domain.entity.SysUser> users = com.ruoyi.common.utils.spring.SpringUtils
                .getBean(com.ruoyi.system.service.ISysUserService.class).selectUserList(userQuery);
        mmap.put("users", users);

        // 加载所有建筑列表
        com.ruoyi.fire.domain.FireBuilding buildingQuery = new com.ruoyi.fire.domain.FireBuilding();
        java.util.List<com.ruoyi.fire.domain.FireBuilding> buildings = com.ruoyi.common.utils.spring.SpringUtils
                .getBean(com.ruoyi.fire.service.IFireBuildingService.class).selectBuildingList(buildingQuery);
        mmap.put("buildings", buildings);

        return prefix + "/add";
    }

    /**
     * 修改维保任务页面
     */
    @RequiresPermissions("fire:task:edit")
    @GetMapping("/edit/{taskId}")
    public String edit(@PathVariable("taskId") Long taskId, org.springframework.ui.ModelMap mmap) {
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        fireDataPermissionService.assertCanAccessTask(ShiroUtils.getSysUser(), task);
        mmap.put("task", task);

        // 加载用户列表
        com.ruoyi.common.core.domain.entity.SysUser userQuery = new com.ruoyi.common.core.domain.entity.SysUser();
        java.util.List<com.ruoyi.common.core.domain.entity.SysUser> users = com.ruoyi.common.utils.spring.SpringUtils
                .getBean(com.ruoyi.system.service.ISysUserService.class).selectUserList(userQuery);
        mmap.put("users", users);

        // 加载建筑列表
        com.ruoyi.fire.domain.FireBuilding buildingQuery = new com.ruoyi.fire.domain.FireBuilding();
        if (task.getCompanyId() != null) {
            buildingQuery.setCompanyId(task.getCompanyId());
        }
        java.util.List<com.ruoyi.fire.domain.FireBuilding> buildings = com.ruoyi.common.utils.spring.SpringUtils
                .getBean(com.ruoyi.fire.service.IFireBuildingService.class).selectBuildingList(buildingQuery);
        mmap.put("buildings", buildings);

        return prefix + "/edit";
    }

    /**
     * 维保任务详情页面（常规维保）
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/detail/{taskId}")
    public String detail(@PathVariable("taskId") Long taskId, org.springframework.ui.ModelMap mmap) {
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId, "0");
        mmap.put("task", task);
        return prefix + "/detail";
    }

    /**
     * 消防测试维护详情页面（消防设施测试）
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/fireTestDetail/{taskId}")
    public String fireTestDetail(@PathVariable("taskId") Long taskId, org.springframework.ui.ModelMap mmap) {
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId, "1");
        mmap.put("task", task);
        return prefix + "/fire_test_detail";
    }

    /**
     * 消防测试系统列表页面
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/fireTestSystem/{recordId}")
    public String fireTestSystem(@PathVariable("recordId") Long recordId, org.springframework.ui.ModelMap mmap) {
        com.ruoyi.fire.domain.FireMaintenanceRecord system = getRecordService()
                .selectFireMaintenanceRecordByRecordId(recordId);
        mmap.put("system", system);

        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> equipments = getRecordService()
                .selectLevel2List(system.getTaskId(), recordId);

        for (com.ruoyi.fire.domain.FireMaintenanceRecord equipment : equipments) {
            calculateEquipmentStats(equipment, system.getTaskId());
        }

        mmap.put("equipments", equipments);
        return prefix + "/fire_test_system";
    }

    /**
     * 消防测试设备检查项列表页面
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/fireTestEquipment/{recordId}")
    public String fireTestEquipment(@PathVariable("recordId") Long recordId, org.springframework.ui.ModelMap mmap) {
        com.ruoyi.fire.domain.FireMaintenanceRecord equipment = getRecordService()
                .selectFireMaintenanceRecordByRecordId(recordId);
        mmap.put("equipment", equipment);

        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> checkItems = getRecordService()
                .selectLevel3List(equipment.getTaskId(), recordId);
        mmap.put("checkItems", checkItems);

        return prefix + "/fire_test_equipment";
    }

    /**
     * 查询维保任务列表
     * 超级管理员可查看所有任务；
     * 普通用户只能查看自己作为「项目负责人 / 执行人 / 维保操作员」的任务。
     */
    @RequiresPermissions("fire:task:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireMaintenanceTask fireMaintenanceTask) {
        fireDataPermissionService.applyTaskListScope(fireMaintenanceTask, ShiroUtils.getSysUser());
        sanitizeDashboardFilter(fireMaintenanceTask);
        startPage();
        List<FireMaintenanceTask> list = fireMaintenanceTaskService.selectFireMaintenanceTaskList(fireMaintenanceTask);
        return getDataTable(list);
    }

    /**
     * 导出维保任务列表
     */
    @RequiresPermissions("fire:task:export")
    @Log(title = "维保任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, FireMaintenanceTask fireMaintenanceTask) {
        fireDataPermissionService.applyTaskListScope(fireMaintenanceTask, ShiroUtils.getSysUser());
        sanitizeDashboardFilter(fireMaintenanceTask);
        List<FireMaintenanceTask> list = fireMaintenanceTaskService.selectFireMaintenanceTaskList(fireMaintenanceTask);
        ExcelUtil<FireMaintenanceTask> util = new ExcelUtil<FireMaintenanceTask>(FireMaintenanceTask.class);
        util.exportExcel(response, list, "维保任务数据");
    }

    /**
     * 获取维保任务详细信息
     */
    @RequiresPermissions("fire:task:query")
    @GetMapping(value = "/{taskId}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId) {
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        fireDataPermissionService.assertCanAccessTask(ShiroUtils.getSysUser(), task);
        return success(task);
    }

    /**
     * 新增维保任务
     */
    @RequiresPermissions("fire:task:add")
    @Log(title = "维保任务", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(FireMaintenanceTask fireMaintenanceTask) {
        if (fireMaintenanceTask.getManagerId() != null) {
            fireDataPermissionService.assertUserDispatchable(fireMaintenanceTask.getManagerId());
        }
        return toAjax(fireMaintenanceTaskService.insertFireMaintenanceTask(fireMaintenanceTask));
    }

    /**
     * 修改维保任务
     */
    @RequiresPermissions("fire:task:edit")
    @Log(title = "维保任务", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(FireMaintenanceTask fireMaintenanceTask) {
        if (fireMaintenanceTask.getManagerId() != null) {
            fireDataPermissionService.assertUserDispatchable(fireMaintenanceTask.getManagerId());
        }
        try {
            if (fireMaintenanceTask.getTaskId() == null) {
                return error("任务ID不能为空");
            }
            FireMaintenanceTask existing = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(fireMaintenanceTask.getTaskId());
            if (existing == null) {
                return error("维保任务不存在");
            }
            fireDataPermissionService.assertCanAccessTask(ShiroUtils.getSysUser(), existing);
            if (StringUtils.isEmpty(fireMaintenanceTask.getTaskName())) {
                return error("任务名称不能为空");
            }
            if (fireMaintenanceTask.getManagerId() == null) {
                return error("请选择项目负责人");
            }
            if (fireMaintenanceTask.getPlanStartTime() != null && fireMaintenanceTask.getPlanEndTime() != null
                    && fireMaintenanceTask.getPlanStartTime().after(fireMaintenanceTask.getPlanEndTime())) {
                return error("计划开始时间不能晚于计划结束时间");
            }
            // 完整编辑：允许清空可选字段，并按选择差异同步检查记录
            // 空串=清空；null/未传=不修改（由 Service.resolveSelectedLevel1Ids 处理）
            fireMaintenanceTask.getParams().put("fullEdit", true);
            fireMaintenanceTask.setUpdateBy(ShiroUtils.getLoginName());
            fireMaintenanceTask.setUpdateTime(new Date());
            int rows = fireMaintenanceTaskService.updateFireMaintenanceTask(fireMaintenanceTask);
            AjaxResult ajax = toAjax(rows);
            Object syncMessage = fireMaintenanceTask.getParams().get("syncMessage");
            if (rows > 0 && syncMessage != null) {
                ajax.put("msg", "修改成功。" + syncMessage);
            }
            return ajax;
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 删除维保任务
     */
    @RequiresPermissions("fire:task:remove")
    @Log(title = "维保任务", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        Long[] taskIds = convertStrToLongArray(ids);
        SysUser user = ShiroUtils.getSysUser();
        for (Long taskId : taskIds) {
            FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
            fireDataPermissionService.assertCanAccessTask(user, task);
        }
        return toAjax(fireMaintenanceTaskService.deleteFireMaintenanceTaskByTaskIds(taskIds));
    }

    /**
     * 维保简报编辑页面
     */
    @RequiresPermissions("fire:task:briefing")
    @GetMapping("/briefing/{taskId}")
    public String briefing(@PathVariable("taskId") Long taskId, org.springframework.ui.ModelMap mmap) {
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        try {
            fireDataPermissionService.assertCanAccessTask(ShiroUtils.getSysUser(), task);
        } catch (ServiceException e) {
            return "error/unauth";
        }
        mmap.put("task", task);
        return prefix + "/briefing";
    }

    /**
     * 保存维保简报（仅更新简报字段，不走通用任务更新）
     */
    @RequiresPermissions("fire:task:briefing")
    @Log(title = "维保简报", businessType = BusinessType.UPDATE)
    @PostMapping("/saveBriefing")
    @ResponseBody
    public AjaxResult saveBriefing(Long taskId, String maintenanceSummary, Date maintenanceTime) {
        try {
            if (taskId == null) {
                return error("任务ID不能为空");
            }
            FireMaintenanceTask existing = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
            fireDataPermissionService.assertCanAccessTask(ShiroUtils.getSysUser(), existing);
            // 仅允许已下发（进行中=1）或已完成（2）填写简报；0=待执行
            String status = existing.getTaskStatus();
            if (!"1".equals(status) && !"2".equals(status)) {
                return error("请先下发任务后再填写维保简报");
            }
            return toAjax(fireMaintenanceTaskService.updateTaskBriefing(taskId, maintenanceSummary,
                    maintenanceTime, ShiroUtils.getLoginName()));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 系统列表页面
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/system/{recordId}")
    public String system(@PathVariable("recordId") Long recordId, org.springframework.ui.ModelMap mmap) {
        // 获取系统信息和设备列表
        com.ruoyi.fire.domain.FireMaintenanceRecord system = getRecordService()
                .selectFireMaintenanceRecordByRecordId(recordId);
        mmap.put("system", system);

        // 获取该系统下的所有设备（二级）
        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> equipments = getRecordService()
                .selectLevel2List(system.getTaskId(), recordId);

        // 为每个设备计算统计数据
        for (com.ruoyi.fire.domain.FireMaintenanceRecord equipment : equipments) {
            calculateEquipmentStats(equipment, system.getTaskId());
        }

        mmap.put("equipments", equipments);

        return prefix + "/system";
    }

    /**
     * 计算设备的统计数据
     */
    private void calculateEquipmentStats(com.ruoyi.fire.domain.FireMaintenanceRecord equipment, Long taskId) {
        // 获取该设备下的所有三级检查项
        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> checkItems = getRecordService()
                .selectLevel3List(taskId, equipment.getRecordId());

        int totalItems = checkItems.size();
        int completedItems = 0;

        for (com.ruoyi.fire.domain.FireMaintenanceRecord item : checkItems) {
            String result = item.getCheckResult();
            if (result != null && !"0".equals(result)) {
                completedItems++;
            }
        }

        equipment.setTotalItems(totalItems);
        equipment.setCompletedItems(completedItems);
        equipment.setUncompletedItems(totalItems - completedItems);
        equipment.setSystemStatus(completedItems == totalItems && totalItems > 0 ? "1" : "0");
    }

    /**
     * 设备检查项列表页面
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/equipment/{recordId}")
    public String equipment(@PathVariable("recordId") Long recordId, org.springframework.ui.ModelMap mmap) {
        // 获取设备信息
        com.ruoyi.fire.domain.FireMaintenanceRecord equipment = getRecordService()
                .selectFireMaintenanceRecordByRecordId(recordId);
        mmap.put("equipment", equipment);

        // 获取该设备下的所有检查项（三级）
        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> checkItems = getRecordService()
                .selectLevel3List(equipment.getTaskId(), recordId);
        mmap.put("checkItems", checkItems);

        return prefix + "/equipment";
    }

    /**
     * 更新检查结果
     */
    @PostMapping("/updateCheckResult")
    @ResponseBody
    public AjaxResult updateCheckResult(Long recordId, String checkResult) {
        com.ruoyi.fire.domain.FireMaintenanceRecord record = new com.ruoyi.fire.domain.FireMaintenanceRecord();
        record.setRecordId(recordId);
        record.setCheckResult(checkResult);
        return toAjax(getRecordService().updateCheckResult(record));
    }

    /**
     * 更新故障描述
     */
    @PostMapping("/updateFaultDesc")
    @ResponseBody
    public AjaxResult updateFaultDesc(Long recordId, String faultDescription) {
        com.ruoyi.fire.domain.FireMaintenanceRecord record = new com.ruoyi.fire.domain.FireMaintenanceRecord();
        record.setRecordId(recordId);
        record.setFaultDescription(faultDescription);
        return toAjax(getRecordService().updateFireMaintenanceRecord(record));
    }

    /**
     * 更新检查详情
     * 仅更新本次传入的非空字段；checkResult 未传时不覆盖外层状态。
     * otherNotes / faultImages 允许传空字符串以清空。
     */
    @PostMapping("/updateCheckDetail")
    @ResponseBody
    public AjaxResult updateCheckDetail(Long recordId, String checkResult, String faultDescription,
            String otherNotes, String faultImages) {
        com.ruoyi.fire.domain.FireMaintenanceRecord record = new com.ruoyi.fire.domain.FireMaintenanceRecord();
        record.setRecordId(recordId);
        // 未传 checkResult 时保持 null，MyBatis 动态 SQL 不会更新该列
        if (checkResult != null && !checkResult.isEmpty()) {
            record.setCheckResult(checkResult);
        }
        if (faultDescription != null) {
            record.setFaultDescription(faultDescription);
        }
        if (otherNotes != null) {
            record.setOtherNotes(otherNotes);
        }
        if (faultImages != null) {
            record.setFaultImages(faultImages);
        }

        return toAjax(getRecordService().updateCheckResult(record));
    }

    /**
     * 更新维护信息（第二级设备维护）
     */
    @PostMapping("/updateMaintenance")
    @ResponseBody
    public AjaxResult updateMaintenance(Long recordId, String deviceLocation, String testSituation,
            String testTime, String testResult, String sitePhotos) {
        com.ruoyi.fire.domain.FireMaintenanceRecord record = new com.ruoyi.fire.domain.FireMaintenanceRecord();
        record.setRecordId(recordId);
        record.setDeviceLocation(deviceLocation);
        record.setTestSituation(testSituation);
        record.setTestResult(testResult);
        record.setSitePhotos(sitePhotos);

        // 处理测试时间
        if (testTime != null && !testTime.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                record.setTestTime(sdf.parse(testTime));
            } catch (Exception e) {
                // 如果解析失败，忽略
            }
        }

        return toAjax(getRecordService().updateFireMaintenanceRecord(record));
    }

    /**
     * 删除照片
     */
    @PostMapping("/deletePhoto")
    @ResponseBody
    public AjaxResult deletePhoto(String key) {
        try {
            if (key == null || key.isEmpty()) {
                return AjaxResult.error("照片路径不能为空");
            }
            // 调用通用删除接口
            com.ruoyi.web.controller.common.CommonController commonController = com.ruoyi.common.utils.spring.SpringUtils
                    .getBean(com.ruoyi.web.controller.common.CommonController.class);
            return commonController.removeFile(key);
        } catch (Exception e) {
            return AjaxResult.error("删除照片失败: " + e.getMessage());
        }
    }

    /**
     * 下发任务
     */
    @RequiresPermissions("fire:task:edit")
    @Log(title = "下发维保任务", businessType = BusinessType.UPDATE)
    @PostMapping("/dispatch/{taskId}")
    @ResponseBody
    public AjaxResult dispatch(@PathVariable("taskId") Long taskId) {
        FireMaintenanceTask existing = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        fireDataPermissionService.assertCanDispatchTask(ShiroUtils.getSysUser(), existing);
        FireMaintenanceTask task = new FireMaintenanceTask();
        task.setTaskId(taskId);
        task.setTaskStatus("1"); // 设置为进行中
        return toAjax(fireMaintenanceTaskService.updateFireMaintenanceTask(task));
    }

    /**
     * 一键完成任务（所有检查项标记为正常）
     * 双层校验：Shiro fire:task:completeAll + 仅系统超级管理员(userId=1)可执行
     */
    @RequiresPermissions("fire:task:completeAll")
    @Log(title = "一键完成维保任务", businessType = BusinessType.UPDATE)
    @PostMapping("/completeAll/{taskId}")
    @ResponseBody
    public AjaxResult completeAll(@PathVariable("taskId") Long taskId) {
        SysUser user = ShiroUtils.getSysUser();
        if (user == null || !user.isAdmin()) {
            return error("只有超级管理员可以使用一键完成功能");
        }
        if (taskId == null) {
            return error("任务ID不能为空");
        }
        FireMaintenanceTask existing = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        if (existing == null) {
            return error("维保任务不存在");
        }
        // 仅进行中(taskStatus=1)允许一键完成
        if (!"1".equals(existing.getTaskStatus())) {
            return error("仅进行中的任务可一键完成");
        }
        return toAjax(getRecordService().completeAllByTaskId(taskId));
    }

    /**
     * 获取FireMaintenanceRecordService
     */
    private com.ruoyi.fire.service.IFireMaintenanceRecordService getRecordService() {
        return com.ruoyi.common.utils.spring.SpringUtils
                .getBean(com.ruoyi.fire.service.IFireMaintenanceRecordService.class);
    }

    /**
     * 转换字符串为Long数组
     */
    /**
     * 获取一级模板列表（系统列表）
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/templates/level1")
    @ResponseBody
    public AjaxResult getLevel1Templates() {
        List<FireMaintenanceTemplate> templates = fireMaintenanceTaskService.getAllTemplatesWithCache();
        List<FireMaintenanceTemplate> level1Templates = new ArrayList<>();
        for (FireMaintenanceTemplate template : templates) {
            // 只返回常规维保的一级模板（template_type = '0' 或 null）
            if (template.getLevel() == 1
                    && (template.getTemplateType() == null || "0".equals(template.getTemplateType()))) {
                level1Templates.add(template);
            }
        }
        return AjaxResult.success(level1Templates);
    }

    /**
     * 获取消防设施测试的一级模板列表
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/templates/firetest/level1")
    @ResponseBody
    public AjaxResult getFireTestLevel1Templates() {
        List<FireMaintenanceTemplate> templates = fireMaintenanceTaskService.getAllTemplatesWithCache();
        List<FireMaintenanceTemplate> fireTestLevel1Templates = new ArrayList<>();
        for (FireMaintenanceTemplate template : templates) {
            // 只返回消防设施测试的一级模板（template_type = '1'）
            if (template.getLevel() == 1 && "1".equals(template.getTemplateType())) {
                fireTestLevel1Templates.add(template);
            }
        }
        return AjaxResult.success(fireTestLevel1Templates);
    }

    /**
     * 编辑选择器使用的完整三级模板。前端只提交一级类目ID，二、三级用于明确展示选择范围。
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/templates/tree")
    @ResponseBody
    public AjaxResult getTemplateTree(@RequestParam("templateType") String templateType) {
        if (!"0".equals(templateType) && !"1".equals(templateType)) {
            return error("模板类型无效");
        }
        List<FireMaintenanceTemplate> result = new ArrayList<>();
        for (FireMaintenanceTemplate template : fireMaintenanceTaskService.getAllTemplatesWithCache()) {
            String actualType = template.getTemplateType() == null ? "0" : template.getTemplateType();
            if (templateType.equals(actualType)) {
                result.add(template);
            }
        }
        return AjaxResult.success(result);
    }

    private void sanitizeDashboardFilter(FireMaintenanceTask task) {
        if (task == null || task.getParams() == null) {
            return;
        }
        Object raw = task.getParams().get("dashboardFilter");
        if (raw == null) {
            return;
        }
        String value = String.valueOf(raw).trim();
        if (!"today".equals(value) && !"overdue".equals(value)) {
            task.getParams().remove("dashboardFilter");
        }
    }

    private Long[] convertStrToLongArray(String ids) {
        if (ids == null || ids.isEmpty()) {
            return new Long[0];
        }
        String[] strIds = ids.split(",");
        Long[] longIds = new Long[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            longIds[i] = Long.parseLong(strIds[i].trim());
        }
        return longIds;
    }

    /**
     * 签到列表页面
     */
    @RequiresPermissions("fire:task:view")
    @GetMapping("/checkInList/{taskId}")
    public String checkInList(@PathVariable("taskId") Long taskId, org.springframework.ui.ModelMap mmap) {
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        mmap.put("task", task);
        return prefix + "/checkInList";
    }
}
