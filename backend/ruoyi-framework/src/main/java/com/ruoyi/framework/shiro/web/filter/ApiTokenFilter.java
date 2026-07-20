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

/**
 * API Token认证过滤器
 * 用于小程序等移动端的Token认证
 * 
 * @author ruoyi
 */
public class ApiTokenFilter extends AccessControlFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = SecurityUtils.getSubject();
        return subject.isAuthenticated();
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
