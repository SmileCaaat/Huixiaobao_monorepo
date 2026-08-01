package com.ruoyi.framework.shiro.token;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * WeChat mini program auth token (passwordless).
 */
public class WxAuthToken implements AuthenticationToken
{
    private static final long serialVersionUID = 1L;

    private final String loginName;

    public WxAuthToken(String loginName)
    {
        this.loginName = loginName;
    }

    public String getLoginName()
    {
        return loginName;
    }

    @Override
    public Object getPrincipal()
    {
        return loginName;
    }

    @Override
    public Object getCredentials()
    {
        return "";
    }
}
