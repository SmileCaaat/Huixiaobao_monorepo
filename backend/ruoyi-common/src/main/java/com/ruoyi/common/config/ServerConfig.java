package com.ruoyi.common.config;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.ServletUtils;

/**
 * 服务相关配置
 * 
 * @author ruoyi
 *
 */
@Component
public class ServerConfig
{
    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径
     * 
     * @return 服务地址
     */
    public String getUrl()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        return getDomain(request);
    }

    public static String getDomain(HttpServletRequest request)
    {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        String domain = url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
        // 强制使用 HTTPS 协议（适用于通过 Nginx 代理的情况），排除本地环境
        if (domain.startsWith("http://") && !domain.contains("localhost") && !domain.contains("127.0.0.1")) {
            domain = domain.replace("http://", "https://");
        }
        return domain;
    }
}
