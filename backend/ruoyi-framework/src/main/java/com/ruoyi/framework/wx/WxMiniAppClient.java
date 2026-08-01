package com.ruoyi.framework.wx;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSON;
import com.ruoyi.common.config.WxMiniAppConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * WeChat mini program jscode2session client.
 */
@Component
public class WxMiniAppClient
{
    private static final Logger log = LoggerFactory.getLogger(WxMiniAppClient.class);

    private static final String JSCODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    @Autowired
    private WxMiniAppConfig wxMiniAppConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public WxSessionResult code2Session(String code)
    {
        if (!wxMiniAppConfig.isConfigured())
        {
            throw new ServiceException("\u5fae\u4fe1\u5c0f\u7a0b\u5e8f\u672a\u914d\u7f6e\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458");
        }
        if (StringUtils.isEmpty(code))
        {
            throw new ServiceException("\u5fae\u4fe1\u767b\u5f55\u51ed\u8bc1 code \u4e0d\u80fd\u4e3a\u7a7a");
        }
        String response = restTemplate.getForObject(JSCODE2SESSION_URL, String.class,
                wxMiniAppConfig.getAppId(), wxMiniAppConfig.getSecret(), code);
        if (StringUtils.isEmpty(response))
        {
            throw new ServiceException("\u5fae\u4fe1\u670d\u52a1\u65e0\u54cd\u5e94\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5");
        }
        Map<String, Object> map = JSON.parseObject(response, Map.class);
        if (map.containsKey("errcode") && !Integer.valueOf(0).equals(map.get("errcode")))
        {
            log.warn("jscode2session failed: {}", response);
            throw new ServiceException("\u5fae\u4fe1\u767b\u5f55\u5931\u8d25\uff1a" + map.getOrDefault("errmsg", "\u672a\u77e5\u9519\u8bef"));
        }
        WxSessionResult result = new WxSessionResult();
        result.setOpenid((String) map.get("openid"));
        result.setSessionKey((String) map.get("session_key"));
        result.setUnionid((String) map.get("unionid"));
        return result;
    }

    public static class WxSessionResult
    {
        private String openid;
        private String sessionKey;
        private String unionid;

        public String getOpenid()
        {
            return openid;
        }

        public void setOpenid(String openid)
        {
            this.openid = openid;
        }

        public String getSessionKey()
        {
            return sessionKey;
        }

        public void setSessionKey(String sessionKey)
        {
            this.sessionKey = sessionKey;
        }

        public String getUnionid()
        {
            return unionid;
        }

        public void setUnionid(String unionid)
        {
            this.unionid = unionid;
        }
    }
}
