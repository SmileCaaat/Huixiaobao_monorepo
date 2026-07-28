package com.ruoyi.framework.shiro.web.filter;

import java.io.PrintWriter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.service.ISysUserService;

/**
 * API Token认证过滤器
 * 用于小程序等移动端的Token认证
 */
public class ApiTokenFilter extends AccessControlFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            return false;
        }
        SysUser sessionUser = ShiroUtils.getSysUser();
        if (sessionUser == null || sessionUser.getUserId() == null) {
            subject.logout();
            return false;
        }
        ISysUserService userService = SpringUtils.getBean(ISysUserService.class);
        SysUser user = userService.selectUserById(sessionUser.getUserId());
        if (!isMiniApiUserAllowed(user)) {
            subject.logout();
            return false;
        }
        if (user != null) {
            ShiroUtils.setSysUser(user);
        }
        return true;
    }

    private boolean isMiniApiUserAllowed(SysUser user) {
        if (user == null) {
            return false;
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            return false;
        }
        // 审核仅约束自主注册员工；历史/后台用户不受 audit_status 空值影响
        String src = user.getRegisterSource();
        boolean selfRegistered = "qr".equals(src) || "invite_code".equals(src) || "direct".equals(src);
        if (selfRegistered && "2".equals(user.getAuditStatus())) {
            return false;
        }
        String allowMini = user.getAllowMiniLogin();
        if (StringUtils.isNotEmpty(allowMini) && !"1".equals(allowMini)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());

        PrintWriter writer = httpResponse.getWriter();
        AjaxResult result = AjaxResult.error("未登录或Token已过期，请重新登录").put(AjaxResult.CODE_TAG, 401);
        writer.write(JSON.toJSONString(result));
        writer.flush();
        writer.close();

        return false;
    }
}
