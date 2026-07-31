package com.ruoyi.fire.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 巡检登记对象 fire_inspection
 * 
 * @author ruoyi
 */
public class FireInspection extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 巡检ID */
    private Long inspectionId;

    /** 公司ID */
    private Long companyId;

    /** 公司名称 */
    @Excel(name = "单位名称")
    private String companyName;

    /** 维保类型（0测试 1巡查 2保养） */
    @Excel(name = "维保类型", readConverterExp = "0=测试,1=巡查,2=保养")
    private String inspectionType;

    /** 建筑ID */
    private Long buildingId;

    /** 建筑名称 */
    @Excel(name = "建筑名称")
    private String buildingName;

    /** 所在楼层 */
    @Excel(name = "所在楼层")
    private String floor;

    /** 系统类型编码（兼容旧字段） */
    private String systemType;

    /** 系统名称 */
    @Excel(name = "系统名称")
    private String systemName;

    /** 消防维护一级类目 key（非 fire_system_type） */
    private String categoryKey;

    /** 消防维护二级设备 key（非 fire_system_type） */
    private String equipmentKey;

    /** 遗留字段：不再关联 fire_system_type */
    private Long systemTypeId;

    /** 遗留字段：不再关联 fire_system_type */
    private Long equipmentTypeId;

    /** 维保标准快照 */
    @Excel(name = "维保标准")
    private String maintenanceStandard;

    /** 设备名称 */
    @Excel(name = "巡检设备")
    private String equipmentName;

    /** 设备数量 */
    @Excel(name = "设备数量")
    private Integer equipmentCount;

    /** 具体位置 */
    @Excel(name = "具体位置")
    private String location;

    /** 巡检时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "巡检时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date inspectionTime;

    /** 设备状态（0正常 1异常） */
    @Excel(name = "设备状态", readConverterExp = "0=正常,1=异常")
    private String equipmentStatus;

    /** 巡查人员ID */
    private Long inspectorId;

    /** 巡查人员姓名 */
    @Excel(name = "操作员")
    private String inspectorName;

    /** 关键词（客户/建筑/设备/位置/操作员模糊匹配，非持久化） */
    private String keyword;

    /** 巡检月份 YYYY-MM（非持久化） */
    private String inspectionMonth;

    /** 巡检日期 YYYY-MM-DD（非持久化，小程序筛选） */
    private String inspectionDate;

    /** 图片URL */
    private String imageUrls;

    /** 关联任务ID */
    private Long taskId;

    /** 任务名称 */
    private String taskName;

    /** 删除标志 */
    private String delFlag;

    /** 图片列表（用于展示） */
    private List<String> images;

    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getEquipmentKey() {
        return equipmentKey;
    }

    public void setEquipmentKey(String equipmentKey) {
        this.equipmentKey = equipmentKey;
    }

    public Long getSystemTypeId() {
        return systemTypeId;
    }

    public void setSystemTypeId(Long systemTypeId) {
        this.systemTypeId = systemTypeId;
    }

    public Long getEquipmentTypeId() {
        return equipmentTypeId;
    }

    public void setEquipmentTypeId(Long equipmentTypeId) {
        this.equipmentTypeId = equipmentTypeId;
    }

    public String getMaintenanceStandard() {
        return maintenanceStandard;
    }

    public void setMaintenanceStandard(String maintenanceStandard) {
        this.maintenanceStandard = maintenanceStandard;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getInspectionMonth() {
        return inspectionMonth;
    }

    public void setInspectionMonth(String inspectionMonth) {
        this.inspectionMonth = inspectionMonth;
    }

    public String getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(String inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public Integer getEquipmentCount() {
        return equipmentCount;
    }

    public void setEquipmentCount(Integer equipmentCount) {
        this.equipmentCount = equipmentCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getInspectionTime() {
        return inspectionTime;
    }

    public void setInspectionTime(Date inspectionTime) {
        this.inspectionTime = inspectionTime;
    }

    public String getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(String equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    public Long getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("inspectionId", getInspectionId())
                .append("companyId", getCompanyId())
                .append("companyName", getCompanyName())
                .append("inspectionType", getInspectionType())
                .append("buildingId", getBuildingId())
                .append("buildingName", getBuildingName())
                .append("floor", getFloor())
                .append("systemType", getSystemType())
                .append("systemName", getSystemName())
                .append("equipmentName", getEquipmentName())
                .append("equipmentCount", getEquipmentCount())
                .append("location", getLocation())
                .append("inspectionTime", getInspectionTime())
                .append("equipmentStatus", getEquipmentStatus())
                .append("inspectorId", getInspectorId())
                .append("inspectorName", getInspectorName())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
