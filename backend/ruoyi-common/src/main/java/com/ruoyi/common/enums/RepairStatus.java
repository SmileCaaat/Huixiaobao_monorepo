package com.ruoyi.common.enums;

/**
 * 报修状态枚举。
 */
public enum RepairStatus {
    PENDING("0", "待处理"),
    IN_PROGRESS("1", "处理中"),
    COMPLETED("2", "已完成");

    private final String code;
    private final String info;

    RepairStatus(String code, String info) {
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
        for (RepairStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getInfo();
            }
        }
        return null;
    }
}
