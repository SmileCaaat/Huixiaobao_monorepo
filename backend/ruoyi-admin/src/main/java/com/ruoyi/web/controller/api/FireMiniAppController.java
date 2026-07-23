package com.ruoyi.web.controller.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.UrgencyLevel;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.*;
import com.ruoyi.fire.service.*;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.fire.enums.FireEquipmentCategory;
import com.ruoyi.system.domain.FireReportRecord;
import com.ruoyi.system.service.IFireReportRecordService;

/**
 * 小程序端API控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/fire")
public class FireMiniAppController extends BaseController {

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireBuildingService buildingService;

    @Autowired
    private IFireEquipmentService equipmentService;

    @Autowired
    private IFireCheckInService checkInService;

    @Autowired
    private IFireMaintenanceTaskService taskService;

    @Autowired
    private IFireMaintenanceRecordService recordService;

    @Autowired
    private IFireInspectionService inspectionService;

    @Autowired
    private IFireSystemTypeService systemTypeService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private IFireFaultRepairService faultRepairService;

    @Autowired
    private IFireReportRecordService fireReportRecordService;

    // ==================== 公司相关接口 ====================

    /**
     * 获取当前用户负责的公司列表（通过任务推导：作为负责人或操作员参与过的任务所关联的公司）
     */
    @GetMapping("/company/myList")
    public AjaxResult myCompanyList() {
        Long userId = ShiroUtils.getUserId();
        List<FireCompany> list = taskService.selectCompanyListByTaskUserId(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取公司详情
     */
    @GetMapping("/company/detail/{companyId}")
    public AjaxResult companyDetail(@PathVariable("companyId") Long companyId) {
        FireCompany company = companyService.selectFireCompanyById(companyId);
        return AjaxResult.success(company);
    }

    /**
     * 切换当前公司（保存到用户会话）
     */
    @PostMapping("/company/switch")
    public AjaxResult switchCompany(@RequestBody Map<String, Object> params) {
        Long companyId = Long.parseLong(params.get("companyId").toString());
        Long userId = ShiroUtils.getUserId();

        // 验证用户是否有权限访问该公司（通过任务推导）
        List<FireCompany> userCompanies = taskService.selectCompanyListByTaskUserId(userId);
        boolean hasPermission = userCompanies.stream()
                .anyMatch(c -> c.getCompanyId().equals(companyId));

        if (!hasPermission) {
            return AjaxResult.error("您没有权限访问该公司");
        }

        // 保存到Session
        ShiroUtils.getSession().setAttribute("currentCompanyId", companyId);

        FireCompany company = companyService.selectFireCompanyById(companyId);
        Map<String, Object> result = new HashMap<>();
        result.put("companyId", companyId);
        result.put("companyName", company != null ? company.getCompanyName() : "");

        return AjaxResult.success("切换成功", result);
    }

    /**
     * 获取当前选中的公司
     */
    @GetMapping("/company/current")
    public AjaxResult getCurrentCompany() {
        Object companyIdObj = ShiroUtils.getSession().getAttribute("currentCompanyId");

        if (companyIdObj == null) {
            // 如果没有选中公司，通过任务推导取第一个关联公司
            Long userId = ShiroUtils.getUserId();
            List<FireCompany> userCompanies = taskService.selectCompanyListByTaskUserId(userId);
            if (userCompanies != null && !userCompanies.isEmpty()) {
                FireCompany firstCompany = userCompanies.get(0);
                // 自动设置为当前公司
                ShiroUtils.getSession().setAttribute("currentCompanyId", firstCompany.getCompanyId());
                return AjaxResult.success(firstCompany);
            }
            return AjaxResult.error("未找到关联的公司");
        }

        Long companyId = Long.parseLong(companyIdObj.toString());
        FireCompany company = companyService.selectFireCompanyById(companyId);

        if (company == null) {
            return AjaxResult.error("公司不存在");
        }

        return AjaxResult.success(company);
    }

    // ==================== 签到相关接口 ====================

    /**
     * 新增签到（打卡）
     */
    @PostMapping("/checkIn/add")
    public AjaxResult addCheckIn(@RequestBody FireCheckIn checkIn) {
        try {
            // 不信任请求体 userId/userName；强制会话用户并回查账号姓名
            checkIn.setUserId(null);
            checkIn.setUserName(null);
            checkInService.prepareMobileInsert(checkIn, true);

            FireCompany company = companyService.selectFireCompanyById(checkIn.getCompanyId());
            if (company != null && company.getCheckInLongitude() != null && company.getCheckInLatitude() != null
                    && checkIn.getLatitude() != null && checkIn.getLongitude() != null) {
                double distance = calculateDistance(
                        checkIn.getLatitude(), checkIn.getLongitude(),
                        company.getCheckInLatitude(), company.getCheckInLongitude());
                Integer radius = company.getCheckInRadius() != null ? company.getCheckInRadius() : 500;

                Map<String, Object> result = new HashMap<>();
                result.put("isInRange", distance <= radius);
                result.put("distance", distance);

                int rows = checkInService.insertFireCheckIn(checkIn);
                if (rows > 0) {
                    result.put("checkInId", checkIn.getCheckInId());
                    result.put("checkInTime", checkIn.getCheckInTime());
                    return AjaxResult.success("签到成功", result);
                }
                return AjaxResult.error("签到失败");
            }

            int rows = checkInService.insertFireCheckIn(checkIn);
            return toAjax(rows);
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取签到列表
     */
    @PostMapping("/checkIn/list")
    public TableDataInfo checkInList(@RequestBody FireCheckIn checkIn) {
        startPage();
        List<FireCheckIn> list = checkInService.selectFireCheckInList(checkIn);
        return getDataTable(list);
    }

    /**
     * 获取签到详情
     */
    @GetMapping("/checkIn/detail/{checkInId}")
    public AjaxResult checkInDetail(@PathVariable("checkInId") Long checkInId) {
        FireCheckIn checkIn = checkInService.selectFireCheckInById(checkInId);
        if (checkIn == null) {
            return AjaxResult.error("签到记录不存在");
        }
        AjaxResult ajax = AjaxResult.success(checkIn);
        Map<String, FireCheckIn> pair = checkInService.resolvePairRecords(checkIn);
        ajax.put("checkInRecord", pair.get("checkInRecord"));
        ajax.put("checkOutRecord", pair.get("checkOutRecord"));
        ajax.put("historyUnlinkedTask", checkIn.getTaskId() == null);
        return ajax;
    }

    /**
     * 校验打卡位置
     */
    @PostMapping("/checkIn/validateLocation")
    public AjaxResult validateLocation(@RequestBody Map<String, Object> params) {
        Long companyId = Long.parseLong(params.get("companyId").toString());
        Double longitude = Double.parseDouble(params.get("longitude").toString());
        Double latitude = Double.parseDouble(params.get("latitude").toString());

        FireCompany company = companyService.selectFireCompanyById(companyId);
        if (company == null || company.getCheckInLongitude() == null || company.getCheckInLatitude() == null) {
            return AjaxResult.error("公司未设置打卡区域");
        }

        double distance = calculateDistance(latitude, longitude, company.getCheckInLatitude(),
                company.getCheckInLongitude());
        Integer radius = company.getCheckInRadius() != null ? company.getCheckInRadius() : 500;

        Map<String, Object> result = new HashMap<>();
        result.put("isInRange", distance <= radius);
        result.put("distance", Math.round(distance * 100.0) / 100.0);
        result.put("allowedRadius", radius);

        return AjaxResult.success(result);
    }

    // ==================== 建筑相关接口 ====================

    /**
     * 获取公司关联签到任务列表
     */
    @PostMapping("/checkIn/listTasksByCompany")
    public AjaxResult listTasksByCompany(@RequestBody Map<String, Object> params) {
        Long companyId = getLongValue(params, "companyId");
        if (companyId == null) {
            return AjaxResult.success(new java.util.ArrayList<FireMaintenanceTask>());
        }

        Long userId = ShiroUtils.getUserId();
        if (!isSysAdmin() && !hasCompanyAccess(companyId, userId)) {
            return AjaxResult.error("无权访问该公司任务");
        }

        FireMaintenanceTask query = new FireMaintenanceTask();
        query.setCompanyId(companyId);
        if (!isSysAdmin()) {
            // 非管理员仅返回当前用户作为负责人/执行人/操作员的任务
            query.setManagerId(userId);
        }
        List<FireMaintenanceTask> taskList = taskService.selectFireMaintenanceTaskList(query);
        return AjaxResult.success(taskList);
    }

    /**
     * 获取报告列表
     */
    @PostMapping("/report/list")
    public TableDataInfo reportList(@RequestBody FireReportRecord fireReportRecord) {
        startPage();
        List<FireReportRecord> list = fireReportRecordService.selectFireReportRecordList(fireReportRecord);
        return getDataTable(list);
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/report/detail/{reportId}")
    public AjaxResult reportDetail(@PathVariable("reportId") Long reportId) {
        FireReportRecord record = fireReportRecordService.selectFireReportRecordById(reportId);
        if (record == null) {
            return AjaxResult.error("报告不存在");
        }
        return AjaxResult.success(record);
    }

    /**
     * 下载报告
     */
    @GetMapping("/report/download/{reportId}")
    public void downloadReport(@PathVariable("reportId") Long reportId, HttpServletResponse response) {
        writeReportFile(reportId, response, true);
    }

    /**
     * 预览报告
     */
    @GetMapping("/report/preview/{reportId}")
    public void previewReport(@PathVariable("reportId") Long reportId, HttpServletResponse response) {
        writeReportFile(reportId, response, false);
    }

    @PostMapping("/building/list")
    public TableDataInfo buildingList(@RequestBody FireBuilding building) {
        startPage();
        List<FireBuilding> list = buildingService.selectBuildingList(building);
        return getDataTable(list);
    }

    /**
     * 获取建筑详情
     */
    @GetMapping("/building/detail/{buildingId}")
    public AjaxResult buildingDetail(@PathVariable("buildingId") Long buildingId) {
        FireBuilding building = buildingService.selectBuildingById(buildingId);
        return AjaxResult.success(building);
    }

    /**
     * 新增建筑
     */
    @PostMapping("/building/add")
    public AjaxResult addBuilding(@RequestBody FireBuilding building) {
        building.setCreateBy(ShiroUtils.getLoginName());
        int rows = buildingService.insertBuilding(building);
        if (rows > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("buildingId", building.getBuildingId());
            return AjaxResult.success("新增成功", result);
        }
        return AjaxResult.error("新增失败");
    }

    /**
     * 修改建筑
     */
    @PostMapping("/building/edit")
    public AjaxResult editBuilding(@RequestBody FireBuilding building) {
        building.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(buildingService.updateBuilding(building));
    }

    // ==================== 设备相关接口 ====================

    /**
     * 获取设备列表
     * 支持按公司、建筑、项目类别、设备名称、设备编码、设备状态筛选
     */
    @PostMapping("/equipment/list")
    public TableDataInfo equipmentList(@RequestBody FireEquipment equipment) {
        startPage();
        List<FireEquipment> list = equipmentService.selectEquipmentList(equipment);
        return getDataTable(list);
    }

    /**
     * 获取设备详情
     * 返回设备完整信息，包括：
     * - 基本信息：设备名称、编码、所在建筑、楼层、位置
     * - 分类信息：系统名称、项目类别
     * - 设备参数：生产厂家、规格型号、数量、图片
     * - 日期信息：生产日期、安装日期、有效日期、上次检查日期、下次检查日期
     * - 状态信息：设备状态(normal/warning/fault/expired)、使用状态(0启用/1停用)
     */
    @GetMapping("/equipment/detail/{equipmentId}")
    public AjaxResult equipmentDetail(@PathVariable("equipmentId") Long equipmentId) {
        FireEquipment equipment = equipmentService.selectEquipmentById(equipmentId);
        return AjaxResult.success(equipment);
    }

    /**
     * 新增设备
     * 必填字段：equipmentName（设备名称）、location（具体位置）
     * 可选字段：buildingId、floorNo、systemName、projectCategory、manufacturer、
     * expireDate、quantity、model、image、remark
     */
    @PostMapping("/equipment/add")
    public AjaxResult addEquipment(@RequestBody FireEquipment equipment) {
        equipment.setCreateBy(ShiroUtils.getLoginName());
        int rows = equipmentService.insertEquipment(equipment);
        if (rows > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("equipmentId", equipment.getEquipmentId());
            return AjaxResult.success("新增成功", result);
        }
        return AjaxResult.error("新增失败");
    }

    /**
     * 修改设备
     * 可修改字段：buildingId、floorNo、systemName、projectCategory、equipmentName、
     * manufacturer、expireDate、quantity、location、model、image、
     * equipmentStatus、remark
     */
    @PostMapping("/equipment/edit")
    public AjaxResult editEquipment(@RequestBody FireEquipment equipment) {
        equipment.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(equipmentService.updateEquipment(equipment));
    }

    /**
     * 扫码获取设备信息
     * 通过设备编码（equipmentCode）查询设备详情
     * 用于小程序扫描设备二维码后快速获取设备信息
     */
    @GetMapping("/equipment/scan/{equipmentCode}")
    public AjaxResult scanEquipment(@PathVariable("equipmentCode") String equipmentCode) {
        FireEquipment query = new FireEquipment();
        query.setEquipmentCode(equipmentCode);
        List<FireEquipment> list = equipmentService.selectEquipmentList(query);
        if (list != null && !list.isEmpty()) {
            return AjaxResult.success(list.get(0));
        }
        return AjaxResult.error("未找到该设备");
    }

    // ==================== 维保任务相关接口（新系统-三级分类） ====================

    /**
     * 获取我的任务列表
     * 始终以当前登录用户为准，只返回该用户作为「项目负责人 / 执行人 / 维保操作员」的任务。
     * 不接受客户端传入 userId，防止越权查看他人任务。
     */
    @PostMapping("/task/myList")
    @SuppressWarnings("unchecked")
    public TableDataInfo myTaskList(@RequestBody Map<String, Object> params) {
        FireMaintenanceTask query = new FireMaintenanceTask();

        // 始终使用服务端 Session 中的当前用户 ID，禁止客户端覆盖
        // SQL 中 managerId 会同时匹配 manager_id / executor_id / FIND_IN_SET(operator_ids)
        query.setManagerId(ShiroUtils.getUserId());

        // 根据公司ID查询
        Long companyId = getLongValue(params, "companyId");
        if (companyId != null) {
            query.setCompanyId(companyId);
        }

        // 根据任务状态查询
        String taskStatus = getStringValue(params, "taskStatus");
        if (StringUtils.isNotEmpty(taskStatus)) {
            query.setTaskStatus(taskStatus);
        }

        // 根据任务类型查询
        String taskType = getStringValue(params, "taskType");
        if (StringUtils.isNotEmpty(taskType)) {
            query.setTaskType(taskType);
        }

        Object queryParams = params.get("params");
        if (queryParams instanceof Map) {
            query.setParams((Map<String, Object>) queryParams);
        }

        Integer pageNum = getIntValue(params, "pageNum");
        Integer pageSize = getIntValue(params, "pageSize");
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        } else {
            startPage();
        }
        List<FireMaintenanceTask> list = taskService.selectFireMaintenanceTaskList(query);
        return getDataTable(list);
    }

    /**
     * 获取任务详情（一级页面：系统列表）
     * 校验当前用户是否为该任务的相关人员（负责人/执行人/操作员）。
     */
    @GetMapping("/task/detail/{taskId}")
    public AjaxResult taskDetail(@PathVariable("taskId") Long taskId) {
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(taskId);
        if (task == null) {
            return AjaxResult.error("任务不存在");
        }
        if (!isTaskRelated(task, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权查看该任务");
        }
        return AjaxResult.success(task);
    }

    /**
     * 获取系统详情（二级页面：设备列表）
     * 校验当前用户是否为该任务的相关人员。
     */
    @GetMapping("/task/system/{recordId}")
    public AjaxResult getSystemDetail(@PathVariable("recordId") Long recordId) {
        FireMaintenanceRecord systemRecord = recordService.selectFireMaintenanceRecordByRecordId(recordId);
        if (systemRecord == null) {
            return AjaxResult.error("记录不存在");
        }
        // 通过所属任务做权限校验
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(systemRecord.getTaskId());
        if (task == null || !isTaskRelated(task, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权查看该任务");
        }

        // 查询该系统下的二级记录（设备列表）
        List<FireMaintenanceRecord> equipments = recordService.selectLevel2List(systemRecord.getTaskId(), recordId);

        // 为每个设备计算统计数据
        for (FireMaintenanceRecord equipment : equipments) {
            calculateEquipmentStats(equipment);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("system", systemRecord);
        result.put("equipments", equipments);
        return AjaxResult.success(result);
    }

    /**
     * 获取设备详情（三级页面：检查项列表）
     * 校验当前用户是否为该任务的相关人员。
     */
    @GetMapping("/task/equipment/{recordId}")
    public AjaxResult getEquipmentDetail(@PathVariable("recordId") Long recordId) {
        FireMaintenanceRecord equipmentRecord = recordService.selectFireMaintenanceRecordByRecordId(recordId);
        if (equipmentRecord == null) {
            return AjaxResult.error("记录不存在");
        }
        // 通过所属任务做权限校验
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(equipmentRecord.getTaskId());
        if (task == null || !isTaskRelated(task, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权查看该任务");
        }

        // 查询该设备下的三级记录（检查项列表）
        List<FireMaintenanceRecord> checkItems = recordService.selectLevel3List(equipmentRecord.getTaskId(), recordId);

        Map<String, Object> result = new HashMap<>();
        result.put("equipment", equipmentRecord);
        result.put("checkItems", checkItems);
        return AjaxResult.success(result);
    }

    /**
     * 判断用户是否为任务的相关人员（项目负责人 / 执行人 / 维保操作员）
     */
    private boolean isTaskRelated(FireMaintenanceTask task, Long userId) {
        if (userId == null || task == null) {
            return false;
        }
        // 项目负责人
        if (userId.equals(task.getManagerId())) {
            return true;
        }
        // 执行人
        if (userId.equals(task.getExecutorId())) {
            return true;
        }
        // 维保操作员（逗号分隔的 ID 字符串，如 "1,2,3"）
        String operatorIds = task.getOperatorIds();
        if (StringUtils.isNotEmpty(operatorIds)) {
            for (String id : operatorIds.split(",")) {
                try {
                    if (userId.equals(Long.parseLong(id.trim()))) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return false;
    }

    /**
     * 更新检查结果（快速操作）
     * 校验当前用户是否为该任务相关人员。
     */
    @PostMapping("/task/updateCheckResult")
    public AjaxResult updateCheckResult(@RequestBody FireMaintenanceRecord record) {
        if (record.getTaskId() == null) {
            return AjaxResult.error("任务ID不能为空");
        }
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(record.getTaskId());
        if (task == null || !isTaskRelated(task, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权操作该任务");
        }
        record.setCheckTime(new Date());
        record.setCheckerId(ShiroUtils.getUserId());
        record.setCheckerName(ShiroUtils.getLoginName());

        // 如果不是故障，清空故障描述
        if (!"2".equals(record.getCheckResult())) {
            record.setFaultDescription("");
            record.setFaultImages("");
        }

        int result = recordService.updateCheckResult(record);
        return toAjax(result);
    }

    /**
     * 更新故障描述
     * 校验当前用户是否为该任务相关人员。
     */
    @PostMapping("/task/updateFaultDesc")
    public AjaxResult updateFaultDesc(@RequestBody FireMaintenanceRecord record) {
        if (record.getTaskId() == null) {
            return AjaxResult.error("任务ID不能为空");
        }
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(record.getTaskId());
        if (task == null || !isTaskRelated(task, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权操作该任务");
        }
        int result = recordService.updateCheckResult(record);
        return toAjax(result);
    }

    /**
     * 更新检查详情（完整信息）
     * 校验当前用户是否为该任务相关人员。
     */
    @PostMapping("/task/updateCheckDetail")
    public AjaxResult updateCheckDetail(@RequestBody FireMaintenanceRecord record) {
        if (record.getTaskId() == null) {
            return AjaxResult.error("任务ID不能为空");
        }
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(record.getTaskId());
        if (task == null || !isTaskRelated(task, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权操作该任务");
        }
        record.setCheckTime(new Date());
        record.setCheckerId(ShiroUtils.getUserId());
        record.setCheckerName(ShiroUtils.getLoginName());

        int result = recordService.updateCheckResult(record);
        return toAjax(result);
    }

    /**
     * 计算设备的统计数据
     */
    private void calculateEquipmentStats(FireMaintenanceRecord equipment) {
        // 查询该设备下的所有三级检查项
        List<FireMaintenanceRecord> checkItems = recordService.selectLevel3List(equipment.getTaskId(),
                equipment.getRecordId());

        int totalItems = checkItems.size();
        int completedItems = 0;
        int uncompletedItems = 0;

        for (FireMaintenanceRecord item : checkItems) {
            if (!"0".equals(item.getCheckResult())) {
                completedItems++;
            } else {
                uncompletedItems++;
            }
        }

        equipment.setTotalItems(totalItems);
        equipment.setCompletedItems(completedItems);
        equipment.setUncompletedItems(uncompletedItems);
        equipment.setSystemStatus(uncompletedItems == 0 && totalItems > 0 ? "1" : "0");
    }

    // ==================== 工具方法 ====================

    /**
     * 计算两点之间的距离（米）
     * 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ==================== 巡检登记相关接口 ====================

    /**
     * 获取巡检登记列表
     */
    @PostMapping("/inspection/list")
    public TableDataInfo inspectionList(@RequestBody FireInspection inspection) {
        startPage();
        List<FireInspection> list = inspectionService.selectFireInspectionList(inspection);
        return getDataTable(list);
    }

    /**
     * 获取当前用户的巡检列表
     */
    @PostMapping("/inspection/myList")
    public TableDataInfo myInspectionList(@RequestBody FireInspection inspection) {
        inspection.setInspectorId(ShiroUtils.getUserId());
        startPage();
        List<FireInspection> list = inspectionService.selectFireInspectionList(inspection);
        return getDataTable(list);
    }

    /**
     * 获取巡检详情
     */
    @GetMapping("/inspection/detail/{inspectionId}")
    public AjaxResult inspectionDetail(@PathVariable("inspectionId") Long inspectionId) {
        FireInspection inspection = inspectionService.selectFireInspectionById(inspectionId);
        return AjaxResult.success(inspection);
    }

    /**
     * 新增巡检登记
     */
    @PostMapping("/inspection/add")
    public AjaxResult addInspection(@RequestBody FireInspection inspection) {
        inspection.setInspectorId(ShiroUtils.getUserId());
        inspection.setInspectorName(ShiroUtils.getLoginName());
        if (inspection.getInspectionTime() == null) {
            inspection.setInspectionTime(new Date());
        }
        inspection.setCreateBy(ShiroUtils.getLoginName());

        int rows = inspectionService.insertFireInspection(inspection);
        if (rows > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("inspectionId", inspection.getInspectionId());
            return AjaxResult.success("保存成功", result);
        }
        return AjaxResult.error("保存失败");
    }

    /**
     * 修改巡检登记
     */
    @PostMapping("/inspection/edit")
    public AjaxResult editInspection(@RequestBody FireInspection inspection) {
        inspection.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(inspectionService.updateFireInspection(inspection));
    }

    /**
     * 删除巡检登记
     */
    @PostMapping("/inspection/delete/{inspectionId}")
    public AjaxResult deleteInspection(@PathVariable("inspectionId") Long inspectionId) {
        return toAjax(inspectionService.deleteFireInspectionById(inspectionId));
    }

    /**
     * 获取系统类型字典
     */
    @GetMapping("/dict/systemTypes")
    public AjaxResult getSystemTypes() {
        // 从数据库获取系统类型列表
        List<FireSystemType> systemTypes = systemTypeService.selectFireSystemTypeAll();

        java.util.List<Map<String, String>> list = new java.util.ArrayList<>();
        for (FireSystemType type : systemTypes) {
            Map<String, String> item = new HashMap<>();
            item.put("value", type.getTypeCode());
            item.put("label", type.getTypeName());
            list.add(item);
        }
        return AjaxResult.success(list);
    }

    /**
     * 获取维保类型字典
     */
    @GetMapping("/dict/inspectionTypes")
    public AjaxResult getInspectionTypes() {
        java.util.List<Map<String, String>> list = new java.util.ArrayList<>();

        Map<String, String> item1 = new HashMap<>();
        item1.put("value", "0");
        item1.put("label", "测试");
        list.add(item1);

        Map<String, String> item2 = new HashMap<>();
        item2.put("value", "1");
        item2.put("label", "巡查");
        list.add(item2);

        Map<String, String> item3 = new HashMap<>();
        item3.put("value", "2");
        item3.put("label", "保养");
        list.add(item3);

        return AjaxResult.success(list);
    }

    /**
     * 获取设备消防设施分类名称列表（字典）
     */
    @GetMapping("/dict/equipmentCategories")
    public AjaxResult getEquipmentCategories() {
        return AjaxResult.success(FireEquipmentCategory.allLabels());
    }

    /**
     * 根据公司ID获取建筑列表
     */
    @GetMapping("/building/byCompany/{companyId}")
    public AjaxResult getBuildingsByCompany(@PathVariable("companyId") Long companyId) {
        FireBuilding query = new FireBuilding();
        query.setCompanyId(companyId);
        List<FireBuilding> list = buildingService.selectBuildingList(query);
        return AjaxResult.success(list);
    }

    // ==================== 故障报修相关接口 ====================

    /**
     * 获取故障报修列表
     */
    @PostMapping("/repair/list")
    public TableDataInfo repairList(@RequestBody FireFaultRepair repair) {
        Long userId = ShiroUtils.getUserId();
        applyCurrentCompanyScope(repair, userId);
        if (!isSysAdmin() && !isCompanyRepairAdmin(repair.getCompanyId(), userId)) {
            applyRelatedRepairScope(repair, userId);
        }
        startPage();
        List<FireFaultRepair> list = faultRepairService.selectFireFaultRepairList(repair);
        return getDataTable(list);
    }

    /**
     * 获取当前用户的报修列表
     */
    @PostMapping("/repair/myList")
    public TableDataInfo myRepairList(@RequestBody FireFaultRepair repair) {
        Long userId = ShiroUtils.getUserId();
        applyCurrentCompanyScope(repair, userId);
        applyRelatedRepairScope(repair, userId);
        startPage();
        List<FireFaultRepair> list = faultRepairService.selectFireFaultRepairList(repair);
        return getDataTable(list);
    }

    /**
     * 我上报的报修。
     */
    @PostMapping("/repair/myReportedList")
    public TableDataInfo myReportedRepairList(@RequestBody FireFaultRepair repair) {
        Long userId = ShiroUtils.getUserId();
        applyCurrentCompanyScope(repair, userId);
        repair.setReporterId(userId);
        startPage();
        List<FireFaultRepair> list = faultRepairService.selectFireFaultRepairList(repair);
        return getDataTable(list);
    }

    /**
     * 派发给我的报修。
     */
    @PostMapping("/repair/myAssignedList")
    public TableDataInfo myAssignedRepairList(@RequestBody FireFaultRepair repair) {
        Long userId = ShiroUtils.getUserId();
        applyCurrentCompanyScope(repair, userId);
        repair.setRepairUserId(userId);
        startPage();
        List<FireFaultRepair> list = faultRepairService.selectFireFaultRepairList(repair);
        return getDataTable(list);
    }

    /**
     * 获取故障报修详情。
     */
    @GetMapping("/repair/detail/{repairId}")
    public AjaxResult repairDetail(@PathVariable("repairId") Long repairId) {
        Long userId = ShiroUtils.getUserId();
        FireFaultRepair repair = faultRepairService.selectFireFaultRepairById(repairId);
        if (repair == null) {
            return AjaxResult.error("报修记录不存在");
        }
        if (!canViewRepair(repair, userId)) {
            return AjaxResult.error("您无权查看该报修记录");
        }
        return AjaxResult.success(repair);
    }

    /**
     * 小程序主动上报故障。
     */
    @PostMapping("/repair/add")
    public AjaxResult addRepair(@RequestBody FireFaultRepair repair) {
        Long userId = ShiroUtils.getUserId();
        if (repair.getCompanyId() == null) {
            return AjaxResult.error("请选择报修单位");
        }
        if (!hasCompanyAccess(repair.getCompanyId(), userId)) {
            return AjaxResult.error("您无权在该单位发起报修");
        }

        String validateMessage = fillRepairLookupInfo(repair);
        if (validateMessage != null) {
            return AjaxResult.error(validateMessage);
        }
        if (StringUtils.isEmpty(repair.getUrgencyLevel()) || !UrgencyLevel.isValid(repair.getUrgencyLevel())) {
            return AjaxResult.error("紧急程度参数无效");
        }

        repair.setReporterId(userId);
        repair.setReporterName(ShiroUtils.getSysUser().getUserName());
        repair.setReporterPhone(ShiroUtils.getSysUser().getPhonenumber());
        repair.setRepairUserId(null);
        repair.setRepairPerson(null);
        repair.setRepairPhone(null);
        repair.setDispatchBy(null);
        repair.setDispatchTime(null);
        repair.setAcceptTime(null);
        repair.setStartTime(null);
        repair.setCompleteTime(null);
        repair.setRepairStatus("0");
        repair.setCreateBy(ShiroUtils.getLoginName());
        if (StringUtils.isEmpty(repair.getIsReported())) {
            repair.setIsReported("1");
        }
        if (repair.getFoundTime() == null) {
            repair.setFoundTime(new Date());
        }

        int rows = faultRepairService.insertFireFaultRepair(repair);
        if (rows > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("repairId", repair.getRepairId());
            result.put("repairNo", repair.getRepairNo());
            return AjaxResult.success("报修提交成功", result);
        }
        return AjaxResult.error("报修提交失败");
    }

    /**
     * 修改故障报修。
     * 待处理状态下，报修人和管理员都可以调整内容。
     */
    @PostMapping("/repair/edit")
    public AjaxResult editRepair(@RequestBody FireFaultRepair repair) {
        if (repair.getRepairId() == null) {
            return AjaxResult.error("报修ID不能为空");
        }

        Long userId = ShiroUtils.getUserId();
        FireFaultRepair existingRepair = faultRepairService.selectFireFaultRepairById(repair.getRepairId());
        if (existingRepair == null) {
            return AjaxResult.error("报修记录不存在");
        }
        if (!canEditRepair(existingRepair, userId)) {
            return AjaxResult.error("只有报修人或管理员可以修改待处理报修");
        }

        FireFaultRepair update = buildEditableRepair(repair, existingRepair);
        if (StringUtils.isEmpty(update.getUrgencyLevel()) || !UrgencyLevel.isValid(update.getUrgencyLevel())) {
            return AjaxResult.error("紧急程度参数无效");
        }
        if (!hasCompanyAccess(update.getCompanyId(), userId)) {
            return AjaxResult.error("您无权修改该单位的报修");
        }

        String validateMessage = fillRepairLookupInfo(update);
        if (validateMessage != null) {
            return AjaxResult.error(validateMessage);
        }

        update.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(faultRepairService.updateFireFaultRepair(update));
    }

    /**
     * 删除故障报修。
     */
    @PostMapping("/repair/delete/{repairId}")
    public AjaxResult deleteRepair(@PathVariable("repairId") Long repairId) {
        Long userId = ShiroUtils.getUserId();
        FireFaultRepair repair = faultRepairService.selectFireFaultRepairById(repairId);
        if (repair == null) {
            return AjaxResult.error("报修记录不存在");
        }
        if (!canEditRepair(repair, userId)) {
            return AjaxResult.error("只有报修人或管理员可以删除待处理报修");
        }
        return toAjax(faultRepairService.deleteFireFaultRepairById(repairId));
    }

    /**
     * 查询可派发处理人。
     */
    @GetMapping("/repair/dispatchUsers/{companyId}")
    public AjaxResult dispatchUsers(@PathVariable("companyId") Long companyId) {
        Long userId = ShiroUtils.getUserId();
        if (!isCompanyRepairAdmin(companyId, userId)) {
            return AjaxResult.error("只有管理员或项目负责人可以查看派发人员");
        }
        return AjaxResult.success(companyService.selectUserListByCompanyId(companyId));
    }

    /**
     * 管理员派发报修。
     */
    @PostMapping("/repair/dispatch")
    public AjaxResult dispatchRepair(@RequestBody Map<String, Object> params) {
        Long repairId = getLongValue(params, "repairId");
        Long repairUserId = getLongValue(params, "repairUserId");
        if (repairId == null || repairUserId == null) {
            return AjaxResult.error("repairId 和 repairUserId 不能为空");
        }

        Long userId = ShiroUtils.getUserId();
        FireFaultRepair repair = faultRepairService.selectFireFaultRepairById(repairId);
        if (repair == null) {
            return AjaxResult.error("报修记录不存在");
        }
        if (!isCompanyRepairAdmin(repair.getCompanyId(), userId)) {
            return AjaxResult.error("只有管理员或项目负责人可以派发报修");
        }

        return toAjax(faultRepairService.dispatchRepair(repairId, repairUserId, ShiroUtils.getLoginName()));
    }

    /**
     * 兼容旧的受理接口。
     * 如果传了 repairUserId，则按派发处理。
     */
    @PostMapping("/repair/accept")
    public AjaxResult acceptRepair(@RequestBody Map<String, Object> params) {
        Long repairId = getLongValue(params, "repairId");
        if (repairId == null) {
            return AjaxResult.error("repairId 不能为空");
        }

        FireFaultRepair repair = faultRepairService.selectFireFaultRepairById(repairId);
        if (repair == null) {
            return AjaxResult.error("报修记录不存在");
        }

        Long userId = ShiroUtils.getUserId();
        if (!isCompanyRepairAdmin(repair.getCompanyId(), userId)) {
            return AjaxResult.error("只有管理员或项目负责人可以受理报修");
        }

        Long repairUserId = getLongValue(params, "repairUserId");
        if (repairUserId != null) {
            return toAjax(faultRepairService.dispatchRepair(repairId, repairUserId, ShiroUtils.getLoginName()));
        }

        String repairPerson = getStringValue(params, "repairPerson");
        if (StringUtils.isEmpty(repairPerson)) {
            return AjaxResult.error("请填写处理人");
        }
        String repairPhone = getStringValue(params, "repairPhone");
        return toAjax(faultRepairService.acceptRepair(repairId, repairPerson, repairPhone));
    }

    /**
     * 完成报修。
     * 派发给我的处理单和管理员都可以完结。
     */
    @PostMapping("/repair/complete")
    public AjaxResult completeRepair(@RequestBody Map<String, Object> params) {
        Long repairId = getLongValue(params, "repairId");
        if (repairId == null) {
            return AjaxResult.error("repairId 不能为空");
        }

        Long userId = ShiroUtils.getUserId();
        FireFaultRepair repair = faultRepairService.selectFireFaultRepairById(repairId);
        if (repair == null) {
            return AjaxResult.error("报修记录不存在");
        }
        if (!canCompleteRepair(repair, userId)) {
            return AjaxResult.error("只有处理人或管理员可以完成报修");
        }

        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(repairId);
        update.setRepairDescription(getStringValue(params, "repairDescription"));
        update.setRepairImages(getStringValue(params, "repairImages"));
        update.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(faultRepairService.completeRepair(update));
    }

    /**
     * 获取报修统计信息。
     * 默认统计当前单位下与我相关的报修，并额外返回我发起/我被派发的总数。
     */
    @GetMapping("/repair/statistics")
    public AjaxResult repairStatistics() {
        Long userId = ShiroUtils.getUserId();

        FireFaultRepair relatedQuery = new FireFaultRepair();
        applyCurrentCompanyScope(relatedQuery, userId);
        applyRelatedRepairScope(relatedQuery, userId);
        List<FireFaultRepair> relatedRepairs = faultRepairService.selectFireFaultRepairList(relatedQuery);

        FireFaultRepair reportedQuery = new FireFaultRepair();
        applyCurrentCompanyScope(reportedQuery, userId);
        reportedQuery.setReporterId(userId);
        List<FireFaultRepair> reportedRepairs = faultRepairService.selectFireFaultRepairList(reportedQuery);

        FireFaultRepair assignedQuery = new FireFaultRepair();
        applyCurrentCompanyScope(assignedQuery, userId);
        assignedQuery.setRepairUserId(userId);
        List<FireFaultRepair> assignedRepairs = faultRepairService.selectFireFaultRepairList(assignedQuery);

        Map<String, Object> result = new HashMap<>();
        result.put("total", relatedRepairs.size());
        result.put("pending", countRepairsByStatus(relatedRepairs, "0"));
        result.put("processing", countRepairsByStatus(relatedRepairs, "1"));
        result.put("completed", countRepairsByStatus(relatedRepairs, "2"));
        result.put("reportedTotal", reportedRepairs.size());
        result.put("assignedTotal", assignedRepairs.size());

        return AjaxResult.success(result);
    }

    /**
     * 根据公司 ID 获取系统类型列表。
     */
    @GetMapping("/systemType/byCompany/{companyId}")
    public AjaxResult getSystemTypesByCompany(@PathVariable("companyId") Long companyId) {
        if (!hasCompanyAccess(companyId, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权查看该单位的系统类型");
        }
        List<FireSystemType> list = systemTypeService.selectFireSystemTypeAll();
        return AjaxResult.success(list);
    }

    /**
     * 根据公司 ID 和系统类型获取设备列表。
     */
    @GetMapping("/equipment/byCompanyAndSystem")
    public AjaxResult getEquipmentByCompanyAndSystem(
            @RequestParam("companyId") Long companyId,
            @RequestParam(value = "systemTypeId", required = false) Long systemTypeId) {
        if (!hasCompanyAccess(companyId, ShiroUtils.getUserId())) {
            return AjaxResult.error("您无权查看该单位的设备");
        }
        FireEquipment query = new FireEquipment();
        query.setCompanyId(companyId);
        if (systemTypeId != null) {
            query.setSystemTypeId(systemTypeId);
        }
        List<FireEquipment> list = equipmentService.selectEquipmentList(query);
        return AjaxResult.success(list);
    }

    private void applyRelatedRepairScope(FireFaultRepair repair, Long userId) {
        repair.getParams().put("relatedUserId", userId);
    }

    private void applyCurrentCompanyScope(FireFaultRepair repair, Long userId) {
        if (repair.getCompanyId() != null) {
            return;
        }
        Long currentCompanyId = getCurrentCompanyId();
        if (currentCompanyId != null && hasCompanyAccess(currentCompanyId, userId)) {
            repair.setCompanyId(currentCompanyId);
        }
    }

    private Long getCurrentCompanyId() {
        Object companyIdObj = ShiroUtils.getSession().getAttribute("currentCompanyId");
        if (companyIdObj == null) {
            return null;
        }
        try {
            return Long.parseLong(companyIdObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasCompanyAccess(Long companyId, Long userId) {
        return companyId != null;
    }

    private boolean isCompanyRepairAdmin(Long companyId, Long userId) {
        if (companyId == null) {
            return false;
        }
        if (isSysAdmin()) {
            return true;
        }
        List<FireUserCompany> companyUsers = companyService.selectUserListByCompanyId(companyId);
        if (companyUsers == null || companyUsers.isEmpty()) {
            return false;
        }
        return companyUsers.stream()
                .anyMatch(item -> userId.equals(item.getUserId())
                        && ("1".equals(item.getRoleType()) || "2".equals(item.getRoleType())));
    }

    private boolean isRepairReporter(FireFaultRepair repair, Long userId) {
        return repair.getReporterId() != null && repair.getReporterId().equals(userId);
    }

    private boolean isRepairAssignee(FireFaultRepair repair, Long userId) {
        return repair.getRepairUserId() != null && repair.getRepairUserId().equals(userId);
    }

    private boolean canViewRepair(FireFaultRepair repair, Long userId) {
        return isSysAdmin()
                || isRepairReporter(repair, userId)
                || isRepairAssignee(repair, userId)
                || isCompanyRepairAdmin(repair.getCompanyId(), userId);
    }

    private boolean canEditRepair(FireFaultRepair repair, Long userId) {
        return "0".equals(repair.getRepairStatus())
                && (isSysAdmin() || isRepairReporter(repair, userId) || isCompanyRepairAdmin(repair.getCompanyId(), userId));
    }

    private boolean canCompleteRepair(FireFaultRepair repair, Long userId) {
        return "1".equals(repair.getRepairStatus())
                && (isSysAdmin() || isRepairAssignee(repair, userId) || isCompanyRepairAdmin(repair.getCompanyId(), userId));
    }

    private boolean isSysAdmin() {
        return ShiroUtils.getSysUser() != null && ShiroUtils.getSysUser().isAdmin();
    }

    private FireFaultRepair buildEditableRepair(FireFaultRepair request, FireFaultRepair existing) {
        FireFaultRepair update = new FireFaultRepair();
        update.setRepairId(existing.getRepairId());
        update.setCompanyId(request.getCompanyId() != null ? request.getCompanyId() : existing.getCompanyId());
        update.setSystemTypeId(request.getSystemTypeId() != null ? request.getSystemTypeId() : existing.getSystemTypeId());
        update.setEquipmentId(request.getEquipmentId() != null ? request.getEquipmentId() : existing.getEquipmentId());
        update.setEquipmentName(StringUtils.isNotEmpty(request.getEquipmentName())
                ? request.getEquipmentName()
                : existing.getEquipmentName());
        update.setFoundTime(request.getFoundTime() != null ? request.getFoundTime() : existing.getFoundTime());
        update.setIsReported(StringUtils.isNotEmpty(request.getIsReported())
                ? request.getIsReported()
                : existing.getIsReported());
        update.setUrgencyLevel(StringUtils.isNotEmpty(request.getUrgencyLevel())
                ? request.getUrgencyLevel()
                : existing.getUrgencyLevel());
        update.setFaultDescription(StringUtils.isNotEmpty(request.getFaultDescription())
                ? request.getFaultDescription()
                : existing.getFaultDescription());
        update.setFaultImages(StringUtils.isNotEmpty(request.getFaultImages())
                ? request.getFaultImages()
                : existing.getFaultImages());
        update.setCustomerAddress(StringUtils.isNotEmpty(request.getCustomerAddress())
                ? request.getCustomerAddress()
                : existing.getCustomerAddress());
        update.setRemark(StringUtils.isNotEmpty(request.getRemark()) ? request.getRemark() : existing.getRemark());
        return update;
    }

    private String fillRepairLookupInfo(FireFaultRepair repair) {
        if (repair.getCompanyId() == null) {
            return "请选择报修单位";
        }

        FireCompany company = companyService.selectFireCompanyById(repair.getCompanyId());
        if (company == null) {
            return "报修单位不存在";
        }
        repair.setCompanyName(company.getCompanyName());

        if (repair.getEquipmentId() != null) {
            FireEquipment equipment = equipmentService.selectEquipmentById(repair.getEquipmentId());
            if (equipment == null) {
                return "报修设备不存在";
            }
            repair.setEquipmentName(equipment.getEquipmentName());
            if (repair.getSystemTypeId() == null) {
                repair.setSystemTypeId(equipment.getSystemTypeId());
            }
            if (StringUtils.isEmpty(repair.getSystemTypeName()) && StringUtils.isNotEmpty(equipment.getSystemName())) {
                repair.setSystemTypeName(equipment.getSystemName());
            }
            if (StringUtils.isEmpty(repair.getCustomerAddress()) && StringUtils.isNotEmpty(equipment.getLocation())) {
                repair.setCustomerAddress(equipment.getLocation());
            }
        }

        if (repair.getSystemTypeId() != null && StringUtils.isEmpty(repair.getSystemTypeName())) {
            FireSystemType systemType = systemTypeService.selectFireSystemTypeById(repair.getSystemTypeId());
            if (systemType != null) {
                repair.setSystemTypeName(systemType.getTypeName());
            }
        }

        if (StringUtils.isEmpty(repair.getEquipmentName())) {
            repair.setEquipmentName("未指定设备");
        }
        return null;
    }

    private long countRepairsByStatus(List<FireFaultRepair> repairs, String repairStatus) {
        return repairs.stream().filter(item -> repairStatus.equals(item.getRepairStatus())).count();
    }

    private Long getLongValue(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null || StringUtils.isEmpty(value.toString())) {
            return null;
        }
        return Long.parseLong(value.toString());
    }

    private String getStringValue(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }

    private Integer getIntValue(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null || StringUtils.isEmpty(value.toString())) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    private void writeReportFile(Long reportId, HttpServletResponse response, boolean attachment) {
        try {
            FireReportRecord record = fireReportRecordService.selectFireReportRecordById(reportId);
            if (record == null || StringUtils.isEmpty(record.getFilePath())) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Path filePath = getReportFilePath(record.getFilePath());
            if (!Files.exists(filePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String encodedFileName = URLEncoder.encode(record.getReportName(), "UTF-8").replaceAll("\\+", "%20");
            String dispositionType = attachment ? "attachment" : "inline";

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader(
                    "Content-Disposition",
                    dispositionType + "; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Path getReportFilePath(String fileName) {
        try {
            File staticDir = ResourceUtils.getFile("classpath:static");
            return Paths.get(staticDir.getAbsolutePath(), "report", fileName);
        } catch (Exception e) {
            return Paths.get(System.getProperty("user.dir"), "report", fileName);
        }
    }

    // ==================== 维保简报相关接口 ====================

    /**
     * 获取任务简报信息
     */
    @GetMapping("/task/briefing/{taskId}")
    public AjaxResult getTaskBriefing(@PathVariable("taskId") Long taskId) {
        FireMaintenanceTask task = taskService.selectFireMaintenanceTaskByTaskId(taskId);
        if (task == null) {
            return AjaxResult.error("任务不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getTaskId());
        result.put("taskName", task.getTaskName());
        result.put("maintenanceSummary", task.getMaintenanceSummary());
        result.put("maintenanceTime", task.getMaintenanceTime());
        result.put("companyName", task.getCompanyName());
        result.put("planStartTime", task.getPlanStartTime());
        result.put("planEndTime", task.getPlanEndTime());

        return AjaxResult.success(result);
    }

    /**
     * 保存任务简报
     */
    @PostMapping("/task/saveBriefing")
    public AjaxResult saveTaskBriefing(@RequestBody FireMaintenanceTask task) {
        // 验证任务是否存在
        FireMaintenanceTask existingTask = taskService.selectFireMaintenanceTaskByTaskId(task.getTaskId());
        if (existingTask == null) {
            return AjaxResult.error("任务不存在");
        }

        // 验证权限（是否是任务负责人）
        if (!existingTask.getManagerId().equals(ShiroUtils.getUserId())) {
            return AjaxResult.error("只有任务负责人可以编辑简报");
        }

        // 只更新简报相关字段
        FireMaintenanceTask updateTask = new FireMaintenanceTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setMaintenanceSummary(task.getMaintenanceSummary());
        updateTask.setMaintenanceTime(task.getMaintenanceTime());
        updateTask.setUpdateBy(ShiroUtils.getLoginName());

        return toAjax(taskService.updateFireMaintenanceTask(updateTask));
    }

    /**
     * 查询已完成的维修记录
     */
    @GetMapping("/repair/completed/{companyId}")
    public AjaxResult getCompletedRepairs(@PathVariable("companyId") Long companyId) {
        FireFaultRepair query = new FireFaultRepair();
        query.setCompanyId(companyId);
        query.setRepairStatus("2"); // 已完成

        List<FireFaultRepair> list = faultRepairService.selectFireFaultRepairList(query);
        return AjaxResult.success(list);
    }

    /**
     * 查询保养类型的巡检记录
     */
    @GetMapping("/inspection/maintenance/{companyId}")
    public AjaxResult getMaintenanceRecords(@PathVariable("companyId") Long companyId) {
        FireInspection query = new FireInspection();
        query.setCompanyId(companyId);
        query.setInspectionType("2"); // 保养类型

        List<FireInspection> list = inspectionService.selectFireInspectionList(query);
        return AjaxResult.success(list);
    }

    // ==================== 部门相关接口 ====================

    /**
     * 获取部门列表（小程序用，无需登录）
     * 返回所有正常状态的部门，用于下拉选择
     */
    @GetMapping("/dept/list")
    public AjaxResult getDeptList() {
        SysDept dept = new SysDept();
        dept.setStatus("0"); // 只查询正常状态的部门
        List<SysDept> deptList = deptService.selectDeptList(dept);
        
        // 简化返回数据，只返回必要字段
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (SysDept d : deptList) {
            Map<String, Object> item = new HashMap<>();
            item.put("deptId", d.getDeptId());
            item.put("deptName", d.getDeptName());
            item.put("parentId", d.getParentId());
            item.put("orderNum", d.getOrderNum());
            result.add(item);
        }
        
        return AjaxResult.success(result);
    }

    /**
     * 获取部门树形结构（小程序用，无需登录）
     * 返回树形结构的部门数据，便于层级显示
     */
    @GetMapping("/dept/tree")
    public AjaxResult getDeptTree() {
        SysDept dept = new SysDept();
        dept.setStatus("0"); // 只查询正常状态的部门
        List<com.ruoyi.common.core.domain.Ztree> ztrees = deptService.selectDeptTree(dept);
        return AjaxResult.success(ztrees);
    }
}
