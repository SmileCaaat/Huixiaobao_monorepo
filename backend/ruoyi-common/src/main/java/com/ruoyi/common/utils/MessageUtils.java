package com.ruoyi.common.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 获取i18n资源文件。查找失败时回退中文默认文案，避免登录被误判为系统异常。
 */
public class MessageUtils
{
    private static final Map<String, String> FALLBACK = new HashMap<>();

    static
    {
        FALLBACK.put("user.jcaptcha.error", "\u9a8c\u8bc1\u7801\u9519\u8bef");
        FALLBACK.put("user.not.exists", "\u7528\u6237\u6216\u5bc6\u7801\u9519\u8bef");
        FALLBACK.put("user.password.not.match", "\u7528\u6237\u6216\u5bc6\u7801\u9519\u8bef");
        FALLBACK.put("user.password.retry.limit.count", "\u5bc6\u7801\u8f93\u5165\u9519\u8bef{0}\u6b21");
        FALLBACK.put("user.password.retry.limit.exceed",
                "\u5bc6\u7801\u8f93\u5165\u9519\u8bef{0}\u6b21\uff0c\u5e10\u6237\u9501\u5b9a10\u5206\u949f");
        FALLBACK.put("user.password.delete", "\u5bf9\u4e0d\u8d77\uff0c\u60a8\u7684\u8d26\u53f7\u5df2\u88ab\u5220\u9664");
        FALLBACK.put("user.blocked", "\u7528\u6237\u5df2\u5c01\u7981\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458");
        FALLBACK.put("role.blocked", "\u89d2\u8272\u5df2\u5c01\u7981\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458");
        FALLBACK.put("login.blocked", "\u5f88\u9057\u61be\uff0c\u8bbf\u95eeIP\u5df2\u88ab\u5217\u5165\u7cfb\u7edf\u9ed1\u540d\u5355");
        FALLBACK.put("user.login.success", "\u767b\u5f55\u6210\u529f");
        FALLBACK.put("not.null", "* \u5fc5\u987b\u586b\u5199");
    }

    public static String message(String code, Object... args)
    {
        String fallback = formatFallback(code, args);
        try
        {
            MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
            return messageSource.getMessage(code, args, fallback, LocaleContextHolder.getLocale());
        }
        catch (NoSuchMessageException e)
        {
            return fallback;
        }
        catch (Exception e)
        {
            return fallback != null ? fallback : code;
        }
    }

    private static String formatFallback(String code, Object... args)
    {
        String template = FALLBACK.getOrDefault(code, code);
        if (args == null || args.length == 0 || template == null || !template.contains("{0}"))
        {
            return template;
        }
        String result = template;
        for (int i = 0; i < args.length; i++)
        {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }
}
