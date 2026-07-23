package com.ruoyi.common.enums;

/**
 * 紧急程度枚举。
 */
public enum UrgencyLevel {
    NORMAL("0", "一般"),
    URGENT("1", "紧急"),
    CRITICAL("2", "特急");

    private final String code;
    private final String info;

    UrgencyLevel(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static String getInfo(String code) {
        for (UrgencyLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level.getInfo();
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return getInfo(code) != null;
    }
}
