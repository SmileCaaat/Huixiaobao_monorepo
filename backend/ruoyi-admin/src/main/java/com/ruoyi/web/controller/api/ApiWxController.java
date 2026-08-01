package com.ruoyi.web.controller.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.config.WxMiniAppConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.shiro.service.SysLoginService;
import com.ruoyi.framework.shiro.token.WxAuthToken;
import com.ruoyi.framework.wx.WxMiniAppClient;
import com.ruoyi.framework.wx.WxMiniAppClient.WxSessionResult;
import com.ruoyi.system.service.ISysUserService;

/**
 * 微信小程序绑定与登录
 */
@RestController
@RequestMapping("/api/wx")
public class ApiWxController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(ApiWxController.class);

    @Autowired
    private WxMiniAppConfig wxMiniAppConfig;

    @Autowired
    private WxMiniAppClient wxMiniAppClient;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysLoginService loginService;

    /**
     * 绑定微信（需已登录）
     */
    @PostMapping("/bind")
    public AjaxResult bind(@RequestBody Map<String, String> body)
    {
        if (!wxMiniAppConfig.isConfigured())
        {
            return AjaxResult.error("微信小程序未配置，请联系管理员");
        }
        SysUser current = ShiroUtils.getSysUser();
        if (current == null)
        {
            return AjaxResult.error("用户未登录").put(AjaxResult.CODE_TAG, 401);
        }
        String code = body.get("code");
        String phonenumber = body.get("phonenumber");
        SysUser target = current;
        if (StringUtils.isNotEmpty(phonenumber) && !phonenumber.equals(current.getPhonenumber()))
        {
            SysUser byPhone = userService.selectUserByPhoneNumber(phonenumber);
            if (byPhone == null)
            {
                return AjaxResult.error("手机号未注册");
            }
            target = byPhone;
        }
        WxSessionResult session = wxMiniAppClient.code2Session(code);
        SysUser existing = userService.selectUserByOpenid(session.getOpenid());
        if (existing != null && !existing.getUserId().equals(target.getUserId()))
        {
            log.warn("微信绑定冲突 openid={} 已绑 userId={} 当前 userId={}",
                    session.getOpenid(), existing.getUserId(), target.getUserId());
            return AjaxResult.error("该微信已绑定其他账号");
        }
        SysUser update = new SysUser();
        update.setUserId(target.getUserId());
        update.setOpenid(session.getOpenid());
        update.setUnionId(session.getUnionid());
        update.setWxBindTime(new Date());
        update.setUpdateBy(current.getLoginName());
        if (userService.updateUserInfo(update) > 0)
        {
            if (target.getUserId().equals(current.getUserId()))
            {
                ShiroUtils.setSysUser(userService.selectUserById(current.getUserId()));
            }
            return AjaxResult.success("绑定成功");
        }
        return AjaxResult.error("绑定失败");
    }

    /**
     * 微信 code 登录（已绑定用户）
     */
    @PostMapping("/login")
    public AjaxResult wxLogin(@RequestBody Map<String, String> body)
    {
        if (!wxMiniAppConfig.isConfigured())
        {
            return AjaxResult.error("微信小程序未配置，请使用账号密码登录");
        }
        String code = body.get("code");
        WxSessionResult session = wxMiniAppClient.code2Session(code);
        SysUser user = userService.selectUserByOpenid(session.getOpenid());
        if (user == null)
        {
            return AjaxResult.error("微信未绑定账号，请先绑定或使用密码登录");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            return AjaxResult.error("账号已停用");
        }
        try
        {
            loginService.validateAuditStatus(user, user.getLoginName());
            loginService.validateMiniLoginAllowed(user, user.getLoginName());
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
        try
        {
            Subject subject = SecurityUtils.getSubject();
            subject.login(new WxAuthToken(user.getLoginName()));
            SysUser loginUser = ShiroUtils.getSysUser();
            String apiToken = subject.getSession().getId().toString();
            Map<String, Object> data = buildLoginData(apiToken, loginUser);
            return AjaxResult.success("登录成功", data);
        }
        catch (AuthenticationException e)
        {
            return AjaxResult.error(StringUtils.isNotEmpty(e.getMessage()) ? e.getMessage() : "登录失败");
        }
    }

    private Map<String, Object> buildLoginData(String apiToken, SysUser user)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("token", apiToken);
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("loginName", user.getLoginName());
        data.put("phonenumber", user.getPhonenumber());
        data.put("avatar", user.getAvatar());
        data.put("wxBound", StringUtils.isNotEmpty(user.getOpenid()));
        return data;
    }
}
