package com.ruoyi.fire.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 故障报修对象 fire_fault_repair。
 */
public class FireFaultRepair extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long repairId;

    @Excel(name = "报修单号")
    private String repairNo;

    private Long companyId;

    @Excel(name = "单位名称")
    private String companyName;

    private Long systemTypeId;

    @Excel(name = "系统名称")
    private String systemTypeName;

    private Long equipmentId;

    @Excel(name = "设备名称")
    private String equipmentName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发现时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date foundTime;

    @Excel(name = "是否上报", readConverterExp = "0=否,1=是")
    private String isReported;

    @Excel(name = "紧急程度", readConverterExp = "0=一般,1=紧急,2=特急")
    private String urgencyLevel;

    @Excel(name = "故障说明")
    private String faultDescription;

    private String faultImages;

    @Excel(name = "报修人")
    private String reporterName;

    private Long reporterId;

    @Excel(name = "报修电话")
    private String reporterPhone;

    private String customerAddress;

    @Excel(name = "报修状态", readConverterExp = "0=待处理,1=处理中,2=已完成")
    private String repairStatus;

    private Long repairUserId;

    @Excel(name = "处理人")
    private String repairPerson;

    private String repairPhone;

    @Excel(name = "派发人")
    private String dispatchBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "派发时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date dispatchTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date acceptTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    private String repairDescription;

    private String repairImages;

    private String status;

    private String delFlag;

    public Long getRepairId() {
        return repairId;
    }

    public void setRepairId(Long repairId) {
        this.repairId = repairId;
    }

    public String getRepairNo() {
        return repairNo;
    }

    public void setRepairNo(String repairNo) {
        this.repairNo = repairNo;
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

    public Long getSystemTypeId() {
        return systemTypeId;
    }

    public void setSystemTypeId(Long systemTypeId) {
        this.systemTypeId = systemTypeId;
    }

    public String getSystemTypeName() {
        return systemTypeName;
    }

    public void setSystemTypeName(String systemTypeName) {
        this.systemTypeName = systemTypeName;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public Date getFoundTime() {
        return foundTime;
    }

    public void setFoundTime(Date foundTime) {
        this.foundTime = foundTime;
    }

    public String getIsReported() {
        return isReported;
    }

    public void setIsReported(String isReported) {
        this.isReported = isReported;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public String getFaultImages() {
        return faultImages;
    }

    public void setFaultImages(String faultImages) {
        this.faultImages = faultImages;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterPhone() {
        return reporterPhone;
    }

    public void setReporterPhone(String reporterPhone) {
        this.reporterPhone = reporterPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }

    public Long getRepairUserId() {
        return repairUserId;
    }

    public void setRepairUserId(Long repairUserId) {
        this.repairUserId = repairUserId;
    }

    public String getRepairPerson() {
        return repairPerson;
    }

    public void setRepairPerson(String repairPerson) {
        this.repairPerson = repairPerson;
    }

    public String getRepairPhone() {
        return repairPhone;
    }

    public void setRepairPhone(String repairPhone) {
        this.repairPhone = repairPhone;
    }

    public String getDispatchBy() {
        return dispatchBy;
    }

    public void setDispatchBy(String dispatchBy) {
        this.dispatchBy = dispatchBy;
    }

    public Date getDispatchTime() {
        return dispatchTime;
    }

    public void setDispatchTime(Date dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    public Date getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(Date acceptTime) {
        this.acceptTime = acceptTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public String getRepairDescription() {
        return repairDescription;
    }

    public void setRepairDescription(String repairDescription) {
        this.repairDescription = repairDescription;
    }

    public String getRepairImages() {
        return repairImages;
    }

    public void setRepairImages(String repairImages) {
        this.repairImages = repairImages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("repairId", getRepairId())
                .append("repairNo", getRepairNo())
                .append("companyId", getCompanyId())
                .append("companyName", getCompanyName())
                .append("systemTypeId", getSystemTypeId())
                .append("systemTypeName", getSystemTypeName())
                .append("equipmentId", getEquipmentId())
                .append("equipmentName", getEquipmentName())
                .append("foundTime", getFoundTime())
                .append("isReported", getIsReported())
                .append("urgencyLevel", getUrgencyLevel())
                .append("faultDescription", getFaultDescription())
                .append("faultImages", getFaultImages())
                .append("reporterName", getReporterName())
                .append("reporterId", getReporterId())
                .append("reporterPhone", getReporterPhone())
                .append("customerAddress", getCustomerAddress())
                .append("repairStatus", getRepairStatus())
                .append("repairUserId", getRepairUserId())
                .append("repairPerson", getRepairPerson())
                .append("repairPhone", getRepairPhone())
                .append("dispatchBy", getDispatchBy())
                .append("dispatchTime", getDispatchTime())
                .append("acceptTime", getAcceptTime())
                .append("startTime", getStartTime())
                .append("completeTime", getCompleteTime())
                .append("repairDescription", getRepairDescription())
                .append("repairImages", getRepairImages())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
