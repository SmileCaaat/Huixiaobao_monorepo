package com.ruoyi.web.controller.fire;

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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireBuilding;
import com.ruoyi.fire.service.IFireBuildingService;

/**
 * 建筑信息操作处理
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/fire/building")
public class FireBuildingController extends BaseController {
    private String prefix = "fire/building";

    @Autowired
    private IFireBuildingService buildingService;

    @RequiresPermissions("fire:building:view")
    @GetMapping()
    public String building() {
        return prefix + "/building";
    }

    @RequiresPermissions("fire:building:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireBuilding building) {
        startPage();
        List<FireBuilding> list = buildingService.selectBuildingList(building);
        return getDataTable(list);
    }

    @Log(title = "建筑管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("fire:building:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireBuilding building) {
        List<FireBuilding> list = buildingService.selectBuildingList(building);
        ExcelUtil<FireBuilding> util = new ExcelUtil<FireBuilding>(FireBuilding.class);
        return util.exportExcel(list, "建筑信息数据");
    }

    @RequiresPermissions("fire:building:remove")
    @Log(title = "建筑管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(buildingService.deleteBuildingByIds(ids));
    }

    /**
     * 新增建筑
     */
    @RequiresPermissions("fire:building:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存建筑
     */
    @RequiresPermissions("fire:building:add")
    @Log(title = "建筑管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireBuilding building) {
        if (building.getCompanyId() == null) {
            return error("请选择所属客户");
        }
        if (!buildingService.checkBuildingCodeUnique(building)) {
            return error("新增建筑'" + building.getBuildingName() + "'失败，建筑编码已存在");
        } else if (!buildingService.checkBuildingNameUnique(building)) {
            return error("新增建筑'" + building.getBuildingName() + "'失败，同一客户下建筑名称不能重复");
        }
        building.setCreateBy(getLoginName());
        return toAjax(buildingService.insertBuilding(building));
    }

    /**
     * 修改建筑
     */
    @RequiresPermissions("fire:building:edit")
    @GetMapping("/edit/{buildingId}")
    public String edit(@PathVariable("buildingId") Long buildingId, ModelMap mmap) {
        mmap.put("building", buildingService.selectBuildingById(buildingId));
        return prefix + "/edit";
    }

    /**
     * 修改保存建筑
     */
    @RequiresPermissions("fire:building:edit")
    @Log(title = "建筑管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireBuilding building) {
        if (building.getCompanyId() == null) {
            return error("请选择所属客户");
        }
        if (!buildingService.checkBuildingCodeUnique(building)) {
            return error("修改建筑'" + building.getBuildingName() + "'失败，建筑编码已存在");
        } else if (!buildingService.checkBuildingNameUnique(building)) {
            return error("修改建筑'" + building.getBuildingName() + "'失败，同一客户下建筑名称不能重复");
        }
        building.setUpdateBy(getLoginName());
        return toAjax(buildingService.updateBuilding(building));
    }

    /**
     * 校验建筑编码
     */
    @PostMapping("/checkBuildingCodeUnique")
    @ResponseBody
    public boolean checkBuildingCodeUnique(FireBuilding building) {
        return buildingService.checkBuildingCodeUnique(building);
    }

    /**
     * 校验建筑名称
     */
    @PostMapping("/checkBuildingNameUnique")
    @ResponseBody
    public boolean checkBuildingNameUnique(FireBuilding building) {
        return buildingService.checkBuildingNameUnique(building);
    }

    /**
     * 获取所有建筑列表（支持按公司过滤）
     */
    @GetMapping("/all")
    @ResponseBody
    public AjaxResult all(FireBuilding building) {
        List<FireBuilding> list = buildingService.selectBuildingList(building);
        return AjaxResult.success(list);
    }

    /**
     * 根据建筑ID获取建筑详情（返回JSON，用于前端联动获取所属客户信息）
     */
    @GetMapping("/{buildingId}")
    @ResponseBody
    public AjaxResult detail(@PathVariable("buildingId") Long buildingId) {
        FireBuilding building = buildingService.selectBuildingById(buildingId);
        return AjaxResult.success(building);
    }
}
