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
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.domain.FireMaintenanceTemplate;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
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
        // 非超级管理员：自动注入当前用户 ID，SQL 中 managerId 会同时匹配
        // manager_id（项目负责人）、executor_id（执行人）、FIND_IN_SET(operator_ids)（操作员）
        if (!ShiroUtils.getSysUser().isAdmin()) {
            fireMaintenanceTask.setManagerId(ShiroUtils.getUserId());
        }
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
        return success(fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId));
    }

    /**
     * 新增维保任务
     */
    @RequiresPermissions("fire:task:add")
    @Log(title = "维保任务", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(FireMaintenanceTask fireMaintenanceTask) {
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
        try {
            if (fireMaintenanceTask.getTaskId() == null) {
                return error("任务ID不能为空");
            }
            FireMaintenanceTask existing = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(fireMaintenanceTask.getTaskId());
            if (existing == null) {
                return error("维保任务不存在");
            }
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
            // 仅保存选择配置，不触发生成/删除检查记录
            fireMaintenanceTask.setUpdateBy(ShiroUtils.getLoginName());
            fireMaintenanceTask.setUpdateTime(new Date());
            return toAjax(fireMaintenanceTaskService.updateFireMaintenanceTask(fireMaintenanceTask));
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
        return toAjax(fireMaintenanceTaskService.deleteFireMaintenanceTaskByTaskIds(convertStrToLongArray(ids)));
    }

    /**
     * 维保简报编辑页面
     */
    @RequiresPermissions("fire:task:edit")
    @GetMapping("/briefing/{taskId}")
    public String briefing(@PathVariable("taskId") Long taskId, org.springframework.ui.ModelMap mmap) {
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        mmap.put("task", task);
        return prefix + "/briefing";
    }

    /**
     * 保存维保简报
     */
    @RequiresPermissions("fire:task:edit")
    @Log(title = "维保简报", businessType = BusinessType.UPDATE)
    @PostMapping("/saveBriefing")
    @ResponseBody
    public AjaxResult saveBriefing(FireMaintenanceTask fireMaintenanceTask) {
        return toAjax(fireMaintenanceTaskService.updateFireMaintenanceTask(fireMaintenanceTask));
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
     */
    @PostMapping("/updateCheckDetail")
    @ResponseBody
    public AjaxResult updateCheckDetail(Long recordId, String checkResult, String faultDescription,
            String otherNotes, String faultImages) {
        com.ruoyi.fire.domain.FireMaintenanceRecord record = new com.ruoyi.fire.domain.FireMaintenanceRecord();
        record.setRecordId(recordId);
        record.setCheckResult(checkResult);
        record.setFaultDescription(faultDescription);
        record.setOtherNotes(otherNotes);
        record.setFaultImages(faultImages);

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
        FireMaintenanceTask task = new FireMaintenanceTask();
        task.setTaskId(taskId);
        task.setTaskStatus("1"); // 设置为进行中
        return toAjax(fireMaintenanceTaskService.updateFireMaintenanceTask(task));
    }

    /**
     * 一键完成任务（所有检查项标记为正常）
     */
    @RequiresPermissions("fire:task:edit")
    @Log(title = "一键完成维保任务", businessType = BusinessType.UPDATE)
    @PostMapping("/completeAll/{taskId}")
    @ResponseBody
    public AjaxResult completeAll(@PathVariable("taskId") Long taskId) {
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
