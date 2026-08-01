package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Map service config. Keys stay on server via env vars.
 */
@Component
@ConfigurationProperties(prefix = "ruoyi.map")
public class MapProperties
{
    /** Amap Web service key from AMAP_WEB_KEY */
    private String amapWebKey = "";

    private int connectTimeoutMs = 3000;

    private int readTimeoutMs = 5000;

    public String getAmapWebKey()
    {
        return amapWebKey;
    }

    public void setAmapWebKey(String amapWebKey)
    {
        this.amapWebKey = amapWebKey;
    }

    public int getConnectTimeoutMs()
    {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs)
    {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs()
    {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs)
    {
        this.readTimeoutMs = readTimeoutMs;
    }
}
