package com.ruoyi.web.controller.fire;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireContract;
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.domain.FireUserCompany;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireContractService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 维保客户管理 Controller
 */
@Controller
@RequestMapping("/fire/company")
public class FireCompanyController extends BaseController {

    private final String prefix = "fire/company";

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IFireBuildingService buildingService;

    @Autowired
    private IFireContractService contractService;

    @Autowired
    private IFireMaintenanceTaskService maintenanceTaskService;

    @RequiresPermissions("fire:company:view")
    @GetMapping()
    public String company() {
        return prefix + "/company";
    }

    /**
     * 原公司列表接口（保留，避免影响旧逻辑）
     */
    @RequiresPermissions("fire:company:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireCompany company) {
        startPage();
        List<FireCompany> list = companyService.selectFireCompanyList(company);
        return getDataTable(list);
    }

    /**
     * 维保客户管理聚合列表（新页面使用）
     */
    @RequiresPermissions("fire:company:list")
    @PostMapping("/customerList")
    @ResponseBody
    public TableDataInfo customerList(FireCompany company) {
        startPage();
        List<FireCompany> list = companyService.selectCustomerManageList(company);
        return getDataTable(list);
    }

    /**
     * 原导出接口（保留）
     */
    @RequiresPermissions("fire:company:export")
    @Log(title = "维保客户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireCompany company) {
        List<FireCompany> list = companyService.selectFireCompanyList(company);
        ExcelUtil<FireCompany> util = new ExcelUtil<>(FireCompany.class);
        return util.exportExcel(list, "维保客户数据");
    }

    /**
     * 勾选导出（新页面使用）
     */
    @RequiresPermissions("fire:company:export")
    @Log(title = "维保客户", businessType = BusinessType.EXPORT)
    @PostMapping("/exportSelected")
    @ResponseBody
    public AjaxResult exportSelected(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return AjaxResult.warn("请先勾选需要导出的客户");
        }
        List<FireCompany> list = companyService.selectCustomerManageByIds(convertStrToLongArray(ids));
        ExcelUtil<FireCompany> util = new ExcelUtil<>(FireCompany.class);
        return util.exportExcel(list, "维保客户管理数据");
    }

    /**
     * 新增页面
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 地图选择页面
     */
    @GetMapping("/map")
    public String map() {
        return prefix + "/map";
    }

    /**
     * 新增保存
     */
    @RequiresPermissions("fire:company:add")
    @Log(title = "维保客户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireCompany company) {
        if (!companyService.checkCompanyNameUnique(company)) {
            return error("新增客户'" + company.getCompanyName() + "'失败，客户名称已存在");
        }
        company.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(companyService.insertFireCompany(company));
    }

    /**
     * 编辑页面
     */
    @RequiresPermissions("fire:company:edit")
    @GetMapping("/edit/{companyId}")
    public String edit(@PathVariable("companyId") Long companyId, ModelMap mmap) {
        FireCompany company = companyService.selectFireCompanyById(companyId);
        mmap.put("company", company);

        FireBuilding buildingQuery = new FireBuilding();
        buildingQuery.setCompanyId(companyId);
        List<FireBuilding> buildingList = buildingService.selectBuildingList(buildingQuery);
        mmap.put("buildingList", buildingList);

        return prefix + "/edit";
    }

    /**
     * 编辑保存
     */
    @RequiresPermissions("fire:company:edit")
    @Log(title = "维保客户", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireCompany company) {
        if (!companyService.checkCompanyNameUnique(company)) {
            return error("修改客户'" + company.getCompanyName() + "'失败，客户名称已存在");
        }
        company.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(companyService.updateFireCompany(company));
    }

    /**
     * 作废客户（逻辑删除）
     */
    @RequiresPermissions("fire:company:remove")
    @Log(title = "维保客户", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(companyService.deleteFireCompanyByIds(convertStrToLongArray(ids)));
    }

    /**
     * 分配人员页面
     */
    @RequiresPermissions("fire:company:edit")
    @GetMapping("/assignUsers/{companyId}")
    public String assignUsers(@PathVariable("companyId") Long companyId, ModelMap mmap) {
        FireCompany company = companyService.selectFireCompanyById(companyId);
        mmap.put("company", company);

        List<SysUser> users = userService.selectUserList(new SysUser());
        mmap.put("users", users);

        List<FireUserCompany> assignedUsers = companyService.selectUserListByCompanyId(companyId);
        mmap.put("assignedUsers", assignedUsers);
        return prefix + "/assignUsers";
    }

    /**
     * 保存分配人员
     */
    @RequiresPermissions("fire:company:edit")
    @Log(title = "分配客户人员", businessType = BusinessType.UPDATE)
    @PostMapping("/assignUsers")
    @ResponseBody
    public AjaxResult assignUsersSave(Long companyId, String userIds, String roleType) {
        Long[] userIdArray = convertStrToLongArray(userIds);
        return toAjax(companyService.assignUsers(companyId, userIdArray, roleType, ShiroUtils.getLoginName()));
    }

    /**
     * 客户详情：客户录入信息 + 建筑列表 + 合同列表 + 本年度计划
     */
    @GetMapping("/detail/{companyId}")
    public String detail(@PathVariable("companyId") Long companyId, ModelMap mmap) {
        FireCompany company = companyService.selectFireCompanyById(companyId);
        mmap.put("company", company);

        FireBuilding buildingQuery = new FireBuilding();
        buildingQuery.setCompanyId(companyId);
        List<FireBuilding> buildingList = buildingService.selectBuildingList(buildingQuery);
        mmap.put("buildingList", buildingList);

        FireContract contractQuery = new FireContract();
        contractQuery.setCompanyId(companyId);
        List<FireContract> contractList = contractService.selectFireContractList(contractQuery);
        for (FireContract contract : contractList) {
            contract.setEntryUnit("Admin");
        }
        mmap.put("contractList", contractList);

        List<FireUserCompany> assignedUsers = companyService.selectUserListByCompanyId(companyId);
        mmap.put("assignedUsers", assignedUsers);

        mmap.put("yearPlanList", getCurrentYearPlanList(companyId));
        return prefix + "/detail";
    }

    /**
     * 本年度计划接口
     */
    @GetMapping("/yearPlan/{companyId}")
    @ResponseBody
    public AjaxResult yearPlan(@PathVariable("companyId") Long companyId) {
        return AjaxResult.success(getCurrentYearPlanList(companyId))
                .put("year", LocalDate.now().getYear())
                .put("companyId", companyId);
    }

    /**
     * 全量公司（下拉选择）
     */
    @RequestMapping("/all")
    @ResponseBody
    public AjaxResult all() {
        List<FireCompany> list = companyService.selectCompanyAll();
        return AjaxResult.success(list);
    }

    /**
     * 当前用户可检客户列表（API）
     */
    @GetMapping("/api/myList")
    @ResponseBody
    public AjaxResult myCompanyList() {
        Long userId = ShiroUtils.getUserId();
        List<FireCompany> list = companyService.selectCompanyListByUserId(userId);
        return AjaxResult.success(list);
    }

    /**
     * 字符串转Long数组
     */
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

    private List<FireMaintenanceTask> getCurrentYearPlanList(Long companyId) {
        FireMaintenanceTask query = new FireMaintenanceTask();
        query.setCompanyId(companyId);
        List<FireMaintenanceTask> taskList = maintenanceTaskService.selectFireMaintenanceTaskList(query);
        List<FireMaintenanceTask> currentYearList = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (FireMaintenanceTask task : taskList) {
            if (isCurrentYearTask(task, currentYear)) {
                currentYearList.add(task);
            }
        }
        currentYearList.sort((left, right) -> comparePlanTimeDesc(left.getPlanStartTime(), right.getPlanStartTime()));
        return currentYearList;
    }

    private boolean isCurrentYearTask(FireMaintenanceTask task, int currentYear) {
        if (task == null) {
            return false;
        }
        if (task.getPeriodYear() != null && task.getPeriodYear() == currentYear) {
            return true;
        }
        Integer startYear = getYear(task.getPlanStartTime());
        Integer endYear = getYear(task.getPlanEndTime());
        return (startYear != null && startYear == currentYear)
                || (endYear != null && endYear == currentYear);
    }

    private Integer getYear(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).getYear();
    }

    private int comparePlanTimeDesc(Date left, Date right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return right.compareTo(left);
    }
}
