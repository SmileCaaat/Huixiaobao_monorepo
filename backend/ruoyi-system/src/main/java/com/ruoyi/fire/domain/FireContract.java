package com.ruoyi.fire.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 维保合同对象 fire_contract
 */
public class FireContract extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 合同ID */
    private Long contractId;

    /** 客户ID */
    private Long companyId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String companyName;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 合同名称 */
    @Excel(name = "合同名称")
    private String contractName;

    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractNo;

    /** 合同金额 */
    @Excel(name = "合同金额")
    private BigDecimal contractAmount;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 录入单位 */
    @Excel(name = "录入单位")
    private String entryUnit;

    /** 附件名称 */
    private String attachmentName;

    /** 附件路径 */
    private String attachmentPath;

    /** 终止标记（0否 1是） */
    private String terminateFlag;

    /** 终止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date terminateTime;

    /** 续签来源合同ID */
    private Long renewedFromId;

    /** 删除标记 */
    private String delFlag;

    /** 关键字（查询） */
    private String keyword;

    /** 对象池类型（查询）：0有效 1即将过期 2过期 3未生效 */
    private String poolType;

    /** 合同状态（展示） */
    @Excel(name = "合同状态", readConverterExp = "valid=有效,expiring=即将过期,expired=过期,inactive=未生效")
    private String contractStatus;

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public BigDecimal getContractAmount() {
        return contractAmount;
    }

    public void setContractAmount(BigDecimal contractAmount) {
        this.contractAmount = contractAmount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getEntryUnit() {
        return entryUnit;
    }

    public void setEntryUnit(String entryUnit) {
        this.entryUnit = entryUnit;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getTerminateFlag() {
        return terminateFlag;
    }

    public void setTerminateFlag(String terminateFlag) {
        this.terminateFlag = terminateFlag;
    }

    public Date getTerminateTime() {
        return terminateTime;
    }

    public void setTerminateTime(Date terminateTime) {
        this.terminateTime = terminateTime;
    }

    public Long getRenewedFromId() {
        return renewedFromId;
    }

    public void setRenewedFromId(Long renewedFromId) {
        this.renewedFromId = renewedFromId;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getPoolType() {
        return poolType;
    }

    public void setPoolType(String poolType) {
        this.poolType = poolType;
    }

    public String getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("contractId", getContractId())
                .append("companyId", getCompanyId())
                .append("companyName", getCompanyName())
                .append("projectName", getProjectName())
                .append("contractName", getContractName())
                .append("contractNo", getContractNo())
                .append("contractAmount", getContractAmount())
                .append("startDate", getStartDate())
                .append("endDate", getEndDate())
                .append("entryUnit", getEntryUnit())
                .append("attachmentName", getAttachmentName())
                .append("attachmentPath", getAttachmentPath())
                .append("terminateFlag", getTerminateFlag())
                .append("terminateTime", getTerminateTime())
                .append("renewedFromId", getRenewedFromId())
                .append("delFlag", getDelFlag())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
