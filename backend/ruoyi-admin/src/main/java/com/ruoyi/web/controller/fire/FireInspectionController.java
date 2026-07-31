package com.ruoyi.web.controller.fire;

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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireInspection;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireDataPermissionService;
import com.ruoyi.fire.service.IFireInspectionService;
import com.ruoyi.fire.service.IFireMaintenanceTemplateCategoryService;

/**
 * 巡检测试Controller
 */
@Controller
@RequestMapping("/fire/inspection")
public class FireInspectionController extends BaseController {

    private String prefix = "fire/inspection";

    @Autowired
    private IFireInspectionService inspectionService;

    @Autowired
    private IFireBuildingService buildingService;

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireMaintenanceTemplateCategoryService templateCategoryService;

    @Autowired
    private IFireDataPermissionService fireDataPermissionService;

    @RequiresPermissions("fire:inspection:view")
    @GetMapping()
    public String inspection(ModelMap mmap) {
        List<FireCompany> companies = companyService.selectCompanyAll();
        mmap.put("companies", companies);
        mmap.put("systemTypes", templateCategoryService.listInspectionLevel1Categories());
        return prefix + "/inspection";
    }

    @RequiresPermissions("fire:inspection:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireInspection inspection) {
        fireDataPermissionService.applyInspectionListScope(inspection, ShiroUtils.getSysUser());
        startPage();
        List<FireInspection> list = inspectionService.selectFireInspectionList(inspection);
        return getDataTable(list);
    }

    @RequiresPermissions("fire:inspection:export")
    @Log(title = "巡检测试", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireInspection inspection) {
        fireDataPermissionService.applyInspectionListScope(inspection, ShiroUtils.getSysUser());
        List<FireInspection> list = inspectionService.selectFireInspectionList(inspection);
        ExcelUtil<FireInspection> util = new ExcelUtil<FireInspection>(FireInspection.class);
        return util.exportExcel(list, "巡检测试数据");
    }

    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("companies", companyService.selectCompanyAll());
        mmap.put("systemTypes", templateCategoryService.listInspectionLevel1Categories());
        return prefix + "/add_new";
    }

    @RequiresPermissions("fire:inspection:add")
    @Log(title = "巡检测试", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireInspection inspection) {
        try {
            fireDataPermissionService.assertCanAccessCompany(ShiroUtils.getSysUser(), inspection.getCompanyId());
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
        inspection.setInspectorId(ShiroUtils.getUserId());
        inspection.setInspectorName(resolveInspectorName());
        if (inspection.getInspectionTime() == null) {
            inspection.setInspectionTime(new Date());
        }
        inspection.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(inspectionService.insertFireInspection(inspection));
    }

    @RequiresPermissions("fire:inspection:edit")
    @GetMapping("/edit/{inspectionId}")
    public String edit(@PathVariable("inspectionId") Long inspectionId, ModelMap mmap) {
        FireInspection inspection = inspectionService.selectFireInspectionById(inspectionId);
        fireDataPermissionService.assertCanAccessInspection(ShiroUtils.getSysUser(), inspection);
        mmap.put("inspection", inspection);
        mmap.put("companies", companyService.selectCompanyAll());
        mmap.put("systemTypes", templateCategoryService.listInspectionLevel1Categories());
        if (inspection != null && inspection.getCategoryKey() != null) {
            mmap.put("equipmentTypes",
                    templateCategoryService.listEquipmentsByCategoryKey(inspection.getCategoryKey()));
        }
        return prefix + "/edit_new";
    }

    @RequiresPermissions("fire:inspection:edit")
    @Log(title = "巡检测试", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireInspection inspection) {
        FireInspection existing = inspectionService.selectFireInspectionById(inspection.getInspectionId());
        try {
            fireDataPermissionService.assertCanAccessInspection(ShiroUtils.getSysUser(), existing);
            fireDataPermissionService.assertCanAccessCompany(ShiroUtils.getSysUser(), inspection.getCompanyId());
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
        inspection.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(inspectionService.updateFireInspection(inspection));
    }

    @RequiresPermissions("fire:inspection:remove")
    @Log(title = "巡检测试", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return AjaxResult.error("请选择要删除的数据");
        }
        for (String id : ids.split(",")) {
            FireInspection existing = inspectionService.selectFireInspectionById(Long.parseLong(id.trim()));
            try {
                fireDataPermissionService.assertCanAccessInspection(ShiroUtils.getSysUser(), existing);
            } catch (ServiceException e) {
                return AjaxResult.error(e.getMessage());
            }
        }
        return toAjax(inspectionService.deleteFireInspectionByIds(ids));
    }

    @RequiresPermissions("fire:inspection:list")
    @GetMapping("/detail/{inspectionId}")
    public String detail(@PathVariable("inspectionId") Long inspectionId, ModelMap mmap) {
        FireInspection inspection = inspectionService.selectFireInspectionById(inspectionId);
        fireDataPermissionService.assertCanAccessInspection(ShiroUtils.getSysUser(), inspection);
        mmap.put("inspection", inspection);
        return prefix + "/detail";
    }

    @GetMapping("/buildings/{companyId}")
    @ResponseBody
    public AjaxResult getBuildingsByCompanyId(@PathVariable("companyId") Long companyId) {
        try {
            fireDataPermissionService.assertCanAccessCompany(ShiroUtils.getSysUser(), companyId);
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
        FireBuilding query = new FireBuilding();
        query.setCompanyId(companyId);
        List<FireBuilding> buildings = buildingService.selectBuildingList(query);
        return AjaxResult.success(buildings);
    }

    /**
     * 一级系统类目（与消防维护模板一致）
     */
    @GetMapping("/systemTypes")
    @ResponseBody
    public AjaxResult systemTypes() {
        return AjaxResult.success(templateCategoryService.listInspectionLevel1Categories());
    }

    /**
     * 指定一级类目下的设备（与消防维护模板一致）
     */
    @GetMapping("/equipmentTypes")
    @ResponseBody
    public AjaxResult equipmentTypes(String categoryKey) {
        return AjaxResult.success(templateCategoryService.listEquipmentsByCategoryKey(categoryKey));
    }

    private String resolveInspectorName() {
        try {
            String userName = ShiroUtils.getSysUser().getUserName();
            if (userName != null && !userName.trim().isEmpty()) {
                return userName;
            }
        } catch (Exception ignored) {
            // fallback
        }
        return ShiroUtils.getLoginName();
    }
}
