package com.ruoyi.fire.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 维保设备对象 fire_maintenance_device
 * 
 * @author ruoyi
 */
public class FireMaintenanceDevice {
    private static final long serialVersionUID = 1L;

    /** 设备ID */
    private Long deviceId;

    /** 关联系统ID */
    private Long systemId;

    /** 关联任务ID */
    private Long taskId;

    /** 设备名称 */
    private String deviceName;

    /** 设备编码 */
    private String deviceCode;

    /** 检查类型（巡查/测试） */
    private String checkType;

    /** 总项数 */
    private Integer totalItems;

    /** 已完成项数 */
    private Integer completedItems;

    /** 设备状态（0未完成 1已完成） */
    private String deviceStatus;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 检查项列表 */
    private List<FireMaintenanceItem> items;

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getCompletedItems() {
        return completedItems;
    }

    public void setCompletedItems(Integer completedItems) {
        this.completedItems = completedItems;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<FireMaintenanceItem> getItems() {
        return items;
    }

    public void setItems(List<FireMaintenanceItem> items) {
        this.items = items;
    }

    /** 获取未完成项数 */
    public Integer getUncompletedItems() {
        if (totalItems != null && completedItems != null) {
            return totalItems - completedItems;
        }
        return 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("deviceId", getDeviceId())
                .append("systemId", getSystemId())
                .append("taskId", getTaskId())
                .append("deviceName", getDeviceName())
                .append("deviceCode", getDeviceCode())
                .append("checkType", getCheckType())
                .append("totalItems", getTotalItems())
                .append("completedItems", getCompletedItems())
                .append("deviceStatus", getDeviceStatus())
                .append("sortOrder", getSortOrder())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
