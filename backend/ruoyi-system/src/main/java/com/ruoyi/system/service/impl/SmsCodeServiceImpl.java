package com.ruoyi.system.service.impl;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.SmsProperties;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISmsCodeService;

/**
 * In-memory SMS code service. provider=log writes debug code only when sms.enabled=true.
 * No master/fixed codes.
 */
@Service
public class SmsCodeServiceImpl implements ISmsCodeService
{
    private static final Logger log = LoggerFactory.getLogger(SmsCodeServiceImpl.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSendAt = new ConcurrentHashMap<>();
    private final Map<String, HourCounter> hourCounters = new ConcurrentHashMap<>();
    private final Map<String, Integer> verifyFailCount = new ConcurrentHashMap<>();

    @Autowired
    private SmsProperties smsProperties;

    @Override
    public String sendRegisterCode(String phone, String clientIp)
    {
        if (!smsProperties.isEnabled())
        {
            throw new ServiceException("\u77ed\u4fe1\u670d\u52a1\u672a\u5f00\u542f\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u914d\u7f6e");
        }
        if (StringUtils.isEmpty(phone) || !phone.matches("^1\\d{10}$"))
        {
            throw new ServiceException("\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e");
        }

        long now = System.currentTimeMillis();
        Long last = lastSendAt.get(phone);
        if (last != null && now - last < smsProperties.getResendIntervalSeconds() * 1000L)
        {
            long wait = (smsProperties.getResendIntervalSeconds() * 1000L - (now - last) + 999) / 1000;
            throw new ServiceException("\u53d1\u9001\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7" + wait + "\u79d2\u540e\u518d\u8bd5");
        }

        String rateKey = StringUtils.isNotEmpty(clientIp) ? clientIp + ":" + phone : phone;
        HourCounter counter = hourCounters.computeIfAbsent(rateKey, k -> new HourCounter());
        if (!counter.tryIncrement(now, smsProperties.getMaxSendPerHour()))
        {
            throw new ServiceException("\u53d1\u9001\u6b21\u6570\u8fc7\u591a\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        codeStore.put(phone, new CodeEntry(code, now + smsProperties.getCodeTtlSeconds() * 1000L));
        lastSendAt.put(phone, now);
        verifyFailCount.remove(phone);
        cleanupExpired(now);

        String provider = (smsProperties.getProvider() == null ? "log" : smsProperties.getProvider()).toLowerCase();
        if ("log".equals(provider))
        {
            // Local debug only: never enable provider=log in production.
            log.info("[SMS-LOG] register code phone={} code={} ttl={}s", phone, code, smsProperties.getCodeTtlSeconds());
            return "\u9a8c\u8bc1\u7801\u5df2\u53d1\u9001\uff08\u8c03\u8bd5\u6a21\u5f0f\u89c1\u670d\u52a1\u65e5\u5fd7\uff09";
        }
        if ("aliyun".equals(provider) || "tencent".equals(provider))
        {
            if (StringUtils.isEmpty(smsProperties.getAccessKey()) || StringUtils.isEmpty(smsProperties.getAccessSecret()))
            {
                throw new ServiceException("\u77ed\u4fe1\u4f9b\u5e94\u5546\u672a\u914d\u7f6e\u5b8c\u6574\uff0c\u8bf7\u8054\u7cfb\u8fd0\u7ef4");
            }
            throw new ServiceException("\u77ed\u4fe1\u4f9b\u5e94\u5546 " + provider + " \u5c1a\u672a\u63a5\u5165\u5b9e\u73b0\uff0c\u8bf7\u6539\u7528\u5df2\u914d\u7f6e\u7684\u901a\u9053");
        }
        throw new ServiceException("\u4e0d\u652f\u6301\u7684\u77ed\u4fe1\u901a\u9053: " + provider);
    }

    @Override
    public boolean verifyAndConsume(String phone, String code)
    {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code))
        {
            return false;
        }
        int fails = verifyFailCount.getOrDefault(phone, 0);
        if (fails >= 5)
        {
            codeStore.remove(phone);
            return false;
        }
        CodeEntry entry = codeStore.get(phone);
        if (entry == null)
        {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now > entry.expireAt)
        {
            codeStore.remove(phone);
            return false;
        }
        if (!entry.code.equals(code.trim()))
        {
            verifyFailCount.put(phone, fails + 1);
            return false;
        }
        codeStore.remove(phone);
        verifyFailCount.remove(phone);
        return true;
    }

    private void cleanupExpired(long now)
    {
        Iterator<Map.Entry<String, CodeEntry>> it = codeStore.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, CodeEntry> e = it.next();
            if (now > e.getValue().expireAt)
            {
                it.remove();
            }
        }
    }

    private static final class CodeEntry
    {
        private final String code;
        private final long expireAt;

        private CodeEntry(String code, long expireAt)
        {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    private static final class HourCounter
    {
        private long windowStart;
        private int count;

        synchronized boolean tryIncrement(long now, int max)
        {
            if (windowStart == 0 || now - windowStart > 3600_000L)
            {
                windowStart = now;
                count = 0;
            }
            if (count >= max)
            {
                return false;
            }
            count++;
            return true;
        }
    }
}
