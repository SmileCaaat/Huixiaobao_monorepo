package com.ruoyi.fire.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 维保任务检查记录对象 fire_maintenance_record
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public class FireMaintenanceRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 任务ID */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 模板项ID */
    @Excel(name = "模板项ID")
    private Long templateId;

    /** 层级（1=一级 2=二级 3=三级） */
    @Excel(name = "层级", readConverterExp = "1=一级,2=二级,3=三级")
    private Integer level;

    /** 父级记录ID */
    @Excel(name = "父级记录ID")
    private Long parentRecordId;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String itemName;

    /** 项目编码 */
    @Excel(name = "项目编码")
    private String itemCode;

    /** 检查结果（0=待检查 1=正常 2=故障 3=无此设备） */
    @Excel(name = "检查结果", readConverterExp = "0=待检查,1=正常,2=故障,3=无此设备")
    private String checkResult;

    /** 故障描述 */
    @Excel(name = "故障描述")
    private String faultDescription;

    /** 故障图片（多个用逗号分隔） */
    @Excel(name = "故障图片")
    private String faultImages;

    /** 维修建议 */
    @Excel(name = "维修建议")
    private String repairSuggestion;

    /** 其他说明 */
    @Excel(name = "其他说明")
    private String otherNotes;

    /** 检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "检查时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /** 检查人ID */
    @Excel(name = "检查人ID")
    private Long checkerId;

    /** 检查人姓名 */
    @Excel(name = "检查人姓名")
    private String checkerName;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sortOrder;

    /** 记录类型（0=常规维保 1=消防设施测试） */
    @Excel(name = "记录类型", readConverterExp = "0=常规维保,1=消防设施测试")
    private String recordType;

    /** 设备位置 */
    @Excel(name = "设备位置")
    private String deviceLocation;

    /** 测试情况 */
    @Excel(name = "测试情况")
    private String testSituation;

    /** 测试时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "测试时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date testTime;

    /** 测试结果 */
    @Excel(name = "测试结果")
    private String testResult;

    /** 现场照片（多个用逗号分隔） */
    @Excel(name = "现场照片")
    private String sitePhotos;

    /** 总检查项数（统计字段，非数据库字段） */
    private Integer totalItems;

    /** 已完成项数（统计字段，非数据库字段） */
    private Integer completedItems;

    /** 未完成项数（统计字段，非数据库字段） */
    private Integer uncompletedItems;

    /** 系统状态（统计字段，非数据库字段，0=未完成 1=已完成） */
    private String systemStatus;

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }

    public void setParentRecordId(Long parentRecordId) {
        this.parentRecordId = parentRecordId;
    }

    public Long getParentRecordId() {
        return parentRecordId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultImages(String faultImages) {
        this.faultImages = faultImages;
    }

    public String getFaultImages() {
        return faultImages;
    }

    public void setRepairSuggestion(String repairSuggestion) {
        this.repairSuggestion = repairSuggestion;
    }

    public String getRepairSuggestion() {
        return repairSuggestion;
    }

    public void setOtherNotes(String otherNotes) {
        this.otherNotes = otherNotes;
    }

    public String getOtherNotes() {
        return otherNotes;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckerId(Long checkerId) {
        this.checkerId = checkerId;
    }

    public Long getCheckerId() {
        return checkerId;
    }

    public void setCheckerName(String checkerName) {
        this.checkerName = checkerName;
    }

    public String getCheckerName() {
        return checkerName;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setCompletedItems(Integer completedItems) {
        this.completedItems = completedItems;
    }

    public Integer getCompletedItems() {
        return completedItems;
    }

    public void setUncompletedItems(Integer uncompletedItems) {
        this.uncompletedItems = uncompletedItems;
    }

    public Integer getUncompletedItems() {
        return uncompletedItems;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    public String getDeviceLocation() {
        return deviceLocation;
    }

    public void setTestSituation(String testSituation) {
        this.testSituation = testSituation;
    }

    public String getTestSituation() {
        return testSituation;
    }

    public void setTestTime(Date testTime) {
        this.testTime = testTime;
    }

    public Date getTestTime() {
        return testTime;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setSitePhotos(String sitePhotos) {
        this.sitePhotos = sitePhotos;
    }

    public String getSitePhotos() {
        return sitePhotos;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("recordId", getRecordId())
                .append("taskId", getTaskId())
                .append("templateId", getTemplateId())
                .append("level", getLevel())
                .append("parentRecordId", getParentRecordId())
                .append("itemName", getItemName())
                .append("itemCode", getItemCode())
                .append("checkResult", getCheckResult())
                .append("faultDescription", getFaultDescription())
                .append("faultImages", getFaultImages())
                .append("repairSuggestion", getRepairSuggestion())
                .append("checkTime", getCheckTime())
                .append("checkerId", getCheckerId())
                .append("checkerName", getCheckerName())
                .append("sortOrder", getSortOrder())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }

}
