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
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireInspection;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.domain.FireCompany;
import com.ruoyi.fire.domain.FireSystemType;
import com.ruoyi.fire.service.IFireInspectionService;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireSystemTypeService;

/**
 * 巡检登记Controller
 * 
 * @author ruoyi
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
    private IFireSystemTypeService systemTypeService;

    @Autowired
    private com.ruoyi.fire.service.IFireEquipmentService equipmentService;

    @RequiresPermissions("fire:inspection:view")
    @GetMapping()
    public String inspection(ModelMap mmap) {
        // 获取所有公司供筛选
        List<FireCompany> companies = companyService.selectCompanyAll();
        mmap.put("companies", companies);
        return prefix + "/inspection";
    }

    /**
     * 查询巡检登记列表
     */
    @RequiresPermissions("fire:inspection:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireInspection inspection) {
        startPage();
        List<FireInspection> list = inspectionService.selectFireInspectionList(inspection);
        return getDataTable(list);
    }

    /**
     * 导出巡检登记列表
     */
    @RequiresPermissions("fire:inspection:export")
    @Log(title = "巡检登记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireInspection inspection) {
        List<FireInspection> list = inspectionService.selectFireInspectionList(inspection);
        ExcelUtil<FireInspection> util = new ExcelUtil<FireInspection>(FireInspection.class);
        return util.exportExcel(list, "巡检登记数据");
    }

    /**
     * 新增巡检登记
     */
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        // 获取所有公司
        List<FireCompany> companies = companyService.selectCompanyAll();
        mmap.put("companies", companies);
        // 获取所有建筑
        List<FireBuilding> buildings = buildingService.selectBuildingAll();
        mmap.put("buildings", buildings);
        // 获取所有系统类型
        List<FireSystemType> systemTypes = systemTypeService.selectFireSystemTypeAll();
        mmap.put("systemTypes", systemTypes);
        return prefix + "/add_new";  // 使用新页面
    }

    /**
     * 新增保存巡检登记
     */
    @RequiresPermissions("fire:inspection:add")
    @Log(title = "巡检登记", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireInspection inspection) {
        // 设置巡检人员
        inspection.setInspectorId(ShiroUtils.getUserId());
        inspection.setInspectorName(ShiroUtils.getLoginName());
        if (inspection.getInspectionTime() == null) {
            inspection.setInspectionTime(new Date());
        }
        inspection.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(inspectionService.insertFireInspection(inspection));
    }

    /**
     * 修改巡检登记
     */
    @RequiresPermissions("fire:inspection:edit")
    @GetMapping("/edit/{inspectionId}")
    public String edit(@PathVariable("inspectionId") Long inspectionId, ModelMap mmap) {
        FireInspection inspection = inspectionService.selectFireInspectionById(inspectionId);
        mmap.put("inspection", inspection);
        // 获取所有公司
        List<FireCompany> companies = companyService.selectCompanyAll();
        mmap.put("companies", companies);
        // 获取所有建筑
        List<FireBuilding> buildings = buildingService.selectBuildingAll();
        mmap.put("buildings", buildings);
        // 获取所有系统类型
        List<FireSystemType> systemTypes = systemTypeService.selectFireSystemTypeAll();
        mmap.put("systemTypes", systemTypes);
        return prefix + "/edit_new";  // 使用新页面
    }

    /**
     * 修改保存巡检登记
     */
    @RequiresPermissions("fire:inspection:edit")
    @Log(title = "巡检登记", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireInspection inspection) {
        inspection.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(inspectionService.updateFireInspection(inspection));
    }

    /**
     * 删除巡检登记
     */
    @RequiresPermissions("fire:inspection:remove")
    @Log(title = "巡检登记", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(inspectionService.deleteFireInspectionByIds(ids));
    }

    /**
     * 查看巡检详情
     */
    @RequiresPermissions("fire:inspection:list")
    @GetMapping("/detail/{inspectionId}")
    public String detail(@PathVariable("inspectionId") Long inspectionId, ModelMap mmap) {
        FireInspection inspection = inspectionService.selectFireInspectionById(inspectionId);
        mmap.put("inspection", inspection);
        return prefix + "/detail";
    }

    /**
     * 根据公司ID获取建筑列表
     */
    @GetMapping("/buildings/{companyId}")
    @ResponseBody
    public AjaxResult getBuildingsByCompanyId(@PathVariable("companyId") Long companyId) {
        FireBuilding query = new FireBuilding();
        query.setCompanyId(companyId);
        List<FireBuilding> buildings = buildingService.selectBuildingList(query);
        return AjaxResult.success(buildings);
    }

    /**
     * 根据建筑ID获取设备列表
     */
    @GetMapping("/equipments/{buildingId}")
    @ResponseBody
    public AjaxResult getEquipmentsByBuildingId(@PathVariable("buildingId") Long buildingId) {
        com.ruoyi.fire.domain.FireEquipment query = new com.ruoyi.fire.domain.FireEquipment();
        query.setBuildingId(buildingId);
        List<com.ruoyi.fire.domain.FireEquipment> list = equipmentService.selectEquipmentList(query);
        return AjaxResult.success(list);
    }
}
