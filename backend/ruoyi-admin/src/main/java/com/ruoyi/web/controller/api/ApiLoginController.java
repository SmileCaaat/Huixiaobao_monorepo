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
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.framework.shiro.service.SysLoginService;
import com.ruoyi.framework.shiro.service.SysPasswordService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 小程序端登录API控制器
 * 
 * @author ruoyi
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
            // 创建Token
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, false);
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);

            // 获取用户信息
            SysUser user = ShiroUtils.getSysUser();

            // 生成API Token（使用sessionId作为token）
            String apiToken = subject.getSession().getId().toString();

            Map<String, Object> data = new HashMap<>();
            data.put("token", apiToken);
            data.put("userId", user.getUserId());
            data.put("userName", user.getUserName());
            data.put("loginName", user.getLoginName());
            data.put("phonenumber", user.getPhonenumber());
            data.put("avatar", user.getAvatar());

            return AjaxResult.success("登录成功", data);
        } catch (AuthenticationException e) {
            String msg = "用户名或密码错误";
            if (e.getMessage() != null) {
                msg = e.getMessage();
            }
            return AjaxResult.error(msg);
        }
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
        user.setSalt(""); // BCrypt 自带 salt，数据库字段置空
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

        currentUser.setUserName(user.getUserName());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setEmail(user.getEmail());
        currentUser.setSex(user.getSex());

        if (userService.updateUserInfo(currentUser) > 0) {
            ShiroUtils.setSysUser(userService.selectUserById(currentUser.getUserId()));
            return AjaxResult.success("修改成功");
        }
        return AjaxResult.error("修改失败");
    }
}
