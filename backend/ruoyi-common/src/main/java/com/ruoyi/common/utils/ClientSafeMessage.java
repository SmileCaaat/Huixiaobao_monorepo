package com.ruoyi.common.utils;

/**
 * Sanitize messages returned to browser clients.
 * Chinese literals use Unicode escapes to avoid source-encoding corruption on Windows.
 */
public final class ClientSafeMessage
{
    private static final String SYSTEM_HINT =
            "\u7cfb\u7edf\u6682\u65f6\u65e0\u6cd5\u767b\u5f55\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u3002";

    /** Schema/code mismatch — actionable, still no raw SQL leak. */
    private static final String SCHEMA_HINT =
            "\u6570\u636e\u5e93\u7ed3\u6784\u672a\u5347\u7ea7\uff0c\u8bf7\u6267\u884c\u5458\u5de5\u6ce8\u518c\u8fc1\u79fb SQL \u540e\u91cd\u8bd5\u3002";

    private static final String DEFAULT_LOGIN_FAIL = "\u7528\u6237\u6216\u5bc6\u7801\u9519\u8bef";

    private static final String API_BUSY =
            "\u7cfb\u7edf\u7e41\u5fd9\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u6216\u8054\u7cfb\u7ba1\u7406\u5458\u3002";

    private static final String OP_FAIL = "\u64cd\u4f5c\u5931\u8d25";

    private ClientSafeMessage()
    {
    }

    /**
     * Login-facing message. Prefer business auth messages; only hide SQL/stack details.
     */
    public static String forLogin(String message, Throwable cause)
    {
        String business = extractBusinessAuthMessage(message, cause);
        if (StringUtils.isNotEmpty(business))
        {
            return business;
        }
        if (isSchemaMismatch(message, cause))
        {
            return SCHEMA_HINT;
        }
        if (isSqlOrStackLeak(message, cause))
        {
            return SYSTEM_HINT;
        }
        if (StringUtils.isEmpty(message))
        {
            return DEFAULT_LOGIN_FAIL;
        }
        if (message.length() <= 80 && !looksLikeStackOrSql(message))
        {
            return message;
        }
        return SYSTEM_HINT;
    }

    public static String forApi(String message, Throwable cause)
    {
        String duplicate = extractDuplicateKeyHint(message, cause);
        if (StringUtils.isNotEmpty(duplicate))
        {
            return duplicate;
        }
        if (isSchemaMismatch(message, cause))
        {
            return SCHEMA_HINT;
        }
        if (isSqlOrStackLeak(message, cause))
        {
            return API_BUSY;
        }
        return StringUtils.isEmpty(message) ? OP_FAIL : message;
    }

    /**
     * Map DB unique-key conflicts to short business tips (no SQL text).
     */
    private static String extractDuplicateKeyHint(String message, Throwable cause)
    {
        String blob = joinCauseMessages(message, cause);
        if (StringUtils.isEmpty(blob))
        {
            return null;
        }
        if (!containsIgnoreCase(blob, "duplicate") && !blob.contains("\u91cd\u590d"))
        {
            return null;
        }
        if (containsIgnoreCase(blob, "phonenumber") || containsIgnoreCase(blob, "uk_sys_user_phonenumber"))
        {
            return "\u8be5\u624b\u673a\u53f7\u5df2\u6ce8\u518c";
        }
        if (containsIgnoreCase(blob, "login_name"))
        {
            return "\u6ce8\u518c\u8d26\u53f7\u5df2\u5b58\u5728";
        }
        return "\u6570\u636e\u5df2\u5b58\u5728\uff0c\u8bf7\u52ff\u91cd\u590d\u63d0\u4ea4";
    }

    private static String joinCauseMessages(String message, Throwable cause)
    {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(message))
        {
            sb.append(message).append(' ');
        }
        Throwable t = cause;
        int guard = 0;
        while (t != null && guard++ < 20)
        {
            String m = safeThrowableMessage(t);
            if (StringUtils.isNotEmpty(m))
            {
                sb.append(m).append(' ');
            }
            t = t.getCause();
        }
        return sb.toString();
    }

    private static String extractBusinessAuthMessage(String message, Throwable cause)
    {
        if (isBusinessAuthText(message))
        {
            return message;
        }
        Throwable t = cause;
        int guard = 0;
        while (t != null && guard++ < 20)
        {
            String name = t.getClass().getName();
            String msg = safeThrowableMessage(t);
            if (isBusinessAuthText(msg))
            {
                return msg;
            }
            if (name.endsWith("IncorrectCredentialsException") || name.endsWith("UnknownAccountException")
                    || name.endsWith("UserPasswordNotMatchException") || name.endsWith("UserNotExistsException"))
            {
                return StringUtils.isNotEmpty(msg) ? msg : DEFAULT_LOGIN_FAIL;
            }
            if (name.endsWith("ExcessiveAttemptsException") || name.endsWith("UserPasswordRetryLimitExceedException"))
            {
                return StringUtils.isNotEmpty(msg) ? msg
                        : "\u5bc6\u7801\u8f93\u5165\u9519\u8bef\u6b21\u6570\u8fc7\u591a\uff0c\u5e10\u6237\u5df2\u9501\u5b9a";
            }
            if (name.endsWith("CaptchaException"))
            {
                return StringUtils.isNotEmpty(msg) ? msg : "\u9a8c\u8bc1\u7801\u9519\u8bef";
            }
            if (name.endsWith("LockedAccountException") || name.endsWith("UserBlockedException")
                    || name.endsWith("UserAuditException") || name.endsWith("UserLoginChannelException")
                    || name.endsWith("BlackListException") || name.endsWith("RoleBlockedException"))
            {
                return StringUtils.isNotEmpty(msg) ? msg : SYSTEM_HINT;
            }
            t = t.getCause();
        }
        return null;
    }

    private static boolean isBusinessAuthText(String message)
    {
        if (StringUtils.isEmpty(message) || looksLikeStackOrSql(message))
        {
            return false;
        }
        return message.contains("\u9a8c\u8bc1\u7801")
                || message.contains("\u5bc6\u7801")
                || message.contains("\u7528\u6237")
                || message.contains("\u9501\u5b9a")
                || message.contains("\u5ba1\u6838")
                || message.contains("\u5c01\u7981")
                || message.contains("\u4e0d\u5141\u8bb8")
                || message.contains("\u9ed1\u540d\u5355")
                || message.contains("\u5df2\u5220\u9664")
                || message.contains("\u6570\u636e\u5e93\u7ed3\u6784");
    }

    /**
     * Detect code/DB schema drift (Unknown column / missing table) without exposing SQL text.
     */
    private static boolean isSchemaMismatch(String message, Throwable cause)
    {
        if (looksLikeSchemaMismatch(message))
        {
            return true;
        }
        Throwable t = cause;
        int guard = 0;
        while (t != null && guard++ < 20)
        {
            if (looksLikeSchemaMismatch(safeThrowableMessage(t)))
            {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private static boolean looksLikeSchemaMismatch(String message)
    {
        if (StringUtils.isEmpty(message))
        {
            return false;
        }
        return containsIgnoreCase(message, "unknown column")
                || containsIgnoreCase(message, "doesn't exist")
                || containsIgnoreCase(message, "does not exist")
                || containsIgnoreCase(message, "table") && containsIgnoreCase(message, "not found")
                || message.contains("\u4e0d\u5b58\u5728") && (containsIgnoreCase(message, "column")
                        || containsIgnoreCase(message, "table") || message.contains("\u8868")
                        || message.contains("\u5b57\u6bb5"));
    }

    private static String safeThrowableMessage(Throwable t)
    {
        try
        {
            return t == null ? null : t.getMessage();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static boolean isSqlOrStackLeak(String message, Throwable cause)
    {
        if (looksLikeStackOrSql(message))
        {
            return true;
        }
        Throwable t = cause;
        int guard = 0;
        while (t != null && guard++ < 20)
        {
            String cn = t.getClass().getName();
            String msg = safeThrowableMessage(t);
            if (looksLikeStackOrSql(msg))
            {
                return true;
            }
            if (cn != null && (cn.contains("SQLSyntaxErrorException") || cn.contains("SQLException")
                    || cn.contains("PersistenceException") || cn.contains("MyBatisSystemException")
                    || cn.contains("BadSqlGrammarException") || cn.contains("DataAccessException")))
            {
                if (containsIgnoreCase(msg, "unknown column") || looksLikeStackOrSql(msg))
                {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }

    private static boolean looksLikeStackOrSql(String message)
    {
        if (StringUtils.isEmpty(message))
        {
            return false;
        }
        return containsAny(message,
                "### Error",
                "### The error",
                "SQL:",
                "Cause:",
                "java.sql.",
                "org.apache.ibatis",
                "PersistenceException",
                "MyBatisSystemException",
                "Mapper method",
                "jdbc:mysql",
                "C:\\",
                "file:/",
                "jar:file:",
                "Nested exception",
                "Unknown column")
                || (message.length() > 180 && (message.contains("at ") || message.contains(".java:")));
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
}
