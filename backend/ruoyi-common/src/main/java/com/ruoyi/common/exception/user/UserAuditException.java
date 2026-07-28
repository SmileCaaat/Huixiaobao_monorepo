package com.ruoyi.common.exception.user;

/**
 * User audit status exception.
 */
public class UserAuditException extends UserException
{
    private static final long serialVersionUID = 1L;

    private final String defaultMessage;

    public UserAuditException(String message)
    {
        super(null, null);
        this.defaultMessage = message;
    }

    @Override
    public String getMessage()
    {
        return defaultMessage;
    }
}
