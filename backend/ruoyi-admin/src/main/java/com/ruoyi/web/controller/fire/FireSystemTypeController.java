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
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.fire.domain.FireSystemType;
import com.ruoyi.fire.service.IFireSystemTypeService;

/**
 * 消防系统类型配置Controller
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/fire/systemType")
public class FireSystemTypeController extends BaseController {

    private String prefix = "fire/systemType";

    @Autowired
    private IFireSystemTypeService systemTypeService;

    @RequiresPermissions("fire:systemType:view")
    @GetMapping()
    public String systemType() {
        return prefix + "/systemType";
    }

    /**
     * 查询消防系统类型列表
     */
    @RequiresPermissions("fire:systemType:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FireSystemType systemType) {
        startPage();
        List<FireSystemType> list = systemTypeService.selectFireSystemTypeList(systemType);
        return getDataTable(list);
    }

    /**
     * 获取所有系统类型（用于下拉选择）
     */
    @GetMapping("/all")
    @ResponseBody
    public AjaxResult all() {
        List<FireSystemType> list = systemTypeService.selectFireSystemTypeAll();
        return AjaxResult.success(list);
    }

    /**
     * 新增消防系统类型
     */
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        // 获取所有系统类型作为父级选择
        List<FireSystemType> list = systemTypeService.selectFireSystemTypeAll();
        mmap.put("systemTypes", list);
        return prefix + "/add";
    }

    /**
     * 新增保存消防系统类型
     */
    @RequiresPermissions("fire:systemType:add")
    @Log(title = "消防系统类型", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated FireSystemType systemType) {
        if (!systemTypeService.checkTypeCodeUnique(systemType)) {
            return error("新增系统类型'" + systemType.getTypeName() + "'失败，类型编码已存在");
        }
        if (!systemTypeService.checkTypeNameUnique(systemType)) {
            return error("新增系统类型'" + systemType.getTypeName() + "'失败，类型名称已存在");
        }
        systemType.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(systemTypeService.insertFireSystemType(systemType));
    }

    /**
     * 修改消防系统类型
     */
    @RequiresPermissions("fire:systemType:edit")
    @GetMapping("/edit/{typeId}")
    public String edit(@PathVariable("typeId") Long typeId, ModelMap mmap) {
        FireSystemType systemType = systemTypeService.selectFireSystemTypeById(typeId);
        mmap.put("systemType", systemType);
        // 获取所有系统类型作为父级选择
        List<FireSystemType> list = systemTypeService.selectFireSystemTypeAll();
        mmap.put("systemTypes", list);
        return prefix + "/edit";
    }

    /**
     * 修改保存消防系统类型
     */
    @RequiresPermissions("fire:systemType:edit")
    @Log(title = "消防系统类型", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated FireSystemType systemType) {
        if (!systemTypeService.checkTypeCodeUnique(systemType)) {
            return error("修改系统类型'" + systemType.getTypeName() + "'失败，类型编码已存在");
        }
        if (!systemTypeService.checkTypeNameUnique(systemType)) {
            return error("修改系统类型'" + systemType.getTypeName() + "'失败，类型名称已存在");
        }
        systemType.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(systemTypeService.updateFireSystemType(systemType));
    }

    /**
     * 删除消防系统类型
     */
    @RequiresPermissions("fire:systemType:remove")
    @Log(title = "消防系统类型", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(systemTypeService.deleteFireSystemTypeByIds(ids));
    }

    /**
     * 校验类型编码唯一
     */
    @PostMapping("/checkTypeCodeUnique")
    @ResponseBody
    public boolean checkTypeCodeUnique(FireSystemType systemType) {
        return systemTypeService.checkTypeCodeUnique(systemType);
    }

    /**
     * 校验类型名称唯一
     */
    @PostMapping("/checkTypeNameUnique")
    @ResponseBody
    public boolean checkTypeNameUnique(FireSystemType systemType) {
        return systemTypeService.checkTypeNameUnique(systemType);
    }
}
