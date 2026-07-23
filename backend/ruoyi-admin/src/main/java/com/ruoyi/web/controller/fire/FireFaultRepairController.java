package com.ruoyi.web.controller.fire;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.ruoyi.common.enums.RepairStatus;
import com.ruoyi.common.enums.UrgencyLevel;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireFaultRepair;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireEquipmentService;
import com.ruoyi.fire.service.IFireFaultRepairService;
import com.ruoyi.fire.service.IFireSystemTypeService;

/**
 * 故障报修管理。
 */
@Controller
@RequestMapping("/fire/repair")
public class FireFaultRepairController extends BaseController {
    private final String prefix = "fire/repair";

    @Autowired
    private IFireFaultRepairService fireFaultRepairService;

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireSystemTypeService systemTypeService;

    @Autowired
    private IFireEquipmentService equipmentService;

    @RequiresPermissions("fire:repair:view")
    @GetMapping()
    public String repair(ModelMap mmap) {
        mmap.put("urgencyLevels", UrgencyLevel.values());
        mmap.put("repairStatuses", RepairStatus.values());
        return prefix + "/repair";
    }

    @RequiresPermissions("fire:repair:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireFaultRepair fireFaultRepair) {
        startPage();
        List<FireFaultRepair> list = fireFaultRepairService.selectFireFaultRepairList(fireFaultRepair);
        return getDataTable(list);
    }

    @RequiresPermissions("fire:repair:export")
    @Log(title = "故障报修", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireFaultRepair fireFaultRepair) {
        List<FireFaultRepair> list = fireFaultRepairService.selectFireFaultRepairList(fireFaultRepair);
        ExcelUtil<FireFaultRepair> util = new ExcelUtil<>(FireFaultRepair.class);
        return util.exportExcel(list, "故障报修");
    }

    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("companies", companyService.selectCompanyAll());
        mmap.put("systemTypes", systemTypeService.selectFireSystemTypeAll());
        mmap.put("equipments", equipmentService.selectEquipmentAll());
        mmap.put("urgencyLevels", UrgencyLevel.values());
        mmap.put("equipmentNames", com.ruoyi.fire.enums.FireEquipmentCategory.allLabels());
        return prefix + "/add";
    }

    @RequiresPermissions("fire:repair:add")
    @Log(title = "故障报修", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(FireFaultRepair fireFaultRepair) {
        try {
            fireFaultRepair.setCreateBy(ShiroUtils.getLoginName());
            return toAjax(fireFaultRepairService.insertFireFaultRepair(fireFaultRepair));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/edit/{repairId}")
    public String edit(@PathVariable("repairId") Long repairId, ModelMap mmap) {
        mmap.put("repair", getRepair(repairId));
        mmap.put("companies", companyService.selectCompanyAll());
        mmap.put("systemTypes", systemTypeService.selectFireSystemTypeAll());
        mmap.put("equipments", equipmentService.selectEquipmentAll());
        mmap.put("urgencyLevels", UrgencyLevel.values());
        mmap.put("equipmentNames", com.ruoyi.fire.enums.FireEquipmentCategory.allLabels());
        return prefix + "/edit";
    }

    @RequiresPermissions("fire:repair:edit")
    @Log(title = "故障报修", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(FireFaultRepair fireFaultRepair) {
        try {
            fireFaultRepair.setUpdateBy(ShiroUtils.getLoginName());
            return toAjax(fireFaultRepairService.updateFireFaultRepair(fireFaultRepair));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("fire:repair:remove")
    @Log(title = "故障报修", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(fireFaultRepairService.deleteFireFaultRepairByIds(ids));
    }

    @GetMapping("/detail/{repairId}")
    public String detail(@PathVariable("repairId") Long repairId, ModelMap mmap) {
        mmap.put("repair", getRepair(repairId));
        return prefix + "/detail";
    }

    /**
     * 复用 fire:repair:accept 权限，避免额外权限初始化。
     */
    @RequiresPermissions("fire:repair:accept")
    @GetMapping("/dispatch/{repairId}")
    public String dispatch(@PathVariable("repairId") Long repairId, ModelMap mmap) {
        FireFaultRepair repair = getRepair(repairId);
        mmap.put("repair", repair);
        return prefix + "/dispatch";
    }

    @RequiresPermissions("fire:repair:accept")
    @Log(title = "派发报修", businessType = BusinessType.UPDATE)
    @PostMapping("/dispatch")
    @ResponseBody
    public AjaxResult dispatchSave(Long repairId) {
        try {
            return toAjax(fireFaultRepairService.dispatchRepairToCurrentUser(repairId));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("fire:repair:accept")
    @GetMapping("/accept/{repairId}")
    public String accept(@PathVariable("repairId") Long repairId, ModelMap mmap) {
        return dispatch(repairId, mmap);
    }

    @RequiresPermissions("fire:repair:accept")
    @Log(title = "接受报修", businessType = BusinessType.UPDATE)
    @PostMapping("/accept")
    @ResponseBody
    public AjaxResult acceptSave(Long repairId, String repairPerson, String repairPhone) {
        return toAjax(fireFaultRepairService.acceptRepair(repairId, repairPerson, repairPhone));
    }

    @RequiresPermissions("fire:repair:start")
    @Log(title = "开始处理报修", businessType = BusinessType.UPDATE)
    @PostMapping("/start/{repairId}")
    @ResponseBody
    public AjaxResult start(@PathVariable("repairId") Long repairId) {
        return toAjax(fireFaultRepairService.startRepair(repairId));
    }

    @GetMapping("/complete/{repairId}")
    public String complete(@PathVariable("repairId") Long repairId, ModelMap mmap) {
        mmap.put("repair", getRepair(repairId));
        return prefix + "/complete";
    }

    @RequiresPermissions("fire:repair:complete")
    @Log(title = "完成报修", businessType = BusinessType.UPDATE)
    @PostMapping("/complete")
    @ResponseBody
    public AjaxResult completeSave(FireFaultRepair fireFaultRepair) {
        fireFaultRepair.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(fireFaultRepairService.completeRepair(fireFaultRepair));
    }

    private FireFaultRepair getRepair(Long repairId) {
        return fireFaultRepairService.selectFireFaultRepairById(repairId);
    }
}
