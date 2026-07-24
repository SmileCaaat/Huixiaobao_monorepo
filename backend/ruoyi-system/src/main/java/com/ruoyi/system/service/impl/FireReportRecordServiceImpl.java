package com.ruoyi.system.service.impl;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.system.domain.FireReportRecord;
import com.ruoyi.system.mapper.FireReportRecordMapper;
import com.ruoyi.system.service.IFireReportRecordService;
import com.ruoyi.system.service.IMaintenanceReportService;
import com.ruoyi.system.service.report.DocxToPdfConverter;

/**
 * 维保报告记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-01-05
 */
@Service
public class FireReportRecordServiceImpl implements IFireReportRecordService {

    private static final Logger log = LoggerFactory.getLogger(FireReportRecordServiceImpl.class);

    /**
     * 模板文件在classpath中的路径
     */
    private static final String TEMPLATE_CLASSPATH = "template/空白模板维保报告.docx";

    @Autowired
    private FireReportRecordMapper fireReportRecordMapper;

    @Autowired
    private IMaintenanceReportService maintenanceReportService;

    @Autowired
    private DocxToPdfConverter docxToPdfConverter;

    @Autowired
    private com.ruoyi.fire.service.IFireBuildingService fireBuildingService;

    @Autowired
    private com.ruoyi.fire.service.IFireCompanyService fireCompanyService;

    @Autowired
    private com.ruoyi.system.service.ISysUserService sysUserService;

    @Autowired
    private com.ruoyi.system.service.ISysDeptService sysDeptService;

    @Autowired
    private IFireMaintenanceTaskService fireMaintenanceTaskService;

    @Autowired
    private com.ruoyi.fire.service.IFireMaintenanceRecordService fireMaintenanceRecordService;

    @Autowired
    private com.ruoyi.fire.mapper.FireMaintenanceRecordMapper fireMaintenanceRecordMapper;

    @Autowired
    private com.ruoyi.fire.service.IFireInspectionService fireInspectionService;

    @Autowired
    private com.ruoyi.fire.service.IFireFaultRepairService fireFaultRepairService;

    /**
     * 查询维保报告记录
     *
     * @param reportId 维保报告记录ID
     * @return 维保报告记录
     */
    @Override
    public FireReportRecord selectFireReportRecordById(Long reportId) {
        return fireReportRecordMapper.selectFireReportRecordById(reportId);
    }

    /**
     * 查询维保报告记录列表
     *
     * @param fireReportRecord 维保报告记录
     * @return 维保报告记录
     */
    @Override
    public List<FireReportRecord> selectFireReportRecordList(FireReportRecord fireReportRecord) {
        return fireReportRecordMapper.selectFireReportRecordList(fireReportRecord);
    }

    /**
     * 新增维保报告记录
     *
     * @param fireReportRecord 维保报告记录
     * @return 结果
     */
    @Override
    public int insertFireReportRecord(FireReportRecord fireReportRecord) {
        fireReportRecord.setCreateTime(DateUtils.getNowDate());
        return fireReportRecordMapper.insertFireReportRecord(fireReportRecord);
    }

    /**
     * 修改维保报告记录
     *
     * @param fireReportRecord 维保报告记录
     * @return 结果
     */
    @Override
    public int updateFireReportRecord(FireReportRecord fireReportRecord) {
        return fireReportRecordMapper.updateFireReportRecord(fireReportRecord);
    }

    /**
     * 删除维保报告记录对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteFireReportRecordByIds(String ids) {
        String[] idArray = Convert.toStrArray(ids);
        // 同时删除文件
        for (String id : idArray) {
            FireReportRecord record = selectFireReportRecordById(Long.parseLong(id));
            if (record != null && record.getFilePath() != null) {
                try {
                    Path filePath = resolveReportFile(record);
                    if (filePath != null) {
                        Files.deleteIfExists(filePath);
                        log.info("删除报告文件: {}", filePath);
                    }
                } catch (Exception e) {
                    log.warn("删除报告文件失败: {}", e.getMessage());
                }
            }
        }
        return fireReportRecordMapper.deleteFireReportRecordByIds(idArray);
    }

    /**
     * 删除维保报告记录信息
     *
     * @param reportId 维保报告记录ID
     * @return 结果
     */
    @Override
    public int deleteFireReportRecordById(Long reportId) {
        FireReportRecord record = selectFireReportRecordById(reportId);
        if (record != null && record.getFilePath() != null) {
            try {
                Path filePath = resolveReportFile(record);
                if (filePath != null) {
                    Files.deleteIfExists(filePath);
                    log.info("删除报告文件: {}", filePath);
                }
            } catch (Exception e) {
                log.warn("删除报告文件失败: {}", e.getMessage());
            }
        }
        return fireReportRecordMapper.deleteFireReportRecordById(reportId);
    }

    /**
     * 根据维保任务生成报告
     *
     * @param taskId 维保任务ID
     * @return 生成的报告记录
     */
    @Override
    public FireReportRecord generateReportForTask(Long taskId) {
        return doGenerateReportForTask(taskId);
    }

    /**
     * 按客户校验后根据维保任务生成报告（手动生成）
     */
    @Override
    public FireReportRecord generateReportForTask(Long companyId, Long taskId) {
        if (companyId == null) {
            throw new com.ruoyi.common.exception.ServiceException("请选择客户");
        }
        if (taskId == null) {
            throw new com.ruoyi.common.exception.ServiceException("请选择维保任务");
        }

        com.ruoyi.fire.domain.FireCompany company = fireCompanyService.selectFireCompanyById(companyId);
        if (company == null || !"0".equals(company.getStatus())) {
            throw new com.ruoyi.common.exception.ServiceException("客户不存在或无权访问");
        }
        assertCurrentUserCanAccessCompany(companyId);

        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        if (task == null) {
            throw new com.ruoyi.common.exception.ServiceException("维保任务不存在");
        }
        if (task.getCompanyId() == null || !companyId.equals(task.getCompanyId())) {
            throw new com.ruoyi.common.exception.ServiceException("维保任务不属于所选客户，无法生成报告");
        }

        return doGenerateReportForTask(taskId);
    }

    /**
     * 当前用户是否可访问该客户：
     * 管理员放行；已绑定客户的用户仅可访问绑定客户；未绑定的后台账号与 company/all 一致可访问全部有效客户。
     */
    private void assertCurrentUserCanAccessCompany(Long companyId) {
        try {
            if (ShiroUtils.getSysUser() != null && ShiroUtils.getSysUser().isAdmin()) {
                return;
            }
            Long userId = ShiroUtils.getUserId();
            java.util.List<com.ruoyi.fire.domain.FireCompany> bound =
                    fireCompanyService.selectCompanyListByUserId(userId);
            if (bound != null && !bound.isEmpty()) {
                boolean allowed = bound.stream()
                        .anyMatch(item -> companyId.equals(item.getCompanyId()));
                if (!allowed) {
                    throw new com.ruoyi.common.exception.ServiceException("客户不存在或无权访问");
                }
                return;
            }
            java.util.List<com.ruoyi.fire.domain.FireCompany> all =
                    fireCompanyService.selectCompanyAll();
            boolean allowed = all != null && all.stream()
                    .anyMatch(item -> companyId.equals(item.getCompanyId()));
            if (!allowed) {
                throw new com.ruoyi.common.exception.ServiceException("客户不存在或无权访问");
            }
        } catch (com.ruoyi.common.exception.ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new com.ruoyi.common.exception.ServiceException("客户不存在或无权访问");
        }
    }

    private FireReportRecord doGenerateReportForTask(Long taskId) {
        // 1. 获取任务信息
        FireMaintenanceTask task = fireMaintenanceTaskService.selectFireMaintenanceTaskByTaskId(taskId);
        if (task == null) {
            throw new RuntimeException("维保任务不存在: " + taskId);
        }

        // 获取建筑信息
        com.ruoyi.fire.domain.FireBuilding building = null;
        if (task.getBuildingId() != null) {
            building = fireBuildingService.selectBuildingById(task.getBuildingId());
        }

        // 获取公司信息
        com.ruoyi.fire.domain.FireCompany company = null;
        if (task.getCompanyId() != null) {
            company = fireCompanyService.selectFireCompanyById(task.getCompanyId());
        }

        // 获取维保单位信息 (基于当前登录用户的所属部门)
        com.ruoyi.common.core.domain.entity.SysDept maintenanceDept = null;
        try {
            Long currentUserDeptId = ShiroUtils.getSysUser().getDeptId();
            if (currentUserDeptId != null) {
                maintenanceDept = sysDeptService.selectDeptById(currentUserDeptId);
            }
        } catch (Exception e) {
            log.warn("获取当前用户部门信息失败", e);
        }

        // 2. 准备模板数据
        Map<String, Object> data = new HashMap<>();
        // 委托单位 (公司名称)
        data.put("clientName", company != null ? company.getCompanyName() : task.getCompanyName());
        // 项目名称
        data.put("projectName", task.getTaskName());
        // 项目地址 (公司地址)
        data.put("projectAddress", company != null ? company.getAddress() : "");

        // 维保单位 (取负责人的部门名称)
        data.put("maintenanceCompany", maintenanceDept != null ? maintenanceDept.getDeptName() : "未配置单位");
        // 维保单位地址 (取负责人的部门地址)
        data.put("maintenanceAddress", maintenanceDept != null ? maintenanceDept.getAddress() : "未配置单位地址");

        // 维保周期 (计划开始时间 至 计划结束时间)
        String periodStr = "";
        if (task.getPlanStartTime() != null && task.getPlanEndTime() != null) {
            periodStr = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, task.getPlanStartTime()) +
                    " 至 " +
                    DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, task.getPlanEndTime());
        }
        data.put("period", periodStr);

        // 服务电话
        data.put("servicePhone", task.getManagerPhone());

        // 项目负责人和技术负责人（都使用 managerName）
        data.put("projectManager", task.getManagerName() != null ? task.getManagerName() : "");
        
        // 作业人员（取第一个操作员）
        String workPerson = "";
        if (task.getOperatorNames() != null && !task.getOperatorNames().isEmpty()) {
            String[] operators = task.getOperatorNames().split(",");
            if (operators.length > 0) {
                workPerson = operators[0].trim();
            }
        }
        data.put("workPerson", workPerson);

        // 保留原有的字段以兼容可能存在的旧模板标签
        data.put("taskName", task.getTaskName());
        data.put("buildingName", task.getBuildingName());
        data.put("managerName", task.getManagerName());
        data.put("managerPhone", task.getManagerPhone());
        data.put("operatorNames", task.getOperatorNames());
        data.put("planStartTime",
                task.getPlanStartTime() != null
                        ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, task.getPlanStartTime())
                        : "");
        data.put("planEndTime",
                task.getPlanEndTime() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, task.getPlanEndTime())
                        : "");
        data.put("actualStartTime",
                task.getActualStartTime() != null
                        ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, task.getActualStartTime())
                        : "");
        data.put("actualEndTime",
                task.getActualEndTime() != null
                        ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, task.getActualEndTime())
                        : "");
        data.put("totalItems", task.getTotalItems());
        data.put("completedItems", task.getCompletedItems());
        data.put("generateTime", DateUtils.getTime());

        // ========== 第二页内容 ==========

        // 获取一级列表
        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level1Records =
                fireMaintenanceRecordService.selectLevel1List(taskId);

        // 维保时间 - 优先使用维保简报中填写的时间，否则使用计划时间
        String maintenanceTimeStr = "";
        if (task.getMaintenanceTime() != null) {
            maintenanceTimeStr = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, task.getMaintenanceTime());
        } else if (task.getPlanStartTime() != null && task.getPlanEndTime() != null) {
            maintenanceTimeStr = periodStr;
        }
        data.put("maintenanceTime", maintenanceTimeStr);

        // 报告日期
        data.put("reportDate", DateUtils.getDate());

        // 报告编号 (格式: WB-2026-001)
        String reportNo = generateReportNumber();
        data.put("reportNo", reportNo);

        // 报告次数 (第X次/共X次)
        String periodTypeMap = "";
        if ("1".equals(task.getPeriodType())) periodTypeMap = "月度";
        else if ("2".equals(task.getPeriodType())) periodTypeMap = "季度";
        else if ("3".equals(task.getPeriodType())) periodTypeMap = "半年度";
        else if ("4".equals(task.getPeriodType())) periodTypeMap = "年度";

        int totalPeriods = 1;
        if ("1".equals(task.getPeriodType())) totalPeriods = 12;      // 月度12次
        else if ("2".equals(task.getPeriodType())) totalPeriods = 4;  // 季度4次
        else if ("3".equals(task.getPeriodType())) totalPeriods = 2;  // 半年度2次
        else if ("4".equals(task.getPeriodType())) totalPeriods = 1;  // 年度1次

        int currentPeriod = task.getPeriodNum() != null ? task.getPeriodNum() : 1;
        data.put("reportNum", "第" + currentPeriod + "次/共" + totalPeriods + "次");
        data.put("periodTypeName", periodTypeMap);

        // ========== 建筑物信息 ==========
        // 查询公司关联的所有建筑物
        java.util.List<com.ruoyi.fire.domain.FireBuilding> buildings = new java.util.ArrayList<>();
        if (task.getCompanyId() != null) {
            com.ruoyi.fire.domain.FireBuilding queryBuilding = new com.ruoyi.fire.domain.FireBuilding();
            queryBuilding.setCompanyId(task.getCompanyId());
            buildings = fireBuildingService.selectBuildingList(queryBuilding);
        }

        data.put("buildingCount", buildings.size());

        // 为每个建筑生成单独的占位符 (最多支持10个建筑)
        for (int i = 0; i < 10; i++) {
            String suffix = String.valueOf(i + 1);
            if (i < buildings.size()) {
                com.ruoyi.fire.domain.FireBuilding b = buildings.get(i);
                data.put("buildingName" + suffix, b.getBuildingName() != null ? b.getBuildingName() : "");
                data.put("buildingAddress" + suffix, b.getAddress() != null ? b.getAddress() : "");
                data.put("buildingType" + suffix, convertBuildingType(b.getBuildingType()));
                data.put("autoFireSystem" + suffix, "1".equals(b.getAutoFireSystem()) ? "有" : "无");
                data.put("landArea" + suffix, b.getLandArea() != null ? b.getLandArea().toString() : "");
                data.put("area" + suffix, b.getArea() != null ? b.getArea().toString() : "");
                data.put("floorCount" + suffix, b.getFloorCount() != null ? b.getFloorCount().toString() : "");
                data.put("buildingHeight" + suffix, b.getBuildingHeight() != null ? b.getBuildingHeight().toString() : "");
                data.put("aboveGroundFloors" + suffix, b.getAboveGroundFloors() != null ? b.getAboveGroundFloors().toString() : "");
                data.put("undergroundFloors" + suffix, b.getUndergroundFloors() != null ? b.getUndergroundFloors().toString() : "");
                data.put("emergencyExits" + suffix, b.getEmergencyExits() != null ? b.getEmergencyExits().toString() : "");
                data.put("evacuationStairs" + suffix, b.getEvacuationStairs() != null ? b.getEvacuationStairs().toString() : "");
                data.put("fireElevators" + suffix, b.getFireElevators() != null ? b.getFireElevators().toString() : "");
                data.put("refugeFloor" + suffix, b.getRefugeFloor() != null ? b.getRefugeFloor() : "");
            } else {
                // 空值填充
                data.put("buildingName" + suffix, "");
                data.put("buildingAddress" + suffix, "");
                data.put("buildingType" + suffix, "");
                data.put("autoFireSystem" + suffix, "");
                data.put("landArea" + suffix, "");
                data.put("area" + suffix, "");
                data.put("floorCount" + suffix, "");
                data.put("buildingHeight" + suffix, "");
                data.put("aboveGroundFloors" + suffix, "");
                data.put("undergroundFloors" + suffix, "");
                data.put("emergencyExits" + suffix, "");
                data.put("evacuationStairs" + suffix, "");
                data.put("fireElevators" + suffix, "");
                data.put("refugeFloor" + suffix, "");
            }
        }

        // 如果只有一个建筑，也支持不带数字后缀的占位符
        if (!buildings.isEmpty()) {
            com.ruoyi.fire.domain.FireBuilding firstBuilding = buildings.get(0);
            data.put("buildingNameSingle", firstBuilding.getBuildingName());
            data.put("buildingAddressSingle", firstBuilding.getAddress());
            data.put("buildingTypeSingle", convertBuildingType(firstBuilding.getBuildingType()));
        }

        // ========== 动态扩展：生成建筑物表格 ==========
        // 使用 poi-tl 直接生成单个表格 (用 {{buildingTable}} 占位符)
        if (!buildings.isEmpty()) {
            // 构建表格行数据
            java.util.List<String[]> rows = new java.util.ArrayList<>();

            for (int i = 0; i < buildings.size(); i++) {
                com.ruoyi.fire.domain.FireBuilding b = buildings.get(i);

                // 每个建筑添加多行
                rows.add(new String[]{"建筑物信息表（" + (i + 1) + "）", "", "", ""});
                rows.add(new String[]{"建筑名称", safeStr(b.getBuildingName()), "", ""});
                rows.add(new String[]{"建筑地址", safeStr(b.getAddress()), "", ""});
                rows.add(new String[]{"建筑类别", convertBuildingType(b.getBuildingType()), "自动消防设施", "1".equals(b.getAutoFireSystem()) ? "有" : "无"});
                rows.add(new String[]{"占地面积（㎡）", safeStr(b.getLandArea()), "建筑面积（㎡）", safeStr(b.getArea())});
                rows.add(new String[]{"楼层数（层）", safeStr(b.getFloorCount()), "建筑高度（m）", safeStr(b.getBuildingHeight())});
                rows.add(new String[]{"地上层层数", safeStr(b.getAboveGroundFloors()), "地下层层数", safeStr(b.getUndergroundFloors())});
                rows.add(new String[]{"安全出口数（个）", safeStr(b.getEmergencyExits()), "疏散楼梯数（个）", safeStr(b.getEvacuationStairs())});
                rows.add(new String[]{"消防电梯数（个）", safeStr(b.getFireElevators()), "避难层位置", safeStr(b.getRefugeFloor())});

                // 建筑之间加空行分隔
                if (i < buildings.size() - 1) {
                    rows.add(new String[]{"", "", "", ""});
                }
            }

            // 创建表格
            String[][] tableData = rows.toArray(new String[0][]);
            com.deepoove.poi.data.TableRenderData table = com.deepoove.poi.data.Tables.of(tableData)
                    .border(com.deepoove.poi.data.style.BorderStyle.DEFAULT)
                    .create();
            data.put("buildingTable", table);
        } else {
            // 无建筑时输出null（模板中使用占位符时会被忽略）
            data.put("buildingTable", null);
        }

        // ========== 巡查记录表格 - 动态生成（使用维保任务数据）==========
        // 重用之前获取的 level1Records

        if (level1Records != null && !level1Records.isEmpty()) {
            com.deepoove.poi.data.TableRenderData checkTable = createCheckRecordTable(taskId, level1Records);
            data.put("checkTable", checkTable);

            // 填充统计信息（包括 systemNames 和 maintenanceSummary）
            fillCheckStatistics(data, taskId, level1Records, task);
        } else {
            data.put("checkTable", null);
            // 即使没有检查记录，也要设置 maintenanceSummary
            if (task.getMaintenanceSummary() != null && !task.getMaintenanceSummary().trim().isEmpty()) {
                data.put("maintenanceSummary", task.getMaintenanceSummary());
            } else {
                data.put("maintenanceSummary", "暂无维保记录");
            }
            data.put("systemNames", "");
            data.put("systemCount", 0);
        }

        // ========== 测试记录表格 - 动态生成（使用维保任务recordType=1的消防测试数据）==========
        // 查询该任务下recordType=1的消防测试记录
        com.ruoyi.fire.domain.FireMaintenanceRecord testQuery = new com.ruoyi.fire.domain.FireMaintenanceRecord();
        testQuery.setTaskId(taskId);
        testQuery.setRecordType("1");  // 消防设施测试
        testQuery.setLevel(1);  // 一级分类
        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> testLevel1Records =
                fireMaintenanceRecordService.selectFireMaintenanceRecordList(testQuery);

        if (testLevel1Records != null && !testLevel1Records.isEmpty()) {
            com.deepoove.poi.data.TableRenderData testTable = createFireTestRecordTable(taskId, testLevel1Records);
            data.put("testTable", testTable);
        } else {
            data.put("testTable", null);
        }

        // 添加报表尾部备注信息 (居中展示)
        String noteContent = "注1：情况正常打“√”，存在问题或故障的打“√”，无设备情况在正常打“——”，并在备注栏中写明存在问题或故障处理情况；\n" +
                "注2：对发现的问题应及时处理，当场不能处置的要填报《建筑消防设施故障维修记录表》；\n" +
                "注3：本表由单位存档，存档时间不得少于6年。";
        data.put("reportNotes", com.deepoove.poi.data.Texts.of(noteContent).create());

        // ========== 设备实测记录表格（使用消防测试第二级设备维护信息）==========
        // 重用前面查询的 testLevel1Records（recordType=1的消防测试一级记录）
        // 收集所有第二级设备记录
        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> allLevel2Equipments = new java.util.ArrayList<>();
        if (testLevel1Records != null && !testLevel1Records.isEmpty()) {
            for (com.ruoyi.fire.domain.FireMaintenanceRecord level1 : testLevel1Records) {
                java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level2Records =
                        fireMaintenanceRecordService.selectLevel2List(taskId, level1.getRecordId());
                if (level2Records != null && !level2Records.isEmpty()) {
                    allLevel2Equipments.addAll(level2Records);
                }
            }
        }

        if (!allLevel2Equipments.isEmpty()) {
            // 为每个设备创建独立的表格，然后组合
            java.util.List<com.deepoove.poi.data.RowRenderData> allRows = new java.util.ArrayList<>();

            for (int i = 0; i < allLevel2Equipments.size(); i++) {
                com.ruoyi.fire.domain.FireMaintenanceRecord equipment = allLevel2Equipments.get(i);

                // 表头行 - 增加行高到1.2cm，居中对齐
                allRows.add(com.deepoove.poi.data.Rows.of("设备名称", "设备位置", "测试情况", "测试时间", "测试结果")
                        .textBold().bgColor("D9D9D9").rowHeight(1.2)
                        .center().create());

                // 数据行 - 增加行高到1.5cm，支持多行文本，居中对齐
                String testTime = equipment.getTestTime() != null
                        ? DateUtils.parseDateToStr("yyyy/MM/dd", equipment.getTestTime())
                        : "";
                allRows.add(com.deepoove.poi.data.Rows.of(
                        safeStr(equipment.getItemName()),
                        safeStr(equipment.getDeviceLocation()),
                        safeStr(equipment.getTestSituation()),
                        testTime,
                        safeStr(equipment.getTestResult())
                ).rowHeight(1.5).center().create());

                // 图片处理 - 始终显示现场照片行（即使没有图片也占位）
                // 图片行 - 第一列为"现场照片"标签，后面是图片
                java.util.List<com.deepoove.poi.data.CellRenderData> imageCells = new java.util.ArrayList<>();
                imageCells.add(com.deepoove.poi.data.Cells.of("现场照片").verticalCenter().horizontalCenter().create());

                if (equipment.getSitePhotos() != null && !equipment.getSitePhotos().isEmpty()) {
                    String[] photoUrls = equipment.getSitePhotos().split(",");
                    log.info("设备 {} 有 {} 张现场照片", equipment.getItemName(), photoUrls.length);
                    for (String imgUrl : photoUrls) {
                        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                            try {
                                String localPath = convertUrlToLocalPath(imgUrl.trim());
                                log.info("图片URL: {} -> 本地路径: {}", imgUrl, localPath);
                                java.io.File imgFile = new java.io.File(localPath);
                                if (imgFile.exists() && imgFile.isFile()) {
                                    log.info("图片文件存在: {}", localPath);
                                    imageCells.add(com.deepoove.poi.data.Cells.of(
                                            com.deepoove.poi.data.Pictures.ofLocal(localPath)
                                                    .size(150, 110)  // 增大图片尺寸
                                                    .create()
                                    ).verticalCenter().horizontalCenter().create());
                                } else {
                                    log.warn("图片文件不存在: {}, Profile配置: {}", localPath,
                                            com.ruoyi.common.config.RuoYiConfig.getProfile());
                                    imageCells.add(com.deepoove.poi.data.Cells.of("").verticalCenter().horizontalCenter().create());
                                }
                            } catch (Exception e) {
                                log.warn("处理图片失败: {} - {}", imgUrl, e.getMessage());
                                imageCells.add(com.deepoove.poi.data.Cells.of("").verticalCenter().horizontalCenter().create());
                            }
                        }
                    }
                } else {
                    log.info("设备 {} 没有现场照片", equipment.getItemName());
                }

                // 填充空单元格到5列
                while (imageCells.size() < 5) {
                    imageCells.add(com.deepoove.poi.data.Cells.of("").verticalCenter().horizontalCenter().create());
                }
                // 如果超过5列，截取前5个
                if (imageCells.size() > 5) {
                    imageCells = imageCells.subList(0, 5);
                }

                com.deepoove.poi.data.RowRenderData imageRow = new com.deepoove.poi.data.RowRenderData();
                imageRow.setCells(imageCells);
//                imageRow.setHeight(3.5);  // 图片行高度设置为3.5cm
                allRows.add(imageRow);

                // 设备之间添加空行分隔（除了最后一个）- 增加间隔高度
                if (i < allLevel2Equipments.size() - 1) {
                    allRows.add(com.deepoove.poi.data.Rows.of("", "", "", "", "").rowHeight(1.0).create());
                }
            }

            // 创建表格
            com.deepoove.poi.data.TableRenderData testTable = com.deepoove.poi.data.Tables.ofPercentWidth("90%")
                    .border(com.deepoove.poi.data.style.BorderStyle.DEFAULT)
                    .create();
            testTable.setRows(allRows);
            data.put("testRecordTable", testTable);
        } else {
            // 无数据时设置为null（模板中占位符会被忽略）
            data.put("testRecordTable", null);
        }

        // 2.3 生成故障维修记录表格（只包含已完成的维修记录）
        java.util.List<com.ruoyi.fire.domain.FireFaultRepair> completedRepairs = getCompletedRepairRecords(task.getCompanyId(), task.getCreateTime());
        if (!completedRepairs.isEmpty()) {
            com.deepoove.poi.data.TableRenderData repairTable = createRepairRecordTable(completedRepairs);
            data.put("repairRecordTable", repairTable);
        } else {
            data.put("repairRecordTable", null);
        }

        // 2.4 生成建筑消防设施维护保养记录表（使用保养类型的巡检数据）
        java.util.List<com.ruoyi.fire.domain.FireInspection> maintenanceRecords =
                fireInspectionService.selectRecentInspectionTests(task.getCompanyId());

        // 过滤出保养类型的记录（inspectionType = "2"）
        java.util.List<com.ruoyi.fire.domain.FireInspection> filteredMaintenanceRecords = new java.util.ArrayList<>();
        if (maintenanceRecords != null) {
            for (com.ruoyi.fire.domain.FireInspection inspection : maintenanceRecords) {
                if ("2".equals(inspection.getInspectionType())) {
                    filteredMaintenanceRecords.add(inspection);
                }
            }
        }

        if (!filteredMaintenanceRecords.isEmpty()) {
            com.deepoove.poi.data.TableRenderData maintenanceTable = createMaintenanceRecordTable(filteredMaintenanceRecords);
            data.put("maintenanceRecordTable", maintenanceTable);
        } else {
            data.put("maintenanceRecordTable", null);
        }

        // 3. 生成临时 DOCX → 转换为 PDF → 落盘到统一报告目录
        String templatePath = getTemplatePath();
        String companyPart = sanitizeFileName(company != null ? company.getCompanyName() : task.getCompanyName());
        String taskPart = sanitizeFileName(task.getTaskName());
        String stamp = DateUtils.dateTimeNow();
        String pdfFileName = companyPart + "_" + taskPart + "_" + stamp + ".pdf";
        String unique = java.util.UUID.randomUUID().toString().replace("-", "");

        Path outputDir = getReportOutputDir();
        Path pdfPath = outputDir.resolve(pdfFileName);
        Path tempDir = null;
        Path tempDocx = null;

        try {
            Files.createDirectories(outputDir);
            tempDir = Files.createTempDirectory("fire-report-" + unique + "-");
            tempDocx = tempDir.resolve("report-" + unique + ".docx");

            log.info("开始生成报告 DOCX, 模板: {}, 临时: {}", templatePath, tempDocx);
            maintenanceReportService.generateReport(data, templatePath, tempDocx.toString());

            log.info("开始转换为 PDF: {}", pdfPath);
            docxToPdfConverter.convert(tempDocx, pdfPath);
            DocxToPdfConverter.assertValidPdf(pdfPath);
            log.info("报告 PDF 生成成功: {}", pdfPath);
        } catch (ServiceException e) {
            deleteQuietly(pdfPath);
            throw e;
        } catch (Exception e) {
            deleteQuietly(pdfPath);
            log.error("生成报告失败", e);
            throw new ServiceException("生成报告失败: " + e.getMessage());
        } finally {
            deleteQuietly(tempDocx);
            deleteDirectoryQuietly(tempDir);
        }

        // 4. 获取文件大小并保存记录（仅 PDF 成功后落库）
        long fileSize;
        try {
            fileSize = Files.size(pdfPath);
        } catch (Exception e) {
            deleteQuietly(pdfPath);
            throw new ServiceException("读取 PDF 文件大小失败");
        }

        FireReportRecord record = new FireReportRecord();
        record.setTaskId(taskId);
        record.setTaskName(task.getTaskName());
        record.setReportName(pdfFileName);
        record.setFilePath(pdfFileName); // 只存文件名，下载/预览时拼接安全目录
        record.setFileSize(fileSize);
        record.setCreateTime(new Date());

        try {
            record.setCreateBy(ShiroUtils.getLoginName());
        } catch (Exception e) {
            record.setCreateBy("System");
        }

        try {
            insertFireReportRecord(record);
        } catch (Exception e) {
            deleteQuietly(pdfPath);
            throw new ServiceException("保存报告记录失败: " + e.getMessage());
        }

        return record;
    }

    @Override
    public Path resolveReportFile(FireReportRecord record) {
        if (record == null || StringUtils.isEmpty(record.getFilePath())) {
            return null;
        }
        String stored = record.getFilePath().replace("\\", "/");
        // 禁止路径穿越：仅允许文件名
        String fileName = Paths.get(stored).getFileName().toString();
        if (StringUtils.isEmpty(fileName) || fileName.contains("..")) {
            return null;
        }

        List<Path> candidates = new java.util.ArrayList<>();
        candidates.add(getReportOutputDir().resolve(fileName));
        candidates.add(Paths.get(System.getProperty("user.dir"), "report", fileName));
        try {
            File staticDir = ResourceUtils.getFile("classpath:static");
            candidates.add(Paths.get(staticDir.getAbsolutePath(), "report", fileName));
        } catch (Exception ignored) {
            // jar 环境无解压 static 目录时忽略
        }

        Path reportRoot = getReportOutputDir().toAbsolutePath().normalize();
        for (Path candidate : candidates) {
            try {
                Path normalized = candidate.toAbsolutePath().normalize();
                if (!Files.isRegularFile(normalized)) {
                    continue;
                }
                // 主目录必须在报告根下；兼容旧目录时允许 user.dir/report 与历史 static/report
                if (normalized.startsWith(reportRoot)
                        || normalized.toString().replace("\\", "/").contains("/report/" + fileName)
                        || normalized.getFileName().toString().equals(fileName)
                                && normalized.getParent() != null
                                && "report".equalsIgnoreCase(normalized.getParent().getFileName().toString())) {
                    return normalized;
                }
            } catch (Exception ignored) {
                // continue
            }
        }
        return null;
    }

    @Override
    public void assertReportFileReady(FireReportRecord record) {
        if (record == null) {
            throw new ServiceException("报告不存在");
        }
        if (StringUtils.isEmpty(record.getFilePath())) {
            throw new ServiceException("报告尚未生成成功");
        }
        Path file = resolveReportFile(record);
        if (file == null || !Files.isRegularFile(file)) {
            throw new ServiceException("报告文件不存在或已被删除");
        }
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".pdf")) {
            try {
                DocxToPdfConverter.assertValidPdf(file);
            } catch (ServiceException e) {
                throw e;
            } catch (Exception e) {
                throw new ServiceException("报告文件损坏，无法预览");
            }
            return;
        }
        if (name.endsWith(".docx")) {
            throw new ServiceException("历史报告为 Word 格式，无法在线预览，请重新生成 PDF 报告");
        }
        throw new ServiceException("不支持的报告文件格式");
    }

    /**
     * 获取模板文件路径
     * 优先从classpath读取，如果是jar包运行则提取到临时目录
     */
    private String getTemplatePath() {
        try {
            // 尝试直接获取文件路径（开发环境）
            ClassPathResource resource = new ClassPathResource(TEMPLATE_CLASSPATH);
            if (resource.exists()) {
                try {
                    // 如果可以直接获取文件（非jar运行）
                    return resource.getFile().getAbsolutePath();
                } catch (Exception e) {
                    // jar包运行，需要提取到临时目录
                    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "fire-report-template");
                    Files.createDirectories(tempDir);
                    Path tempFile = tempDir.resolve("空白模板维保报告.docx");

                    try (InputStream is = resource.getInputStream()) {
                        Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return tempFile.toString();
                }
            }
        } catch (Exception e) {
            log.warn("从classpath获取模板失败: {}", e.getMessage());
        }

        // 回退到默认路径
        throw new RuntimeException("模板文件不存在，请将模板放置到classpath:template/空白模板维保报告.docx");
    }

    /**
     * 报告输出目录：统一使用 ruoyi.profile/report，兼容本地与 JAR 部署
     */
    private Path getReportOutputDir() {
        String profile = RuoYiConfig.getProfile();
        if (StringUtils.isEmpty(profile)) {
            profile = "./uploadPath";
        }
        return Paths.get(profile, "report").toAbsolutePath().normalize();
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

    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "未命名";
        }
        // 移除文件名中的非法字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    /**
     * 生成报告编号
     * 格式: WB-2026-001
     */
    private String generateReportNumber() {
        String year = DateUtils.parseDateToStr("yyyy", new Date());
        // 获取当年已有报告数量，生成序号
        FireReportRecord query = new FireReportRecord();
        List<FireReportRecord> records = selectFireReportRecordList(query);

        // 筛选当年的报告
        int currentYearCount = 0;
        for (FireReportRecord r : records) {
            if (r.getCreateTime() != null) {
                String recordYear = DateUtils.parseDateToStr("yyyy", r.getCreateTime());
                if (year.equals(recordYear)) {
                    currentYearCount++;
                }
            }
        }

        // 生成序号 (当前数量 + 1)
        int seq = currentYearCount + 1;
        return String.format("WB-%s-%03d", year, seq);
    }

    /**
     * 转换建筑类型编码为中文名称
     */
    private String convertBuildingType(String typeCode) {
        if (typeCode == null) return "";
        switch (typeCode) {
            case "type1_high_rise_civil":
                return "一类高层民用建筑";
            case "type2_high_rise_civil":
                return "二类高层民用建筑";
            case "high_rise_factory":
                return "高层厂房";
            case "high_rise_warehouse":
                return "高层库房";
            case "single_multi_civil":
                return "单、多层民用建筑";
            case "single_multi_factory":
                return "单、多层厂房";
            case "single_multi_warehouse":
                return "单、多层库房";
            case "underground":
                return "地下建筑";
            case "tunnel_culvert":
                return "隧道、涵洞";
            case "other":
                return "其他建筑";
            default:
                return typeCode;
        }
    }

    /**
     * 安全转换为字符串，null返回空字符串
     */
    private String safeStr(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * 将图片URL转换为本地文件路径
     * https://localhost:83/profile/upload/xxx -> D:/ruoyi/uploadPath/upload/xxx
     * /profile/upload/xxx -> D:/ruoyi/uploadPath/upload/xxx
     */
    private String convertUrlToLocalPath(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        // 处理完整URL: https://localhost:83/profile/upload/xxx
        if (url.contains("/profile/")) {
            int profileIndex = url.indexOf("/profile/");
            String relativePath = url.substring(profileIndex + "/profile".length());
            return com.ruoyi.common.config.RuoYiConfig.getProfile() + relativePath;
        }

        // 处理相对路径: /profile/upload/xxx
        if (url.startsWith("/profile")) {
            return com.ruoyi.common.config.RuoYiConfig.getProfile() + url.substring("/profile".length());
        }

        return url;
    }

    /**
     * 创建巡查记录表格
     *
     * @param taskId        任务ID
     * @param level1Records 一级记录列表
     * @return 表格数据
     */
    private com.deepoove.poi.data.TableRenderData createCheckRecordTable(
            Long taskId,
            java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level1Records) {

        java.util.List<String[]> rows = new java.util.ArrayList<>();
        java.util.List<int[]> level1Merges = new java.util.ArrayList<>();
        java.util.List<int[]> level2Merges = new java.util.ArrayList<>();

        // 添加表头
        rows.add(new String[]{"巡查项目", "巡查内容(设备数)", "", "巡查情况", "", ""});
        rows.add(new String[]{"", "", "", "正常", "故障", "故障原因及处理情况"});

        int currentRow = 2; // 从第3行开始（前两行是表头）

        // 遍历一级分类
        for (com.ruoyi.fire.domain.FireMaintenanceRecord level1 : level1Records) {
            String level1Name = level1.getItemName();

            // 查询该一级下的所有二级
            java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level2Records =
                    fireMaintenanceRecordService.selectLevel2List(taskId, level1.getRecordId());

            if (level2Records == null || level2Records.isEmpty()) {
                // 无二级的一级，添加一行空数据
                rows.add(new String[]{level1Name, "", "——", "——", "", ""});
                currentRow++;
                continue;
            }

            int level1StartRow = currentRow;
            int level1RowCount = 0;

            // 遍历二级分类
            for (com.ruoyi.fire.domain.FireMaintenanceRecord level2 : level2Records) {
                // 查询该二级下的所有三级
                java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level3Records =
                        fireMaintenanceRecordService.selectLevel3List(taskId, level2.getRecordId());

                if (level3Records == null || level3Records.isEmpty()) {
                    // 无三级的二级，添加一行
                    String displayLevel1Name = (level1RowCount == 0) ? level1Name : "";
                    rows.add(new String[]{displayLevel1Name, level2.getItemName(), "——", "——", "", ""});
                    level1RowCount++;
                    currentRow++;
                    continue;
                }

                int level2StartRow = currentRow;

                // 遍历三级检查项
                for (int i = 0; i < level3Records.size(); i++) {
                    com.ruoyi.fire.domain.FireMaintenanceRecord level3 = level3Records.get(i);

                    // 只在第一行显示一级、二级名称
                    String displayLevel1Name = (level1RowCount == 0 && i == 0) ? level1Name : "";
                    String displayLevel2Name = (i == 0) ? level2.getItemName() : "";

                    // 格式化检查结果
                    String[] resultCols = formatCheckResult(level3);

                    rows.add(new String[]{
                            displayLevel1Name,
                            displayLevel2Name,
                            level3.getItemName(),
                            resultCols[0],  // 正常
                            resultCols[1],  // 故障
                            resultCols[2]   // 故障描述
                    });

                    level1RowCount++;
                    currentRow++;
                }

                // 记录二级合并信息（如果有多个三级）
                if (level3Records.size() > 1) {
                    level2Merges.add(new int[]{level2StartRow, currentRow - 1, 1});
                }
            }

            // 记录一级合并信息（如果有多行）
            if (level1RowCount > 1) {
                level1Merges.add(new int[]{level1StartRow, currentRow - 1, 0});
            }
        }

        // 创建表格
        return createTableWithMerge(rows, level1Merges, level2Merges);
    }

    /**
     * 格式化检查结果
     *
     * @param record 检查记录
     * @return [正常, 故障, 故障描述]
     */
    private String[] formatCheckResult(com.ruoyi.fire.domain.FireMaintenanceRecord record) {
        String normal = "";
        String fault = "";
        String faultDesc = "";

        String checkResult = record.getCheckResult();
        if (checkResult == null) {
            checkResult = "0";
        }

        switch (checkResult) {
            case "1":  // 正常
                normal = "√";
                break;
            case "2":  // 故障
                fault = "√";
                faultDesc = record.getFaultDescription() != null ? record.getFaultDescription() : "";
                break;
            case "3":  // 无此设备
                normal = "——";
                break;
            default:   // 0=待检查
                break;
        }

        return new String[]{normal, fault, faultDesc};
    }

    /**
     * 创建带合并单元格的表格
     *
     * @param rows         表格行数据
     * @param level1Merges 一级合并信息
     * @param level2Merges 二级合并信息
     * @return 表格数据
     */
    private com.deepoove.poi.data.TableRenderData createTableWithMerge(
            java.util.List<String[]> rows,
            java.util.List<int[]> level1Merges,
            java.util.List<int[]> level2Merges) {

        if (rows.size() <= 2) {
            return null;
        }

        // 转换为RowRenderData
        java.util.List<com.deepoove.poi.data.RowRenderData> tableRows = new java.util.ArrayList<>();
        for (String[] row : rows) {
            tableRows.add(com.deepoove.poi.data.Rows.of(row).create());
        }

        // 创建表格
        com.deepoove.poi.data.TableRenderData table = com.deepoove.poi.data.Tables.ofPercentWidth("90%")
                .border(com.deepoove.poi.data.style.BorderStyle.DEFAULT)
                .create();
        table.setRows(tableRows);

        // 设置合并规则
        com.deepoove.poi.data.MergeCellRule.MergeCellRuleBuilder mergeBuilder =
                com.deepoove.poi.data.MergeCellRule.builder();

        // 合并表头
        // 第一行第一列（巡查项目）合并到第二行第一列
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 0),
                com.deepoove.poi.data.MergeCellRule.Grid.of(1, 0)
        );

        // 第一行第二列（巡查内容）合并到第二行第三列
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 1),
                com.deepoove.poi.data.MergeCellRule.Grid.of(1, 2)
        );

        // 第一行第四列（巡查情况）合并到第一行第六列
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 3),
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 5)
        );

        // 合并一级分类
        for (int[] merge : level1Merges) {
            mergeBuilder.map(
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[0], merge[2]),
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[1], merge[2])
            );
        }

        // 合并二级分类
        for (int[] merge : level2Merges) {
            mergeBuilder.map(
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[0], merge[2]),
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[1], merge[2])
            );
        }

        table.setMergeRule(mergeBuilder.build());
        return table;
    }

    /**
     * 填充巡查统计信息
     *
     * @param data          数据Map
     * @param taskId        任务ID
     * @param level1Records 一级记录列表
     * @param task          维保任务对象
     */
    private void fillCheckStatistics(
            java.util.Map<String, Object> data,
            Long taskId,
            java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level1Records,
            com.ruoyi.fire.domain.FireMaintenanceTask task) {

        // 系统名称列表 - 生成两个纯文本列表（左右两列）
        StringBuilder systemNames1 = new StringBuilder();
        StringBuilder systemNames2 = new StringBuilder();
        
        int totalSystems = level1Records.size();
        int halfSize = (totalSystems + 1) / 2;
        
        // 左边列表（前一半）
        for (int i = 0; i < halfSize && i < totalSystems; i++) {
            if (i > 0) {
                systemNames1.append("\n");
            }
            systemNames1.append("☑  ").append(level1Records.get(i).getItemName());
        }
        
        // 右边列表（后一半）
        for (int i = halfSize; i < totalSystems; i++) {
            if (i > halfSize) {
                systemNames2.append("\n");
            }
            systemNames2.append("☑  ").append(level1Records.get(i).getItemName());
        }
        
        data.put("systemNames1", systemNames1.toString());
        data.put("systemNames2", systemNames2.toString());
        data.put("systemCount", level1Records.size());

        // 统计所有三级检查项
        java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> allLevel3Records =
                fireMaintenanceRecordMapper.selectLevel3ByTaskId(taskId);

        int totalCheckItems = 0;
        int normalItems = 0;
        int faultItems = 0;
        int pendingItems = 0;

        for (com.ruoyi.fire.domain.FireMaintenanceRecord record : allLevel3Records) {
            totalCheckItems++;
            String checkResult = record.getCheckResult();
            if (checkResult == null) {
                checkResult = "0";
            }

            switch (checkResult) {
                case "1":
                    normalItems++;
                    break;
                case "2":
                    faultItems++;
                    break;
                default:
                    pendingItems++;
                    break;
            }
        }

        // 维保情况简述 - 优先使用维保简报中填写的内容，否则自动生成
        String maintenanceSummaryText = "";
        if (task.getMaintenanceSummary() != null && !task.getMaintenanceSummary().trim().isEmpty()) {
            // 使用用户填写的维保简报
            maintenanceSummaryText = task.getMaintenanceSummary();
            log.info("使用维保简报中的简述，长度: {}", maintenanceSummaryText.length());
        } else {
            // 自动生成维保情况简述
            StringBuilder summary = new StringBuilder();
            summary.append("本次维保共涉及").append(level1Records.size()).append("个消防系统，");
            summary.append("检查项目共计").append(totalCheckItems).append("项。");

            if (normalItems > 0) {
                summary.append("其中正常项目").append(normalItems).append("项");
            }
            if (pendingItems > 0) {
                summary.append("，待检查项目").append(pendingItems).append("项");
            }
            summary.append("。");

            if (faultItems == 0 && pendingItems == 0) {
                summary.append("本次维保检查中，各消防系统运行正常，未发现明显故障隐患。");
            } else if (faultItems > 0) {
                summary.append("发现故障隐患").append(faultItems).append("项，已做记录并提出整改建议。");
            }
            maintenanceSummaryText = summary.toString();
            log.info("自动生成维保简述，长度: {}", maintenanceSummaryText.length());
        }

        log.info("最终维保简述内容: {}", maintenanceSummaryText);
        data.put("maintenanceSummary", maintenanceSummaryText);
    }

    /**
     * 创建消防测试记录表格（使用维保任务recordType=1的数据）
     * 表格格式：测试项目 | 测试内容(设备数) | 实测记录（正常、故障、故障原因及处理情况）
     *
     * @param taskId        任务ID
     * @param level1Records 一级记录列表（recordType=1）
     * @return 表格数据
     */
    private com.deepoove.poi.data.TableRenderData createFireTestRecordTable(
            Long taskId,
            java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level1Records) {

        java.util.List<String[]> rows = new java.util.ArrayList<>();
        java.util.List<int[]> level1Merges = new java.util.ArrayList<>();
        java.util.List<int[]> level2Merges = new java.util.ArrayList<>();

        // 添加表头（两行）
        rows.add(new String[]{"测试项目", "测试内容(设备数)", "", "实测记录", "", ""});
        rows.add(new String[]{"", "", "", "正常", "故障", "故障原因及处理情况"});

        int currentRow = 2; // 从第3行开始（前两行是表头）

        // 遍历一级分类（测试项目）
        for (com.ruoyi.fire.domain.FireMaintenanceRecord level1 : level1Records) {
            String level1Name = level1.getItemName();

            // 查询该一级下的所有二级（recordType=1）
            com.ruoyi.fire.domain.FireMaintenanceRecord level2Query = new com.ruoyi.fire.domain.FireMaintenanceRecord();
            level2Query.setTaskId(taskId);
            level2Query.setParentRecordId(level1.getRecordId());
            level2Query.setLevel(2);
            level2Query.setRecordType("1");
            java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level2Records =
                    fireMaintenanceRecordService.selectFireMaintenanceRecordList(level2Query);

            if (level2Records == null || level2Records.isEmpty()) {
                // 无二级的一级，添加一行空数据
                rows.add(new String[]{level1Name, "", "——", "——", "", ""});
                currentRow++;
                continue;
            }

            int level1StartRow = currentRow;
            int level1RowCount = 0;

            // 遍历二级分类（测试内容）
            for (com.ruoyi.fire.domain.FireMaintenanceRecord level2 : level2Records) {
                // 查询该二级下的所有三级（具体设备）
                com.ruoyi.fire.domain.FireMaintenanceRecord level3Query = new com.ruoyi.fire.domain.FireMaintenanceRecord();
                level3Query.setTaskId(taskId);
                level3Query.setParentRecordId(level2.getRecordId());
                level3Query.setLevel(3);
                level3Query.setRecordType("1");
                java.util.List<com.ruoyi.fire.domain.FireMaintenanceRecord> level3Records =
                        fireMaintenanceRecordService.selectFireMaintenanceRecordList(level3Query);

                if (level3Records == null || level3Records.isEmpty()) {
                    // 无三级的二级，添加一行
                    String displayLevel1Name = (level1RowCount == 0) ? level1Name : "";
                    rows.add(new String[]{displayLevel1Name, level2.getItemName(), "——", "——", "", ""});
                    level1RowCount++;
                    currentRow++;
                    continue;
                }

                int level2StartRow = currentRow;

                // 遍历三级测试项
                for (int i = 0; i < level3Records.size(); i++) {
                    com.ruoyi.fire.domain.FireMaintenanceRecord level3 = level3Records.get(i);

                    // 只在第一行显示一级、二级名称
                    String displayLevel1Name = (level1RowCount == 0 && i == 0) ? level1Name : "";
                    String displayLevel2Name = (i == 0) ? level2.getItemName() : "";

                    // 格式化测试结果
                    String[] resultCols = formatTestResult(level3);

                    // 确保所有字段都不为null
                    String col0 = displayLevel1Name != null ? displayLevel1Name : "";
                    String col1 = displayLevel2Name != null ? displayLevel2Name : "";
                    String col2 = level3.getItemName() != null ? level3.getItemName() : "";
                    String col3 = resultCols[0] != null ? resultCols[0] : "";
                    String col4 = resultCols[1] != null ? resultCols[1] : "";
                    String col5 = resultCols[2] != null ? resultCols[2] : "";

                    rows.add(new String[]{col0, col1, col2, col3, col4, col5});
                    
                    log.debug("添加测试记录行: [{}, {}, {}, {}, {}, {}]", 
                            col0, col1, col2, col3, col4, col5);

                    level1RowCount++;
                    currentRow++;
                }

                // 记录二级合并信息（如果有多个三级）
                if (level3Records.size() > 1) {
                    level2Merges.add(new int[]{level2StartRow, currentRow - 1, 1});
                }
            }

            // 记录一级合并信息（如果有多行）
            if (level1RowCount > 1) {
                level1Merges.add(new int[]{level1StartRow, currentRow - 1, 0});
            }
        }

        // 创建表格
        log.info("消防测试表格生成完成，共 {} 行数据（包含表头）", rows.size());
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            log.debug("第{}行: 列数={}, 内容={}", i, row.length, java.util.Arrays.toString(row));
        }
        
        return createFireTestTableWithMerge(rows, level1Merges, level2Merges);
    }

    /**
     * 格式化测试结果
     *
     * @param record 测试记录
     * @return [正常, 故障, 故障描述]
     */
    private String[] formatTestResult(com.ruoyi.fire.domain.FireMaintenanceRecord record) {
        String normal = "";
        String fault = "";
        String faultDesc = "";

        // 打印完整的记录信息用于调试
        log.info("========== 开始格式化测试结果 ==========");
        log.info("设备名称: {}", record.getItemName());
        log.info("recordId: {}", record.getRecordId());
        log.info("testResult原始值: [{}], 类型: {}", record.getTestResult(), 
                record.getTestResult() != null ? record.getTestResult().getClass().getName() : "null");
        log.info("checkResult原始值: [{}], 类型: {}", record.getCheckResult(),
                record.getCheckResult() != null ? record.getCheckResult().getClass().getName() : "null");
        log.info("testSituation: [{}]", record.getTestSituation());
        log.info("faultDescription: [{}]", record.getFaultDescription());

        // 使用testResult字段判断测试结果
        String testResult = record.getTestResult();
        if (testResult == null || testResult.isEmpty()) {
            // 如果testResult为空，回退到checkResult
            testResult = record.getCheckResult();
            log.info("testResult为空，使用checkResult: [{}]", testResult);
        }

        if (testResult == null || testResult.isEmpty()) {
            testResult = "0";
            log.info("checkResult也为空，默认设置为: 0");
        }

        // 去除空格并转换为字符串
        testResult = testResult.trim();
        log.info("处理后的testResult: [{}]", testResult);

        // 使用equals比较，支持多种格式
        if ("1".equals(testResult) || "正常".equals(testResult)) {
            // 正常
            normal = "√";
            log.info("判定为正常");
        } else if ("2".equals(testResult) || "故障".equals(testResult) || "异常".equals(testResult)) {
            // 故障
            fault = "√";
            log.info("判定为故障");
            // 优先使用testSituation，否则使用faultDescription
            if (record.getTestSituation() != null && !record.getTestSituation().trim().isEmpty()) {
                faultDesc = record.getTestSituation().trim();
                log.info("使用testSituation作为故障描述: [{}]", faultDesc);
            } else if (record.getFaultDescription() != null && !record.getFaultDescription().trim().isEmpty()) {
                faultDesc = record.getFaultDescription().trim();
                log.info("使用faultDescription作为故障描述: [{}]", faultDesc);
            } else {
                faultDesc = "";
                log.info("没有故障描述");
            }
        } else if ("3".equals(testResult) || "无此设备".equals(testResult)) {
            // 无此设备
            normal = "——";
            log.info("判定为无此设备");
        } else {
            log.info("未匹配任何状态，testResult=[{}]，保持空白", testResult);
        }

        String[] result = new String[]{normal, fault, faultDesc};
        log.info("最终结果: [正常={}, 故障={}, 描述={}]", result[0], result[1], result[2]);
        log.info("========== 格式化测试结果完成 ==========");
        return result;
    }

    /**
     * 创建带合并单元格的消防测试表格
     *
     * @param rows         表格行数据
     * @param level1Merges 一级合并信息
     * @param level2Merges 二级合并信息
     * @return 表格数据
     */
    private com.deepoove.poi.data.TableRenderData createFireTestTableWithMerge(
            java.util.List<String[]> rows,
            java.util.List<int[]> level1Merges,
            java.util.List<int[]> level2Merges) {

        if (rows.size() <= 2) {
            return null;
        }

        // 转换为RowRenderData
        java.util.List<com.deepoove.poi.data.RowRenderData> tableRows = new java.util.ArrayList<>();
        for (String[] row : rows) {
            tableRows.add(com.deepoove.poi.data.Rows.of(row).create());
        }

        // 创建表格
        com.deepoove.poi.data.TableRenderData table = com.deepoove.poi.data.Tables.ofPercentWidth("90%")
                .border(com.deepoove.poi.data.style.BorderStyle.DEFAULT)
                .create();
        table.setRows(tableRows);

        // 设置合并规则
        com.deepoove.poi.data.MergeCellRule.MergeCellRuleBuilder mergeBuilder =
                com.deepoove.poi.data.MergeCellRule.builder();

        // 合并表头
        // 第一行第一列（测试项目）合并到第二行第一列
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 0),
                com.deepoove.poi.data.MergeCellRule.Grid.of(1, 0)
        );

        // 第一行第二列（测试内容）合并到第二行第三列
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 1),
                com.deepoove.poi.data.MergeCellRule.Grid.of(1, 2)
        );

        // 第一行第四列（实测记录）合并到第一行第六列
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 3),
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 5)
        );

        // 合并一级分类
        for (int[] merge : level1Merges) {
            mergeBuilder.map(
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[0], merge[2]),
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[1], merge[2])
            );
        }

        // 合并二级分类
        for (int[] merge : level2Merges) {
            mergeBuilder.map(
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[0], merge[2]),
                    com.deepoove.poi.data.MergeCellRule.Grid.of(merge[1], merge[2])
            );
        }

        table.setMergeRule(mergeBuilder.build());
        return table;
    }

    /**
     * 创建测试记录表格（旧版本，保留用于兼容）
     *
     * @param testRecords 测试记录列表
     * @return 表格数据
     */
    @Deprecated
    private com.deepoove.poi.data.TableRenderData createTestRecordTable(
            java.util.List<com.ruoyi.fire.domain.FireInspection> testRecords) {

        java.util.List<com.deepoove.poi.data.RowRenderData> allRows = new java.util.ArrayList<>();

        // 添加表头
        allRows.add(com.deepoove.poi.data.Rows.of(
                "系统名称", "设备名称", "设备位置", "测试情况", "测试时间", "测试结果"
        ).textBold().bgColor("D9D9D9").rowHeight(0.8).create());

        // 遍历测试记录
        for (int i = 0; i < testRecords.size(); i++) {
            com.ruoyi.fire.domain.FireInspection test = testRecords.get(i);

            // 数据行 - 增加行高
            String statusText = "0".equals(test.getEquipmentStatus()) ? "正常" : "异常";
            String testTime = test.getInspectionTime() != null
                    ? DateUtils.parseDateToStr("yyyy/MM/dd", test.getInspectionTime())
                    : "";

            allRows.add(com.deepoove.poi.data.Rows.of(
                    safeStr(test.getSystemName()),
                    safeStr(test.getEquipmentName()),
                    safeStr(test.getLocation()),
                    safeStr(test.getRemark()),
                    testTime,
                    statusText
            ).rowHeight(1.0).create());  // 增加行高从默认到1.0cm

            // 图片行 - 如果有图片则显示
            if (test.getImages() != null && !test.getImages().isEmpty()) {
                java.util.List<com.deepoove.poi.data.CellRenderData> imageCells = new java.util.ArrayList<>();
                imageCells.add(com.deepoove.poi.data.Cells.of("现场照片").create());

                for (String imgUrl : test.getImages()) {
                    try {
                        String localPath = convertUrlToLocalPath(imgUrl);
                        java.io.File imgFile = new java.io.File(localPath);
                        if (imgFile.exists() && imgFile.isFile()) {
                            imageCells.add(com.deepoove.poi.data.Cells.of(
                                    com.deepoove.poi.data.Pictures.ofLocal(localPath)
                                            .size(120, 90)
                                            .create()
                            ).create());
                        } else {
                            log.warn("图片文件不存在: {}", localPath);
                            imageCells.add(com.deepoove.poi.data.Cells.of("").create());
                        }
                    } catch (Exception e) {
                        log.warn("处理图片失败: {} - {}", imgUrl, e.getMessage());
                        imageCells.add(com.deepoove.poi.data.Cells.of("").create());
                    }
                }

                // 填充空单元格到6列
                while (imageCells.size() < 6) {
                    imageCells.add(com.deepoove.poi.data.Cells.of("").create());
                }
                // 如果超过6列，截取前6个
                if (imageCells.size() > 6) {
                    imageCells = imageCells.subList(0, 6);
                }

                com.deepoove.poi.data.RowRenderData imageRow = new com.deepoove.poi.data.RowRenderData();
                imageRow.setCells(imageCells);
                allRows.add(imageRow);
            }

            // 设备之间添加空行分隔（除了最后一个）- 增加空行高度
            if (i < testRecords.size() - 1) {
                allRows.add(com.deepoove.poi.data.Rows.of("", "", "", "", "", "").rowHeight(0.8).create());  // 增加空行高度从0.5到0.8cm
            }
        }

        // 创建表格
        com.deepoove.poi.data.TableRenderData table = com.deepoove.poi.data.Tables.ofPercentWidth("90%")
                .border(com.deepoove.poi.data.style.BorderStyle.DEFAULT)
                .create();
        table.setRows(allRows);

        return table;
    }

    /**
     * 获取已完成的故障维修记录
     *
     * @param companyId 公司ID
     * @param startTime 开始时间（维保任务开始时间）
     * @return 已完成的维修记录列表
     */
    private java.util.List<com.ruoyi.fire.domain.FireFaultRepair> getCompletedRepairRecords(Long companyId, Date startTime) {
        if (companyId == null) {
            return new java.util.ArrayList<>();
        }

        // 构建查询条件
        com.ruoyi.fire.domain.FireFaultRepair query = new com.ruoyi.fire.domain.FireFaultRepair();
        query.setCompanyId(companyId);
        query.setRepairStatus("2");  // 2=已完成

        // 查询所有已完成的维修记录
        java.util.List<com.ruoyi.fire.domain.FireFaultRepair> allRepairs = 
                fireFaultRepairService.selectFireFaultRepairList(query);

        // 如果有开始时间，过滤出开始时间之后完成的维修记录
        if (startTime != null) {
            java.util.List<com.ruoyi.fire.domain.FireFaultRepair> filteredRepairs = new java.util.ArrayList<>();
            for (com.ruoyi.fire.domain.FireFaultRepair repair : allRepairs) {
                if (repair.getCompleteTime() != null && !repair.getCompleteTime().before(startTime)) {
                    filteredRepairs.add(repair);
                }
            }
            return filteredRepairs;
        }

        return allRepairs;
    }

    /**
     * 创建故障维修记录表格
     *
     * @param repairs 维修记录列表
     * @return 表格数据
     */
    private com.deepoove.poi.data.TableRenderData createRepairRecordTable(
            java.util.List<com.ruoyi.fire.domain.FireFaultRepair> repairs) {

        if (repairs == null || repairs.isEmpty()) {
            return null;
        }

        java.util.List<com.deepoove.poi.data.RowRenderData> allRows = new java.util.ArrayList<>();

        // 添加表头（两行）
        // 第一行：故障情况 | 故障维修情况 | 故障排除确认
        allRows.add(com.deepoove.poi.data.Rows.of(
                "故障情况", "", "", "故障维修情况", "", "", "故障排除确认"
        ).textBold().bgColor("D9D9D9").rowHeight(0.8).create());

        // 第二行：发现时间 | 发现人 | 故障情况描述 | 维修时间 | 维修人员 | 维修方法 | （空）
        allRows.add(com.deepoove.poi.data.Rows.of(
                "发现时间", "发现人", "故障情况描述", "维修时间", "维修人员", "维修方法", ""
        ).textBold().bgColor("D9D9D9").rowHeight(0.8).create());

        // 遍历维修记录，添加数据行
        for (com.ruoyi.fire.domain.FireFaultRepair repair : repairs) {
            String foundTime = repair.getFoundTime() != null
                    ? DateUtils.parseDateToStr("yyyy/MM/dd", repair.getFoundTime())
                    : "";
            String completeTime = repair.getCompleteTime() != null
                    ? DateUtils.parseDateToStr("yyyy/MM/dd", repair.getCompleteTime())
                    : "";

            allRows.add(com.deepoove.poi.data.Rows.of(
                    foundTime,
                    safeStr(repair.getReporterName()),
                    safeStr(repair.getFaultDescription()),
                    completeTime,
                    safeStr(repair.getRepairPerson()),
                    safeStr(repair.getRepairDescription()),
                    ""  // 故障排除确认列留空，供手工填写
            ).rowHeight(1.0).create());
        }

        // 创建表格
        com.deepoove.poi.data.TableRenderData table = com.deepoove.poi.data.Tables.ofPercentWidth("90%")
                .border(com.deepoove.poi.data.style.BorderStyle.DEFAULT)
                .create();
        table.setRows(allRows);

        // 设置合并规则
        com.deepoove.poi.data.MergeCellRule.MergeCellRuleBuilder mergeBuilder =
                com.deepoove.poi.data.MergeCellRule.builder();

        // 第一行：故障情况（列0-2）
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 0),
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 2)
        );

        // 第一行：故障维修情况（列3-5）
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 3),
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 5)
        );

        // 第一行：故障排除确认（列6）- 合并到第二行
        mergeBuilder.map(
                com.deepoove.poi.data.MergeCellRule.Grid.of(0, 6),
                com.deepoove.poi.data.MergeCellRule.Grid.of(1, 6)
        );

        table.setMergeRule(mergeBuilder.build());

        return table;
    }

    /**
     * 创建建筑消防设施维护保养记录表
     *
     * @param maintenanceRecords 保养记录列表
     * @return 表格数据
     */
    private com.deepoove.poi.data.TableRenderData createMaintenanceRecordTable(
            java.util.List<com.ruoyi.fire.domain.FireInspection> maintenanceRecords) {

        if (maintenanceRecords == null || maintenanceRecords.isEmpty()) {
            return null;
        }

        java.util.List<com.deepoove.poi.data.RowRenderData> allRows = new java.util.ArrayList<>();

        // 添加表头
        allRows.add(com.deepoove.poi.data.Rows.of(
                "作业日期", "保养项目", "保养完成情况"
        ).textBold().bgColor("D9D9D9").rowHeight(0.8).create());

        // 按日期分组保养记录
        java.util.Map<String, java.util.List<com.ruoyi.fire.domain.FireInspection>> groupedByDate = 
                new java.util.LinkedHashMap<>();
        
        for (com.ruoyi.fire.domain.FireInspection record : maintenanceRecords) {
            String dateStr = record.getInspectionTime() != null
                    ? DateUtils.parseDateToStr("yyyy/MM/dd", record.getInspectionTime())
                    : "";
            
            if (!groupedByDate.containsKey(dateStr)) {
                groupedByDate.put(dateStr, new java.util.ArrayList<>());
            }
            groupedByDate.get(dateStr).add(record);
        }

        // 记录需要合并的单元格
        java.util.List<int[]> dateMerges = new java.util.ArrayList<>();
        int currentRow = 1; // 从第2行开始（第1行是表头）

        // 遍历每个日期组
        for (java.util.Map.Entry<String, java.util.List<com.ruoyi.fire.domain.FireInspection>> entry : groupedByDate.entrySet()) {
            String dateStr = entry.getKey();
            java.util.List<com.ruoyi.fire.domain.FireInspection> records = entry.getValue();

            int dateStartRow = currentRow;

            // 遍历该日期下的所有保养记录
            for (int i = 0; i < records.size(); i++) {
                com.ruoyi.fire.domain.FireInspection record = records.get(i);

                // 只在第一行显示日期
                String displayDate = (i == 0) ? dateStr : "";

                // 保养项目：设备名称 + 位置
                String maintenanceItem = safeStr(record.getEquipmentName());
                if (record.getLocation() != null && !record.getLocation().isEmpty()) {
                    maintenanceItem += "（" + record.getLocation() + "）";
                }

                // 保养完成情况：根据设备状态显示
                String completionStatus = "0".equals(record.getEquipmentStatus()) ? "正常" : "异常";
                if (record.getRemark() != null && !record.getRemark().isEmpty()) {
                    completionStatus += "，" + record.getRemark();
                }

                allRows.add(com.deepoove.poi.data.Rows.of(
                        displayDate,
                        maintenanceItem,
                        completionStatus
                ).rowHeight(1.0).create());

                currentRow++;
            }

            // 如果该日期有多条记录，记录需要合并的日期列
            if (records.size() > 1) {
                dateMerges.add(new int[]{dateStartRow, currentRow - 1, 0});
            }
        }

        // 创建表格
        com.deepoove.poi.data.TableRenderData table = com.deepoove.poi.data.Tables.ofPercentWidth("90%")
                .border(com.deepoove.poi.data.style.BorderStyle.DEFAULT)
                .create();
        table.setRows(allRows);

        // 设置合并规则（合并日期列）
        if (!dateMerges.isEmpty()) {
            com.deepoove.poi.data.MergeCellRule.MergeCellRuleBuilder mergeBuilder =
                    com.deepoove.poi.data.MergeCellRule.builder();

            for (int[] merge : dateMerges) {
                mergeBuilder.map(
                        com.deepoove.poi.data.MergeCellRule.Grid.of(merge[0], merge[2]),
                        com.deepoove.poi.data.MergeCellRule.Grid.of(merge[1], merge[2])
                );
            }

            table.setMergeRule(mergeBuilder.build());
        }

        return table;
    }
}
