package com.ruoyi.fire.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 维保客户对象 fire_company
 */
public class FireCompany extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 公司ID */
    private Long companyId;

    /** 公司编码 */
    @Excel(name = "公司编码")
    private String companyCode;

    /** 公司名称 */
    @Excel(name = "客户名称")
    private String companyName;

    /** 是否重点单位(0否 1是) */
    @Excel(name = "重点单位", readConverterExp = "0=否,1=是")
    private String isKeyUnit;

    /** 公司简称 */
    private String shortName;

    /** 公司类型(00一般客户 01重点客户 02VIP客户) */
    private String companyType;

    /** 建筑类别 */
    private String buildingType;

    /** 总占地面积(平方米) */
    private BigDecimal totalLandArea;

    /** 总建筑面积(平方米) */
    private BigDecimal totalBuildingArea;

    /** 项目地址 */
    @Excel(name = "项目地址")
    private String address;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区县 */
    private String district;

    /** 打卡区域中心经度 */
    private Double checkInLongitude;

    /** 打卡区域中心纬度 */
    private Double checkInLatitude;

    /** 打卡范围半径(米) */
    private Integer checkInRadius;

    /** 打卡地址描述 */
    private String checkInAddress;

    /** 联系人/消防安全管理员 */
    private String contactPerson;

    /** 联系电话/报告签收手机号 */
    private String contactPhone;

    /** 联系邮箱 */
    private String contactEmail;

    /** 合同开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date contractStartDate;

    /** 合同结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合同日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date contractEndDate;

    /** 合同状态 */
    private String contractStatus;

    /** 合同ID */
    private Long contractId;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 合同名称 */
    private String contractName;

    /** 录入单位 */
    @Excel(name = "录入单位")
    private String entryUnit;

    /** 关键词检索 */
    private String keyword;

    /** 签到要求(0GPS签到 1GPS+人脸识别签到) */
    private String signRequirement;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 自动消防设施(0无 1有) */
    private String autoFireSystem;

    /** 建筑层数 */
    private Integer buildingFloorCount;

    /** 建筑高度(m) */
    private BigDecimal buildingHeight;

    /** 维保标准 */
    private String maintenanceStandard;

    /** 维保内容 */
    private String maintenanceContent;

    /** 项目概况 */
    private String projectOverview;

    /** 报告模式(0线上 1线下) */
    private String reportMode;

    /** 项目负责人 */
    private String projectLeaderName;

    /** 技术负责人 */
    private String technicalLeaderName;

    /** 物业管理单位 */
    private String propertyManageUnit;

    /** 维保方案文件名 */
    private String maintenanceSchemeName;

    /** 维保方案文件路径 */
    private String maintenanceSchemePath;

    /** 建筑数量 */
    @Excel(name = "建筑数量")
    private Integer buildingCount;

    /** 设备数量 */
    private Integer equipmentCount;

    /** 状态(0正常 1停用) */
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 新增客户时携带的建筑列表（非DB字段，用于前端提交多建筑数据写入fire_building表） */
    private List<FireBuilding> buildingList;

    public List<FireBuilding> getBuildingList() {
        return buildingList;
    }

    public void setBuildingList(List<FireBuilding> buildingList) {
        this.buildingList = buildingList;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIsKeyUnit() {
        return isKeyUnit;
    }

    public void setIsKeyUnit(String isKeyUnit) {
        this.isKeyUnit = isKeyUnit;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
    }

    public BigDecimal getTotalLandArea() {
        return totalLandArea;
    }

    public void setTotalLandArea(BigDecimal totalLandArea) {
        this.totalLandArea = totalLandArea;
    }

    public BigDecimal getTotalBuildingArea() {
        return totalBuildingArea;
    }

    public void setTotalBuildingArea(BigDecimal totalBuildingArea) {
        this.totalBuildingArea = totalBuildingArea;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Double getCheckInLongitude() {
        return checkInLongitude;
    }

    public void setCheckInLongitude(Double checkInLongitude) {
        this.checkInLongitude = checkInLongitude;
    }

    public Double getCheckInLatitude() {
        return checkInLatitude;
    }

    public void setCheckInLatitude(Double checkInLatitude) {
        this.checkInLatitude = checkInLatitude;
    }

    public Integer getCheckInRadius() {
        return checkInRadius;
    }

    public void setCheckInRadius(Integer checkInRadius) {
        this.checkInRadius = checkInRadius;
    }

    public String getCheckInAddress() {
        return checkInAddress;
    }

    public void setCheckInAddress(String checkInAddress) {
        this.checkInAddress = checkInAddress;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Date getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(Date contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public String getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
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

    public String getEntryUnit() {
        return entryUnit;
    }

    public void setEntryUnit(String entryUnit) {
        this.entryUnit = entryUnit;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSignRequirement() {
        return signRequirement;
    }

    public void setSignRequirement(String signRequirement) {
        this.signRequirement = signRequirement;
    }

    public String getCreditCode() {
        return creditCode;
    }

    public void setCreditCode(String creditCode) {
        this.creditCode = creditCode;
    }

    public String getAutoFireSystem() {
        return autoFireSystem;
    }

    public void setAutoFireSystem(String autoFireSystem) {
        this.autoFireSystem = autoFireSystem;
    }

    public Integer getBuildingFloorCount() {
        return buildingFloorCount;
    }

    public void setBuildingFloorCount(Integer buildingFloorCount) {
        this.buildingFloorCount = buildingFloorCount;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public String getMaintenanceStandard() {
        return maintenanceStandard;
    }

    public void setMaintenanceStandard(String maintenanceStandard) {
        this.maintenanceStandard = maintenanceStandard;
    }

    public String getMaintenanceContent() {
        return maintenanceContent;
    }

    public void setMaintenanceContent(String maintenanceContent) {
        this.maintenanceContent = maintenanceContent;
    }

    public String getProjectOverview() {
        return projectOverview;
    }

    public void setProjectOverview(String projectOverview) {
        this.projectOverview = projectOverview;
    }

    public String getReportMode() {
        return reportMode;
    }

    public void setReportMode(String reportMode) {
        this.reportMode = reportMode;
    }

    public String getProjectLeaderName() {
        return projectLeaderName;
    }

    public void setProjectLeaderName(String projectLeaderName) {
        this.projectLeaderName = projectLeaderName;
    }

    public String getTechnicalLeaderName() {
        return technicalLeaderName;
    }

    public void setTechnicalLeaderName(String technicalLeaderName) {
        this.technicalLeaderName = technicalLeaderName;
    }

    public String getPropertyManageUnit() {
        return propertyManageUnit;
    }

    public void setPropertyManageUnit(String propertyManageUnit) {
        this.propertyManageUnit = propertyManageUnit;
    }

    public String getMaintenanceSchemeName() {
        return maintenanceSchemeName;
    }

    public void setMaintenanceSchemeName(String maintenanceSchemeName) {
        this.maintenanceSchemeName = maintenanceSchemeName;
    }

    public String getMaintenanceSchemePath() {
        return maintenanceSchemePath;
    }

    public void setMaintenanceSchemePath(String maintenanceSchemePath) {
        this.maintenanceSchemePath = maintenanceSchemePath;
    }

    public Integer getBuildingCount() {
        return buildingCount;
    }

    public void setBuildingCount(Integer buildingCount) {
        this.buildingCount = buildingCount;
    }

    public Integer getEquipmentCount() {
        return equipmentCount;
    }

    public void setEquipmentCount(Integer equipmentCount) {
        this.equipmentCount = equipmentCount;
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
            .append("companyId", getCompanyId())
            .append("companyCode", getCompanyCode())
            .append("companyName", getCompanyName())
            .append("isKeyUnit", getIsKeyUnit())
            .append("shortName", getShortName())
            .append("companyType", getCompanyType())
            .append("buildingType", getBuildingType())
            .append("totalLandArea", getTotalLandArea())
            .append("totalBuildingArea", getTotalBuildingArea())
            .append("address", getAddress())
            .append("province", getProvince())
            .append("city", getCity())
            .append("district", getDistrict())
            .append("checkInLongitude", getCheckInLongitude())
            .append("checkInLatitude", getCheckInLatitude())
            .append("checkInRadius", getCheckInRadius())
            .append("checkInAddress", getCheckInAddress())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("contactEmail", getContactEmail())
            .append("contractStartDate", getContractStartDate())
            .append("contractEndDate", getContractEndDate())
            .append("contractStatus", getContractStatus())
            .append("contractId", getContractId())
            .append("projectName", getProjectName())
            .append("contractName", getContractName())
            .append("entryUnit", getEntryUnit())
            .append("signRequirement", getSignRequirement())
            .append("creditCode", getCreditCode())
            .append("autoFireSystem", getAutoFireSystem())
            .append("buildingFloorCount", getBuildingFloorCount())
            .append("buildingHeight", getBuildingHeight())
            .append("maintenanceStandard", getMaintenanceStandard())
            .append("maintenanceContent", getMaintenanceContent())
            .append("projectOverview", getProjectOverview())
            .append("reportMode", getReportMode())
            .append("projectLeaderName", getProjectLeaderName())
            .append("technicalLeaderName", getTechnicalLeaderName())
            .append("propertyManageUnit", getPropertyManageUnit())
            .append("maintenanceSchemeName", getMaintenanceSchemeName())
            .append("maintenanceSchemePath", getMaintenanceSchemePath())
            .append("buildingCount", getBuildingCount())
            .append("equipmentCount", getEquipmentCount())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
