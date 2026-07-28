package com.ruoyi.web.controller.api;

import java.util.HashMap;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.utils.ClientSafeMessage;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.shiro.service.SysLoginService;
import com.ruoyi.framework.shiro.service.SysPasswordService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 小程序端登录API控制器
 */
@RestController
@RequestMapping("/api")
public class ApiLoginController extends BaseController {

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPasswordService passwordService;

    /**
     * 小程序登录
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> loginBody) {
        String username = loginBody.get("username");
        String password = loginBody.get("password");

        try {
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, false);
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);

            SysUser user = ShiroUtils.getSysUser();
            String denyMsg = validateMiniLoginUser(user);
            if (denyMsg != null) {
                subject.logout();
                return AjaxResult.error(denyMsg);
            }

            String apiToken = subject.getSession().getId().toString();
            Map<String, Object> data = buildUserLoginData(apiToken, user);
            return AjaxResult.success("登录成功", data);
        } catch (AuthenticationException e) {
            String msg = "用户名或密码错误";
            if (e.getMessage() != null) {
                msg = ClientSafeMessage.forLogin(e.getMessage(), e);
            }
            return AjaxResult.error(msg);
        }
    }

    private String validateMiniLoginUser(SysUser user) {
        if (user == null) {
            return "用户未登录";
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            return "账号已停用";
        }
        String auditStatus = user.getAuditStatus();
        if ("2".equals(auditStatus)) {
            return "账号已拒绝，请联系管理员";
        }
        // 待审（1）允许受限登录；业务菜单由角色/权限收窄
        String allowMini = user.getAllowMiniLogin();
        if (StringUtils.isNotEmpty(allowMini) && !"1".equals(allowMini)) {
            return "不允许小程序登录";
        }
        return null;
    }

    private Map<String, Object> buildUserLoginData(String apiToken, SysUser user) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", apiToken);
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("loginName", user.getLoginName());
        data.put("phonenumber", user.getPhonenumber());
        data.put("avatar", user.getAvatar());
        data.put("auditStatus", user.getAuditStatus());
        data.put("allowMiniLogin", user.getAllowMiniLogin());
        data.put("wxBound", StringUtils.isNotEmpty(user.getOpenid()));
        return data;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info")
    public AjaxResult getUserInfo() {
        try {
            SysUser user = ShiroUtils.getSysUser();
            if (user == null) {
                return AjaxResult.error("用户未登录").put(AjaxResult.CODE_TAG, 401);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("userName", user.getUserName());
            data.put("loginName", user.getLoginName());
            data.put("phonenumber", user.getPhonenumber());
            data.put("email", user.getEmail());
            data.put("sex", user.getSex());
            data.put("avatar", user.getAvatar());
            data.put("deptId", user.getDeptId());
            data.put("auditStatus", user.getAuditStatus());
            data.put("allowMiniLogin", user.getAllowMiniLogin());
            data.put("wxBound", StringUtils.isNotEmpty(user.getOpenid()));
            data.put("wxBindTime", user.getWxBindTime());
            if (user.getDept() != null) {
                data.put("deptName", user.getDept().getDeptName());
            }

            return AjaxResult.success(data);
        } catch (Exception e) {
            return AjaxResult.error("获取用户信息失败").put(AjaxResult.CODE_TAG, 401);
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public AjaxResult logout() {
        try {
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
            return AjaxResult.success("退出成功");
        } catch (Exception e) {
            return AjaxResult.error("退出失败");
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/user/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        SysUser user = ShiroUtils.getSysUser();
        if (user == null) {
            return AjaxResult.error("用户未登录").put(AjaxResult.CODE_TAG, 401);
        }

        if (!passwordService.matches(user, oldPassword)) {
            return AjaxResult.error("原密码错误");
        }

        if (passwordService.matches(user, newPassword)) {
            return AjaxResult.error("新密码不能与原密码相同");
        }

        user.setPassword(passwordService.encryptPassword(newPassword));
        user.setSalt("");
        user.setPwdUpdateDate(new java.util.Date());

        if (userService.resetUserPwd(user) > 0) {
            ShiroUtils.setSysUser(userService.selectUserById(user.getUserId()));
            return AjaxResult.success("修改成功");
        }
        return AjaxResult.error("修改失败");
    }

    /**
     * 修改用户信息
     */
    @PostMapping("/user/update")
    public AjaxResult updateUser(@RequestBody SysUser user) {
        SysUser currentUser = ShiroUtils.getSysUser();
        if (currentUser == null) {
            return AjaxResult.error("用户未登录").put(AjaxResult.CODE_TAG, 401);
        }

        // 仅允许更新本人资料；禁止用空串或纯昵称风格覆盖（空则保留原姓名）
        if (StringUtils.isNotEmpty(user.getUserName())) {
            currentUser.setUserName(user.getUserName().trim());
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber())) {
            currentUser.setPhonenumber(user.getPhonenumber().trim());
        }
        currentUser.setEmail(user.getEmail());
        currentUser.setSex(user.getSex());

        if (userService.updateUserInfo(currentUser) > 0) {
            ShiroUtils.setSysUser(userService.selectUserById(currentUser.getUserId()));
            return AjaxResult.success("修改成功");
        }
        return AjaxResult.error("修改失败");
    }
}
