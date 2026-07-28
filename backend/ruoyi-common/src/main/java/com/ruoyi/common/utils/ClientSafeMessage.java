package com.ruoyi.common.utils;

/**
 * Sanitize messages returned to browser clients.
 * Chinese literals use Unicode escapes to avoid source-encoding corruption on Windows.
 */
public final class ClientSafeMessage
{
    private static final String SYSTEM_HINT =
            "\u7cfb\u7edf\u6682\u65f6\u65e0\u6cd5\u767b\u5f55\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u3002";

    private static final String DEFAULT_LOGIN_FAIL = "\u7528\u6237\u6216\u5bc6\u7801\u9519\u8bef";

    private static final String API_BUSY =
            "\u7cfb\u7edf\u7e41\u5fd9\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u6216\u8054\u7cfb\u7ba1\u7406\u5458\u3002";

    private static final String OP_FAIL = "\u64cd\u4f5c\u5931\u8d25";

    private ClientSafeMessage()
    {
    }

    /**
     * Login-facing message. Never returns schema-upgrade wording (it was frequently a false positive
     * when MyBatis SQL text contained new column names). Real schema issues are logged at startup.
     */
    public static String forLogin(String message, Throwable cause)
    {
        if (StringUtils.isEmpty(message))
        {
            return DEFAULT_LOGIN_FAIL;
        }
        if (isUnsafeTechnicalDetail(message, cause) || isSqlError(message, cause))
        {
            return SYSTEM_HINT;
        }
        // Keep short business messages (captcha / password / locked / channel)
        if (message.length() <= 80 && !looksTechnical(message))
        {
            return message;
        }
        return SYSTEM_HINT;
    }

    public static String forApi(String message, Throwable cause)
    {
        if (isUnsafeTechnicalDetail(message, cause) || isSqlError(message, cause))
        {
            return API_BUSY;
        }
        return StringUtils.isEmpty(message) ? OP_FAIL : message;
    }

    private static boolean isSqlError(String message, Throwable cause)
    {
        if (containsIgnoreCase(message, "unknown column")
                || containsIgnoreCase(message, "SQLSyntaxErrorException")
                || containsIgnoreCase(message, "BadSqlGrammarException"))
        {
            return true;
        }
        Throwable t = cause;
        int guard = 0;
        while (t != null && guard++ < 20)
        {
            String cn = t.getClass().getName();
            String msg = t.getMessage();
            if (containsIgnoreCase(msg, "unknown column")
                    || (cn != null && (cn.contains("SQLSyntaxErrorException") || cn.contains("BadSqlGrammarException"))))
            {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private static boolean looksTechnical(String message)
    {
        return containsAny(message,
                "### Error",
                "Exception",
                "java.",
                "SQL:",
                "at ",
                "C:\\",
                "file:/",
                "jar:file:",
                "jdbc:");
    }

    public static boolean isUnsafeTechnicalDetail(String message, Throwable cause)
    {
        if (looksTechnical(message)
                || containsAny(message,
                        "PersistenceException",
                        "MyBatisSystemException",
                        "Mapper method",
                        "Nested exception",
                        "No message found under code",
                        "Cannot invoke"))
        {
            return true;
        }
        if (message != null && message.length() > 180)
        {
            return true;
        }
        return causeChainContains(cause,
                "SQLException",
                "PersistenceException",
                "MyBatisSystemException",
                "DataAccessException",
                "NoSuchMessageException");
    }

    private static boolean containsIgnoreCase(String text, String needle)
    {
        return text != null && needle != null && text.toLowerCase().contains(needle.toLowerCase());
    }

    private static boolean containsAny(String text, String... needles)
    {
        if (StringUtils.isEmpty(text))
        {
            return false;
        }
        for (String n : needles)
        {
            if (text.contains(n))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean causeChainContains(Throwable cause, String... needles)
    {
        Throwable t = cause;
        int guard = 0;
        while (t != null && guard++ < 20)
        {
            String cn = t.getClass().getName();
            String msg = t.getMessage();
            for (String n : needles)
            {
                if ((cn != null && cn.contains(n)) || (msg != null && msg.contains(n)))
                {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }
}
