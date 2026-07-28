package com.ruoyi.common.exception.user;

/**
 * Login channel permission exception.
 */
public class UserLoginChannelException extends UserException
{
    private static final long serialVersionUID = 1L;

    private final String defaultMessage;

    public UserLoginChannelException(String message)
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
