package com.ruoyi.fire.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.MapProperties;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fire.service.IGeoCodingService;

/**
 * Amap reverse geocoding (server-side; key from env).
 */
@Service
public class AmapGeoCodingServiceImpl implements IGeoCodingService
{
    private static final Logger log = LoggerFactory.getLogger(AmapGeoCodingServiceImpl.class);

    private static final String REGEO_URL = "https://restapi.amap.com/v3/geocode/regeo";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MapProperties mapProperties;

    @Override
    public Map<String, Object> reverseGeocode(Double longitude, Double latitude)
    {
        validateCoordinate(longitude, latitude);
        String address = resolveChineseAddress(longitude, latitude);
        Map<String, Object> data = new HashMap<>();
        data.put("longitude", longitude);
        data.put("latitude", latitude);
        data.put("address", address);
        return data;
    }

    @Override
    public String resolveChineseAddress(Double longitude, Double latitude)
    {
        validateCoordinate(longitude, latitude);
        String key = mapProperties.getAmapWebKey();
        if (StringUtils.isEmpty(key))
        {
            throw new ServiceException("地址解析服务未配置");
        }
        HttpURLConnection conn = null;
        try
        {
            String location = longitude + "," + latitude;
            String url = REGEO_URL + "?key=" + URLEncoder.encode(key, "UTF-8")
                    + "&location=" + URLEncoder.encode(location, "UTF-8")
                    + "&extensions=base&radius=1000";
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(Math.max(500, mapProperties.getConnectTimeoutMs()));
            conn.setReadTimeout(Math.max(500, mapProperties.getReadTimeoutMs()));
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            if (code != 200)
            {
                throw new ServiceException("地址解析失败");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
            }
            JsonNode root = objectMapper.readTree(sb.toString());
            String status = root.path("status").asText("");
            if (!"1".equals(status))
            {
                String info = root.path("info").asText("unknown");
                String infoCode = root.path("infocode").asText("");
                log.warn("amap regeo failed status={} info={} infocode={}", status, info, infoCode);
                if ("10009".equals(infoCode) || "USERKEY_PLAT_NOMATCH".equals(info))
                {
                    throw new ServiceException("地址解析密钥类型不匹配，请配置高德Web服务Key");
                }
                if ("10001".equals(infoCode) || "INVALID_USER_KEY".equals(info))
                {
                    throw new ServiceException("地址解析密钥无效");
                }
                throw new ServiceException("地址解析失败");
            }
            String formatted = root.path("regeocode").path("formatted_address").asText("");
            if (StringUtils.isEmpty(formatted) || looksLikeCoordinate(formatted))
            {
                throw new ServiceException("地址解析失败");
            }
            return formatted.trim();
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (SocketTimeoutException e)
        {
            log.warn("amap regeo timeout lng={} lat={}", longitude, latitude);
            throw new ServiceException("地址解析超时，请刷新定位");
        }
        catch (Exception e)
        {
            log.warn("amap regeo error lng={} lat={}: {}", longitude, latitude, e.getMessage());
            throw new ServiceException("地址解析失败");
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    private void validateCoordinate(Double longitude, Double latitude)
    {
        if (longitude == null || latitude == null)
        {
            throw new ServiceException("经纬度不能为空");
        }
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90)
        {
            throw new ServiceException("经纬度参数非法");
        }
    }

    private boolean looksLikeCoordinate(String text)
    {
        return text != null && text.matches("^-?\\d+(\\.\\d+)?\\s*,\\s*-?\\d+(\\.\\d+)?$");
    }
}
