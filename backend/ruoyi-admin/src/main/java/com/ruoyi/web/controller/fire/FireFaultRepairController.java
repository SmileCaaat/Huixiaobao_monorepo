package com.ruoyi.web.controller.fire;

import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.RepairStatus;
import com.ruoyi.common.enums.UrgencyLevel;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fire.domain.FireFaultRepair;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireEquipmentService;
import com.ruoyi.fire.service.IFireDataPermissionService;
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
    private IFireDataPermissionService fireDataPermissionService;

    @Autowired
    private IFireCompanyService companyService;

    @Autowired
    private IFireSystemTypeService systemTypeService;

    @Autowired
    private IFireEquipmentService equipmentService;

    @RequiresPermissions("fire:repair:view")
    @GetMapping()
    public String repair(ModelMap mmap) {
        SysUser user = ShiroUtils.getSysUser();
        boolean workbenchMode = fireDataPermissionService.isRepairEmployeeWorkbench(user);
        mmap.put("urgencyLevels", UrgencyLevel.values());
        mmap.put("repairStatuses", RepairStatus.values());
        mmap.put("currentUserId", user != null ? user.getUserId() : null);
        mmap.put("workbenchMode", workbenchMode);
        mmap.put("defaultWorkbenchCategory", workbenchMode ? "assignedPending" : "");
        return prefix + "/repair";
    }

    @RequiresPermissions("fire:repair:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireFaultRepair fireFaultRepair) {
        SysUser user = ShiroUtils.getSysUser();
        String category = extractWorkbenchCategory(fireFaultRepair);
        fireDataPermissionService.prepareRepairListQuery(fireFaultRepair, user, category);
        startPage();
        List<FireFaultRepair> list = fireFaultRepairService.selectFireFaultRepairList(fireFaultRepair);
        return getDataTable(list);
    }

    @RequiresPermissions("fire:repair:export")
    @Log(title = "故障报修", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireFaultRepair fireFaultRepair) {
        SysUser user = ShiroUtils.getSysUser();
        String category = extractWorkbenchCategory(fireFaultRepair);
        fireDataPermissionService.prepareRepairListQuery(fireFaultRepair, user, category);
        List<FireFaultRepair> list = fireFaultRepairService.selectFireFaultRepairList(fireFaultRepair);
        ExcelUtil<FireFaultRepair> util = new ExcelUtil<>(FireFaultRepair.class);
        return util.exportExcel(list, "故障报修");
    }

    @GetMapping("/add")
    public String add(@RequestParam(value = "linked", required = false, defaultValue = "false") boolean linked,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "systemTypeName", required = false) String systemTypeName,
            @RequestParam(value = "equipmentName", required = false) String equipmentName,
            @RequestParam(value = "customerAddress", required = false) String customerAddress,
            @RequestParam(value = "faultDescription", required = false) String faultDescription,
            ModelMap mmap) {
        List<com.ruoyi.fire.domain.FireCompany> companies = companyService.selectCompanyAll();
        List<com.ruoyi.fire.domain.FireSystemType> systemTypes = systemTypeService.selectFireSystemTypeAll();
        com.ruoyi.fire.domain.FireCompany linkedCompany = linked && companyId != null
                ? companyService.selectFireCompanyById(companyId) : null;
        Long resolvedSystemTypeId = null;
        if (linked && StringUtils.isNotEmpty(systemTypeName)) {
            String normalizedRequested = normalizeSystemName(systemTypeName);
            for (com.ruoyi.fire.domain.FireSystemType type : systemTypes) {
                if (type != null && normalizedRequested.equals(normalizeSystemName(type.getTypeName()))) {
                    resolvedSystemTypeId = type.getTypeId();
                    systemTypeName = type.getTypeName();
                    break;
                }
            }
        }
        if (linkedCompany != null) {
            companyId = linkedCompany.getCompanyId();
            customerAddress = linkedCompany.getAddress();
        }
        mmap.put("companies", companies);
        mmap.put("systemTypes", systemTypes);
        mmap.put("equipments", equipmentService.selectEquipmentAll());
        mmap.put("urgencyLevels", UrgencyLevel.values());
        mmap.put("equipmentNames", com.ruoyi.fire.enums.FireEquipmentCategory.allLabels());
        mmap.put("linkedRepair", linked);
        mmap.put("prefillCompanyId", companyId);
        mmap.put("prefillCompanyName", linkedCompany != null ? linkedCompany.getCompanyName() : "");
        mmap.put("prefillSystemTypeId", resolvedSystemTypeId);
        mmap.put("prefillSystemTypeName", StringUtils.defaultString(systemTypeName));
        mmap.put("prefillEquipmentName", StringUtils.defaultString(equipmentName));
        mmap.put("prefillCustomerAddress", StringUtils.defaultString(customerAddress));
        mmap.put("prefillFaultDescription", StringUtils.defaultString(faultDescription));
        return prefix + "/add";
    }

    private String normalizeSystemName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace('（', '(').replace('）', ')')
                .replace("/", "").replace("防排烟", "防烟排烟");
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
            fireDataPermissionService.assertCanAccessRepair(ShiroUtils.getSysUser(),
                    fireFaultRepairService.selectFireFaultRepairById(fireFaultRepair.getRepairId()));
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
     * 页面仅渲染报修信息；处理人列表由 /dispatchUsers/{repairId} 异步加载。
     */
    @RequiresPermissions("fire:repair:accept")
    @GetMapping("/dispatch/{repairId}")
    public String dispatch(@PathVariable("repairId") Long repairId, ModelMap mmap) {
        FireFaultRepair repair = getRepair(repairId);
        mmap.put("repair", repair);
        return prefix + "/dispatch";
    }

    /**
     * 按报修单加载可派发处理人（全部已注册且正常的系统用户）。
     */
    @RequiresPermissions("fire:repair:accept")
    @GetMapping("/dispatchUsers/{repairId}")
    @ResponseBody
    public AjaxResult dispatchUsers(@PathVariable("repairId") Long repairId) {
        try {
            return success(fireFaultRepairService.selectDispatchUsers(repairId));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("fire:repair:accept")
    @Log(title = "派发报修", businessType = BusinessType.UPDATE)
    @PostMapping("/dispatch")
    @ResponseBody
    public AjaxResult dispatchSave(Long repairId, Long repairUserId) {
        try {
            if (repairUserId == null) {
                return error("请选择报修处理人");
            }
            fireDataPermissionService.assertCanAccessRepair(ShiroUtils.getSysUser(),
                    fireFaultRepairService.selectFireFaultRepairById(repairId));
            return toAjax(fireFaultRepairService.dispatchRepair(
                    repairId, repairUserId, ShiroUtils.getLoginName()));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 撤回派发（复用 fire:repair:accept，与派发同权）。
     */
    @RequiresPermissions("fire:repair:accept")
    @Log(title = "撤回报修派发", businessType = BusinessType.UPDATE)
    @PostMapping("/recall")
    @ResponseBody
    public AjaxResult recall(Long repairId) {
        try {
            if (repairId == null) {
                return error("repairId 不能为空");
            }
            fireDataPermissionService.assertCanAccessRepair(ShiroUtils.getSysUser(),
                    fireFaultRepairService.selectFireFaultRepairById(repairId));
            fireFaultRepairService.recallDispatch(repairId, ShiroUtils.getLoginName());
            return AjaxResult.success("撤回成功");
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
        fireDataPermissionService.assertCanAccessRepair(ShiroUtils.getSysUser(),
                fireFaultRepairService.selectFireFaultRepairById(repairId));
        return toAjax(fireFaultRepairService.acceptRepair(repairId, repairPerson, repairPhone));
    }

    @RequiresPermissions("fire:repair:start")
    @Log(title = "开始处理报修", businessType = BusinessType.UPDATE)
    @PostMapping("/start/{repairId}")
    @ResponseBody
    public AjaxResult start(@PathVariable("repairId") Long repairId) {
        try {
            return toAjax(fireFaultRepairService.startRepair(repairId));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions("fire:repair:complete")
    @GetMapping("/complete/{repairId}")
    public String complete(@PathVariable("repairId") Long repairId, ModelMap mmap) {
        FireFaultRepair repair = fireFaultRepairService.selectFireFaultRepairById(repairId);
        fireDataPermissionService.assertCanProcessRepair(ShiroUtils.getSysUser(), repair);
        if (repair.getStartTime() == null) {
            throw new ServiceException("请先开始处理后再填写处理结果");
        }
        mmap.put("repair", repair);
        return prefix + "/complete";
    }

    @RequiresPermissions("fire:repair:complete")
    @Log(title = "完成报修", businessType = BusinessType.UPDATE)
    @PostMapping("/complete")
    @ResponseBody
    public AjaxResult completeSave(FireFaultRepair fireFaultRepair) {
        try {
            fireFaultRepair.setUpdateBy(ShiroUtils.getLoginName());
            return toAjax(fireFaultRepairService.completeRepair(fireFaultRepair));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    private FireFaultRepair getRepair(Long repairId) {
        FireFaultRepair repair = fireFaultRepairService.selectFireFaultRepairById(repairId);
        fireDataPermissionService.assertCanAccessRepair(ShiroUtils.getSysUser(), repair);
        return repair;
    }

    private String extractWorkbenchCategory(FireFaultRepair query) {
        if (query == null) {
            return null;
        }
        Map<String, Object> params = query.getParams();
        if (params == null || params.isEmpty()) {
            return null;
        }
        Object raw = params.get("workbenchCategory");
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        return StringUtils.isEmpty(value) ? null : value;
    }
}
