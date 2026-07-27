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

    /**
     * 列表/详情展示：合法枚举返回文案；非法历史值不直接暴露裸数字。
     */
    public static String getDisplayInfo(String code) {
        String info = getInfo(code);
        if (info != null) {
            return info;
        }
        if (code == null || code.isEmpty()) {
            return "";
        }
        return "未知";
    }

    public static boolean isValid(String code) {
        return getInfo(code) != null;
    }
}
