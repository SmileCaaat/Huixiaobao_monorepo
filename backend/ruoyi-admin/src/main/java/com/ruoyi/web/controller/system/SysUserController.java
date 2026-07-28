package com.ruoyi.web.controller.system;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.Ztree;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.shiro.service.SysPasswordService;
import com.ruoyi.framework.shiro.util.AuthorizationUtils;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 用户信息
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
    private String prefix = "system/user";

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysRoleMapper roleMapper;

    @RequiresPermissions("system:user:view")
    @GetMapping()
    public String user() {
        return prefix + "/user";
    }

    /**
     * 注册记录页
     */
    @RequiresPermissions("system:user:registerRecord")
    @GetMapping("/registerRecord")
    public String registerRecord(ModelMap mmap) {
        SysUser current = getSysUser();
        mmap.put("currentDeptId", current != null ? current.getDeptId() : null);
        return prefix + "/registerRecord";
    }

    /**
     * 注册记录列表
     */
    @RequiresPermissions("system:user:registerRecord")
    @PostMapping("/registerRecords")
    @ResponseBody
    public TableDataInfo registerRecords(SysUser user) {
        startPage();
        // Self-register sources by default; admin-created users stay on user list page.
        if (StringUtils.isEmpty(user.getRegisterSource())) {
            user.getParams().put("registerSourceIn", new String[] { "qr", "invite_code", "direct" });
        }
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @RequiresPermissions("system:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:user:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.exportExcel(list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions("system:user:import")
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String message = userService.importUser(userList, updateSupport, getLoginName());
        return AjaxResult.success(message);
    }

    @RequiresPermissions("system:user:view")
    @GetMapping("/importTemplate")
    @ResponseBody
    public AjaxResult importTemplate() {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 新增用户
     */
    @RequiresPermissions("system:user:add")
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("roles", roleService.selectRoleAll().stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        mmap.put("posts", postService.selectPostAll());
        return prefix + "/add";
    }

    /**
     * 新增保存用户
     */
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysUser user) {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkLoginNameUnique(user)) {
            return error("新增用户'" + user.getLoginName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return error("新增用户'" + user.getLoginName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return error("新增用户'" + user.getLoginName() + "'失败，邮箱账号已存在");
        }
        user.setPassword(passwordService.encryptPassword(user.getPassword()));
        user.setSalt(""); // BCrypt 自带 salt，数据库字段置空
        user.setPwdUpdateDate(DateUtils.getNowDate());
        user.setCreateBy(getLoginName());
        applyDefaultPhaseBFields(user, true);
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @RequiresPermissions("system:user:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, ModelMap mmap) {
        userService.checkUserDataScope(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        mmap.put("user", userService.selectUserById(userId));
        mmap.put("roles", SysUser.isAdmin(userId) ? roles
                : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        mmap.put("posts", postService.selectPostsByUserId(userId));
        return prefix + "/edit";
    }

    /**
     * 查询用户详细
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/view/{userId}")
    public String view(@PathVariable("userId") Long userId, ModelMap mmap) {
        userService.checkUserDataScope(userId);
        mmap.put("user", userService.selectUserById(userId));
        mmap.put("roleGroup", userService.selectUserRoleGroup(userId));
        mmap.put("postGroup", userService.selectUserPostGroup(userId));
        return prefix + "/view";
    }

    /**
     * 修改保存用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkLoginNameUnique(user)) {
            return error("修改用户'" + user.getLoginName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return error("修改用户'" + user.getLoginName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return error("修改用户'" + user.getLoginName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return toAjax(userService.updateUser(user));
    }

    @RequiresPermissions("system:user:resetPwd")
    @GetMapping("/resetPwd/{userId}")
    public String resetPwd(@PathVariable("userId") Long userId, ModelMap mmap) {
        userService.checkUserDataScope(userId);
        mmap.put("user", userService.selectUserById(userId));
        return prefix + "/resetPwd";
    }

    @RequiresPermissions("system:user:resetPwd")
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    @ResponseBody
    public AjaxResult resetPwdSave(SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(passwordService.encryptPassword(user.getPassword()));
        user.setSalt(""); // BCrypt 自带 salt，数据库字段置空
        if (userService.resetUserPwd(user) > 0) {
            if (ShiroUtils.getUserId().longValue() == user.getUserId().longValue()) {
                setSysUser(userService.selectUserById(user.getUserId()));
            }
            return success();
        }
        return error();
    }

    /**
     * 进入授权角色页
     */
    @RequiresPermissions("system:user:edit")
    @GetMapping("/authRole/{userId}")
    public String authRole(@PathVariable("userId") Long userId, ModelMap mmap) {
        userService.checkUserDataScope(userId);
        SysUser user = userService.selectUserById(userId);
        // 获取用户所属的角色列表
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        mmap.put("user", user);
        mmap.put("roles", SysUser.isAdmin(userId) ? roles
                : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return prefix + "/authRole";
    }

    /**
     * 用户授权角色
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PostMapping("/authRole/insertAuthRole")
    @ResponseBody
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return success();
    }

    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        if (ArrayUtils.contains(Convert.toLongArray(ids), getUserId())) {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(ids));
    }

    /**
     * 校验用户名
     */
    @PostMapping("/checkLoginNameUnique")
    @ResponseBody
    public boolean checkLoginNameUnique(SysUser user) {
        return userService.checkLoginNameUnique(user);
    }

    /**
     * 校验手机号码
     */
    @PostMapping("/checkPhoneUnique")
    @ResponseBody
    public boolean checkPhoneUnique(SysUser user) {
        return userService.checkPhoneUnique(user);
    }

    /**
     * 校验email邮箱
     */
    @PostMapping("/checkEmailUnique")
    @ResponseBody
    public boolean checkEmailUnique(SysUser user) {
        return userService.checkEmailUnique(user);
    }

    /**
     * 用户状态修改
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:user:edit")
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.changeStatus(user));
    }

    /**
     * 加载部门列表树
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/deptTreeData")
    @ResponseBody
    public List<Ztree> deptTreeData() {
        return deptService.selectDeptTree(new SysDept());
    }

    /**
     * 选择部门树
     * 
     * @param deptId 部门ID
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/selectDeptTree/{deptId}")
    public String selectDeptTree(@PathVariable("deptId") Long deptId, ModelMap mmap) {
        mmap.put("dept", deptService.selectDeptById(deptId));
        return prefix + "/deptTree";
    }

    /**
     * 解绑微信
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/unbindWx/{userId}")
    @ResponseBody
    public AjaxResult unbindWx(@PathVariable("userId") Long userId) {
        userService.checkUserAllowed(new SysUser(userId));
        userService.checkUserDataScope(userId);
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setOpenid("");
        update.setUnionId("");
        update.setUpdateBy(getLoginName());
        return toAjax(userService.updateUserInfo(update));
    }

    /**
     * 用户审核（通过/拒绝）— 乐观更新，仅待审可处理
     */
    @RequiresPermissions("system:user:audit")
    @Log(title = "用户审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(@Validated SysUser user, String auditAction, String auditRemark) {
        userService.checkUserDataScope(user.getUserId());
        SysUser existing = userService.selectUserById(user.getUserId());
        if (existing == null) {
            return error("用户不存在");
        }
        if (!"1".equals(existing.getAuditStatus())) {
            return error("该用户已由他人处理或无需审核");
        }

        Date now = new Date();
        if ("reject".equals(auditAction)) {
            if (StringUtils.isEmpty(auditRemark)) {
                return error("驳回时必须填写原因");
            }
            SysUser update = new SysUser();
            update.setUserId(user.getUserId());
            update.setAuditStatus("2");
            update.setDispatchable("0");
            update.setAuditBy(getLoginName());
            update.setAuditTime(now);
            update.setAuditRemark(auditRemark);
            update.setUpdateBy(getLoginName());
            int rows = userService.updateUserAuditOptimistic(update);
            if (rows == 0) {
                return error("已由他人处理");
            }
            return success();
        }
        if ("pass".equals(auditAction)) {
            Long deptId = user.getDeptId() != null ? user.getDeptId() : existing.getDeptId();
            if (deptId != null) {
                deptService.checkDeptDataScope(deptId);
            }
            if (user.getRoleIds() != null && user.getRoleIds().length > 0) {
                roleService.checkRoleDataScope(user.getRoleIds());
            }
            SysUser update = new SysUser();
            update.setUserId(user.getUserId());
            update.setAuditStatus("0");
            update.setDispatchable("1");
            update.setAuditBy(getLoginName());
            update.setAuditTime(now);
            update.setAuditRemark(StringUtils.isNotEmpty(auditRemark) ? auditRemark : "人工审核通过");
            update.setDeptId(deptId);
            update.setUpdateBy(getLoginName());
            int rows = userService.updateUserAuditOptimistic(update);
            if (rows == 0) {
                return error("已由他人处理");
            }
            // 赋角色：若前端指定则用指定；否则默认 maintenance_member
            Long[] roleIds = user.getRoleIds();
            if (roleIds == null || roleIds.length == 0) {
                SysRole memberRole = roleMapper.checkRoleKeyUnique("maintenance_member");
                if (memberRole != null && memberRole.getRoleId() != null) {
                    roleIds = new Long[] { memberRole.getRoleId() };
                }
            }
            if (roleIds != null && roleIds.length > 0) {
                userService.insertUserAuth(user.getUserId(), roleIds);
            }
            AuthorizationUtils.clearAllCachedAuthorizationInfo();
            return success();
        }
        return error("无效的审核操作");
    }

    private void applyDefaultPhaseBFields(SysUser user, boolean adminCreated) {
        if (StringUtils.isEmpty(user.getAllowAdminLogin())) {
            user.setAllowAdminLogin(adminCreated ? "1" : "0");
        }
        if (StringUtils.isEmpty(user.getAllowMiniLogin())) {
            user.setAllowMiniLogin("1");
        }
        if (StringUtils.isEmpty(user.getAuditStatus())) {
            user.setAuditStatus(adminCreated ? "0" : "1");
        }
        if (StringUtils.isEmpty(user.getDispatchable())) {
            user.setDispatchable(adminCreated || "0".equals(user.getAuditStatus()) ? "1" : "0");
        }
        if (StringUtils.isEmpty(user.getRegisterSource()) && adminCreated) {
            user.setRegisterSource("admin");
        }
    }
}