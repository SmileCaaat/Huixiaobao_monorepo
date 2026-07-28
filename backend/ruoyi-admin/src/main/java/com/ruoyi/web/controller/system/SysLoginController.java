package com.ruoyi.web.controller.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ClientSafeMessage;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.ConfigService;

/**
 * 登录验证
 * 
 * @author ruoyi
 */
@Controller
public class SysLoginController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(SysLoginController.class);

    /**
     * 是否开启记住我功能
     */
    @Value("${shiro.rememberMe.enabled: false}")
    private boolean rememberMe;

    @Autowired
    private ConfigService configService;

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, ModelMap mmap)
    {
        Subject subject = SecurityUtils.getSubject();
        // 已登录（含记住我）访问登录页：进入系统首页，避免浏览器返回误展示登录表单
        if (subject.isAuthenticated() || subject.isRemembered())
        {
            if (ServletUtils.isAjaxRequest(request))
            {
                return ServletUtils.renderString(response, "{\"code\":\"0\",\"msg\":\"已登录\"}");
            }
            return "redirect:/index";
        }
        // 如果是Ajax请求，返回Json字符串。
        if (ServletUtils.isAjaxRequest(request))
        {
            return ServletUtils.renderString(response, "{\"code\":\"1\",\"msg\":\"未登录或登录超时。请重新登录\"}");
        }
        // 是否开启记住我
        mmap.put("isRemembered", rememberMe);
        // 是否开启用户注册
        mmap.put("isAllowRegister", Convert.toBool(configService.getKey("sys.account.registerUser"), false));
        return "login";
    }

    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public AjaxResult ajaxLogin(HttpServletRequest request, String username, String password, Boolean rememberMe)
    {
        boolean remember = Boolean.TRUE.equals(rememberMe);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, remember);
        Subject subject = SecurityUtils.getSubject();
        String preSessionId = safeSessionId(subject);
        try
        {
            subject.login(token);
            String postSessionId = safeSessionId(subject);
            // 诊断用：不记录密码/验证码；仅跟踪 Session 是否在登录后仍可用
            log.info("LOGIN_CHAIN phase=post_login host={} uri={} username={} rememberMe={} authenticated={} remembered={} sessionIdBefore={} sessionIdAfter={} cookieHeaderPresent={}",
                    request.getServerName() + ":" + request.getServerPort(),
                    request.getRequestURI(),
                    username,
                    remember,
                    subject.isAuthenticated(),
                    subject.isRemembered(),
                    preSessionId,
                    postSessionId,
                    StringUtils.isNotEmpty(request.getHeader("Cookie")));
            return success();
        }
        catch (AuthenticationException e)
        {
            String msg = "用户或密码错误";
            if (StringUtils.isNotEmpty(e.getMessage()))
            {
                msg = ClientSafeMessage.forLogin(e.getMessage(), e);
            }
            log.info("LOGIN_CHAIN phase=post_login_fail host={} uri={} username={} msg={} sessionId={} authenticated={}",
                    request.getServerName() + ":" + request.getServerPort(),
                    request.getRequestURI(),
                    username,
                    msg,
                    safeSessionId(subject),
                    subject.isAuthenticated());
            return error(msg);
        }
    }

    private static String safeSessionId(Subject subject)
    {
        try
        {
            if (subject == null || subject.getSession(false) == null)
            {
                return "null";
            }
            Object id = subject.getSession(false).getId();
            return id == null ? "null" : String.valueOf(id);
        }
        catch (Exception e)
        {
            return "err";
        }
    }

    @GetMapping("/unauth")
    public String unauth()
    {
        return "error/unauth";
    }
}
