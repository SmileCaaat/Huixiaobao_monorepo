package com.ruoyi.web.controller.fire;

import java.util.Date;
import java.util.List;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.ruoyi.fire.domain.FireMaintenanceTask;
import com.ruoyi.fire.service.IFireBuildingService;
import com.ruoyi.fire.service.IFireCompanyService;
import com.ruoyi.fire.service.IFireDataPermissionService;
import com.ruoyi.fire.service.IFireInspectionService;
import com.ruoyi.fire.service.IFireMaintenanceTemplateCategoryService;
import com.ruoyi.fire.service.IFireMaintenanceTaskService;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateCategoryVO;
import com.ruoyi.fire.domain.dto.FireInspectionTemplateEquipmentVO;
import com.ruoyi.fire.domain.dto.FireInspectionTestCategoryGroup;
import com.ruoyi.fire.domain.dto.FireInspectionTestEquipmentGroup;
import com.ruoyi.fire.service.support.FireInspectionTestKeys;
import com.ruoyi.common.utils.StringUtils;

/**
 * 巡查测试Controller
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
    private IFireMaintenanceTaskService maintenanceTaskService;

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
    @Log(title = "巡查测试", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(FireInspection inspection) {
        fireDataPermissionService.applyInspectionListScope(inspection, ShiroUtils.getSysUser());
        List<FireInspection> list = inspectionService.selectFireInspectionList(inspection);
        ExcelUtil<FireInspection> util = new ExcelUtil<FireInspection>(FireInspection.class);
        return util.exportExcel(list, "巡查测试数据");
    }

    @GetMapping("/add")
    public String add(@RequestParam(value = "linked", required = false, defaultValue = "false") boolean linked,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "categoryKey", required = false) String categoryKey,
            @RequestParam(value = "equipmentKey", required = false) String equipmentKey,
            ModelMap mmap) {
        mmap.put("companies", companyService.selectCompanyAll());
        mmap.put("systemTypes", templateCategoryService.listInspectionLevel1Categories());
        mmap.put("linkedInspection", linked);
        if (linked) {
            FireMaintenanceTask task = maintenanceTaskService.selectFireMaintenanceTaskBaseByTaskId(taskId);
            if (task == null) {
                throw new ServiceException("关联的维保任务不存在");
            }
            fireDataPermissionService.assertCanAccessTask(ShiroUtils.getSysUser(), task);
            companyId = task.getCompanyId();
            FireCompany company = companyService.selectFireCompanyById(companyId);
            fireDataPermissionService.assertCanAccessCompanyContext(ShiroUtils.getSysUser(), companyId, task);
            FireInspectionTestCategoryGroup category =
                    maintenanceTaskService.buildInspectionTestSystem(taskId, categoryKey);
            FireInspectionTestEquipmentGroup equipment = category.getEquipments().stream()
                    .filter(item -> item != null && equipmentKey.equals(item.getEquipmentKey()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("关联设备不存在或不属于当前类目"));
            if (company == null || category == null || equipment == null) {
                throw new ServiceException("关联的单位、系统或设备不存在，请刷新任务后重试");
            }
            // 消防维护页传入的是任务树编码键；表单/落库需要模板类目键（如 n:消防栓（消防炮）灭火系统）
            FireInspectionTemplateCategoryVO templateCategory = resolveTemplateCategory(category);
            if (templateCategory == null) {
                throw new ServiceException("关联系统类目无效，请确认模板中存在：" + category.getCategoryName());
            }
            FireInspectionTemplateEquipmentVO templateEquipment =
                    resolveTemplateEquipment(templateCategory.getCategoryKey(), equipment);
            if (templateEquipment == null) {
                throw new ServiceException("关联设备类目无效，请确认模板中存在：" + equipment.getEquipmentName());
            }
            mmap.put("linkedCompany", company);
            mmap.put("linkedCategory", category);
            mmap.put("linkedEquipment", equipment);
            mmap.put("linkedTemplateCategory", templateCategory);
            mmap.put("linkedTemplateEquipment", templateEquipment);
            mmap.put("linkedBuildingId", task.getBuildingId());
            mmap.put("linkedBuildingName", task.getBuildingName());
            mmap.put("linkedTaskId", task.getTaskId());
            mmap.put("equipmentTypes",
                    templateCategoryService.listEquipmentsByCategoryKey(templateCategory.getCategoryKey()));
        }
        return prefix + "/add_new";
    }

    /**
     * 新增巡查测试。
     * 关联维保任务时：任务干系人即可保存（不强制 fire:inspection:add）。
     * 独立新增：仍要求 fire:inspection:add。
     */
    @Log(title = "巡查测试", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireInspection inspection) {
        try {
            FireMaintenanceTask linkedTask = null;
            if (inspection.getTaskId() != null) {
                linkedTask = maintenanceTaskService
                        .selectFireMaintenanceTaskBaseByTaskId(inspection.getTaskId());
                if (linkedTask == null || linkedTask.getCompanyId() == null
                        || !linkedTask.getCompanyId().equals(inspection.getCompanyId())) {
                    return AjaxResult.error("关联维保任务与所选单位不一致");
                }
                fireDataPermissionService.assertCanAccessTask(ShiroUtils.getSysUser(), linkedTask);
                inspection.setInspectionType("0");
            } else if (!SecurityUtils.getSubject().isPermitted("fire:inspection:add")) {
                return AjaxResult.error("您没有创建数据的权限，请联系管理员添加权限 [fire:inspection:add]");
            }
            fireDataPermissionService.assertCanAccessCompanyContext(
                    ShiroUtils.getSysUser(), inspection.getCompanyId(), linkedTask);
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
    @Log(title = "巡查测试", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireInspection inspection) {
        FireInspection existing = inspectionService.selectFireInspectionById(inspection.getInspectionId());
        try {
            fireDataPermissionService.assertCanAccessInspection(ShiroUtils.getSysUser(), existing);
            FireMaintenanceTask linkedTask = null;
            Long taskId = inspection.getTaskId() != null ? inspection.getTaskId()
                    : (existing != null ? existing.getTaskId() : null);
            if (taskId != null) {
                linkedTask = maintenanceTaskService.selectFireMaintenanceTaskBaseByTaskId(taskId);
            }
            fireDataPermissionService.assertCanAccessCompanyContext(
                    ShiroUtils.getSysUser(), inspection.getCompanyId(), linkedTask);
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
        inspection.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(inspectionService.updateFireInspection(inspection));
    }

    @RequiresPermissions("fire:inspection:remove")
    @Log(title = "巡查测试", businessType = BusinessType.DELETE)
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
    public AjaxResult getBuildingsByCompanyId(@PathVariable("companyId") Long companyId,
            @RequestParam(value = "taskId", required = false) Long taskId) {
        try {
            FireMaintenanceTask linkedTask = null;
            if (taskId != null) {
                linkedTask = maintenanceTaskService.selectFireMaintenanceTaskBaseByTaskId(taskId);
            }
            fireDataPermissionService.assertCanAccessCompanyContext(
                    ShiroUtils.getSysUser(), companyId, linkedTask);
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

    private FireInspectionTemplateCategoryVO resolveTemplateCategory(FireInspectionTestCategoryGroup group) {
        if (group == null) {
            return null;
        }
        FireInspectionTemplateCategoryVO byKey = templateCategoryService.findLevel1ByCategoryKey(group.getCategoryKey());
        if (byKey != null) {
            return byKey;
        }
        String name = FireInspectionTestKeys.normalizeText(group.getCategoryName());
        if (StringUtils.isNotEmpty(name)) {
            FireInspectionTemplateCategoryVO byName =
                    templateCategoryService.findLevel1ByCategoryKey("n:" + name.toLowerCase());
            if (byName != null) {
                return byName;
            }
            for (FireInspectionTemplateCategoryVO vo : templateCategoryService.listInspectionLevel1Categories()) {
                if (name.equalsIgnoreCase(FireInspectionTestKeys.normalizeText(vo.getCategoryName()))) {
                    return vo;
                }
            }
        }
        return null;
    }

    private FireInspectionTemplateEquipmentVO resolveTemplateEquipment(String templateCategoryKey,
            FireInspectionTestEquipmentGroup group) {
        if (StringUtils.isEmpty(templateCategoryKey) || group == null) {
            return null;
        }
        FireInspectionTemplateEquipmentVO byKey =
                templateCategoryService.findEquipment(templateCategoryKey, group.getEquipmentKey());
        if (byKey != null) {
            return byKey;
        }
        String name = FireInspectionTestKeys.normalizeText(group.getEquipmentName());
        if (StringUtils.isNotEmpty(name)) {
            FireInspectionTemplateEquipmentVO byName =
                    templateCategoryService.findEquipment(templateCategoryKey, "n:" + name.toLowerCase());
            if (byName != null) {
                return byName;
            }
            for (FireInspectionTemplateEquipmentVO vo : templateCategoryService
                    .listEquipmentsByCategoryKey(templateCategoryKey)) {
                if (name.equalsIgnoreCase(FireInspectionTestKeys.normalizeText(vo.getEquipmentName()))) {
                    return vo;
                }
            }
        }
        return null;
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
