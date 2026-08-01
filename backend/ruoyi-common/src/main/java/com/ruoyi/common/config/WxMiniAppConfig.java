package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WeChat mini program configuration.
 */
@Component
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMiniAppConfig
{
    private String appId = "";

    private String secret = "";

    public boolean isConfigured()
    {
        return appId != null && !appId.trim().isEmpty()
                && secret != null && !secret.trim().isEmpty();
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getSecret()
    {
        return secret;
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
    }
}
