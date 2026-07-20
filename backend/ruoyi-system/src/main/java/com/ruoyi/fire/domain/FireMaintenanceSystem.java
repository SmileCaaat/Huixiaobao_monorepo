package com.ruoyi.fire.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 维保系统分类对象 fire_maintenance_system
 * 
 * @author ruoyi
 */
public class FireMaintenanceSystem {
    private static final long serialVersionUID = 1L;

    /** 系统ID */
    private Long systemId;

    /** 关联任务ID */
    private Long taskId;

    /** 系统名称 */
    private String systemName;

    /** 系统编码 */
    private String systemCode;

    /** 总项数 */
    private Integer totalItems;

    /** 已完成项数 */
    private Integer completedItems;

    /** 系统状态（0未完成 1已完成） */
    private String systemStatus;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 设备列表 */
    private List<FireMaintenanceDevice> devices;

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

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
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

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
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

    public List<FireMaintenanceDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<FireMaintenanceDevice> devices) {
        this.devices = devices;
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
                .append("systemId", getSystemId())
                .append("taskId", getTaskId())
                .append("systemName", getSystemName())
                .append("systemCode", getSystemCode())
                .append("totalItems", getTotalItems())
                .append("completedItems", getCompletedItems())
                .append("systemStatus", getSystemStatus())
                .append("sortOrder", getSortOrder())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
