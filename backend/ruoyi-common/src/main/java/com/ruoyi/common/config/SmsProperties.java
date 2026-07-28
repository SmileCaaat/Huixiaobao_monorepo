package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信验证码配置（可插拔）
 */
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties
{
    /** 是否启用短信 */
    private boolean enabled = false;

    /** log | aliyun | tencent */
    private String provider = "log";

    private int codeTtlSeconds = 300;

    private int resendIntervalSeconds = 60;

    private int maxSendPerHour = 10;

    private String accessKey;

    private String accessSecret;

    private String signName;

    private String templateCode;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public int getCodeTtlSeconds()
    {
        return codeTtlSeconds;
    }

    public void setCodeTtlSeconds(int codeTtlSeconds)
    {
        this.codeTtlSeconds = codeTtlSeconds;
    }

    public int getResendIntervalSeconds()
    {
        return resendIntervalSeconds;
    }

    public void setResendIntervalSeconds(int resendIntervalSeconds)
    {
        this.resendIntervalSeconds = resendIntervalSeconds;
    }

    public int getMaxSendPerHour()
    {
        return maxSendPerHour;
    }

    public void setMaxSendPerHour(int maxSendPerHour)
    {
        this.maxSendPerHour = maxSendPerHour;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
    }

    public String getAccessSecret()
    {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret)
    {
        this.accessSecret = accessSecret;
    }

    public String getSignName()
    {
        return signName;
    }

    public void setSignName(String signName)
    {
        this.signName = signName;
    }

    public String getTemplateCode()
    {
        return templateCode;
    }

    public void setTemplateCode(String templateCode)
    {
        this.templateCode = templateCode;
    }
}
