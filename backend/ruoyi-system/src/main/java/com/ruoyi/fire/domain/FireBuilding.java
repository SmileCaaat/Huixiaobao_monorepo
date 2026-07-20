package com.ruoyi.fire.domain;

import java.math.BigDecimal;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 建筑信息表 fire_building
 * 
 * @author ruoyi
 */
public class FireBuilding extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 建筑ID */
    @Excel(name = "建筑ID", cellType = ColumnType.NUMERIC)
    private Long buildingId;

    /** 所属客户ID */
    private Long companyId;

    /** 所属客户名称 */
    @Excel(name = "所属客户")
    private String companyName;

    /** 建筑编码 */
    @Excel(name = "建筑编码")
    private String buildingCode;

    /** 建筑名称 */
    @Excel(name = "建筑名称")
    private String buildingName;

    /** 建筑类型/建筑类别 */
    @Excel(name = "建筑类别", readConverterExp = "type1_high_rise_civil=一类高层民用建筑,type2_high_rise_civil=二类高层民用建筑,high_rise_factory=高层厂房,high_rise_warehouse=高层库房,single_multi_civil=单、多层民用建筑,single_multi_factory=单、多层厂房,single_multi_warehouse=单、多层库房,underground=地下建筑,tunnel_culvert=隧道、涵洞,other=其他建筑")
    private String buildingType;
    
    /** 建筑类别翻译（用于前端显示） */
    private String buildingTypeText;

    /** 自动消防设施(0无 1有) */
    @Excel(name = "自动消防设施", readConverterExp = "0=无,1=有")
    private String autoFireSystem;

    /** 建筑地址 */
    @Excel(name = "建筑地址")
    private String address;

    /** 占地面积(平方米) */
    @Excel(name = "占地面积(㎡)", cellType = ColumnType.NUMERIC)
    private BigDecimal landArea;

    /** 建筑面积(平方米) */
    @Excel(name = "建筑面积(㎡)", cellType = ColumnType.NUMERIC)
    private BigDecimal area;

    /** 楼层数(总层数) */
    @Excel(name = "楼层数(层)", cellType = ColumnType.NUMERIC)
    private Integer floorCount;

    /** 建筑高度(米) */
    @Excel(name = "建筑高度(m)", cellType = ColumnType.NUMERIC)
    private BigDecimal buildingHeight;

    /** 地上层层数 */
    @Excel(name = "地上层层数", cellType = ColumnType.NUMERIC)
    private Integer aboveGroundFloors;

    /** 地下层层数 */
    @Excel(name = "地下层层数", cellType = ColumnType.NUMERIC)
    private Integer undergroundFloors;

    /** 安全出口数(个) */
    @Excel(name = "安全出口数(个)", cellType = ColumnType.NUMERIC)
    private Integer emergencyExits;

    /** 疏散楼梯数(个) */
    @Excel(name = "疏散楼梯数(个)", cellType = ColumnType.NUMERIC)
    private Integer evacuationStairs;

    /** 消防电梯数(个) */
    @Excel(name = "消防电梯数(个)", cellType = ColumnType.NUMERIC)
    private Integer fireElevators;

    /** 避难层位置 */
    @Excel(name = "避难层位置")
    private String refugeFloor;

    /** 消防等级 */
    @Excel(name = "消防等级", readConverterExp = "first=一级,second=二级,third=三级")
    private String fireLevel;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
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

    @NotBlank(message = "建筑编码不能为空")
    @Size(min = 0, max = 50, message = "建筑编码长度不能超过50个字符")
    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    @NotBlank(message = "建筑名称不能为空")
    @Size(min = 0, max = 100, message = "建筑名称长度不能超过100个字符")
    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
    }
    
    public String getBuildingTypeText() {
        return buildingTypeText;
    }
    
    public void setBuildingTypeText(String buildingTypeText) {
        this.buildingTypeText = buildingTypeText;
    }

    public String getAutoFireSystem() {
        return autoFireSystem;
    }

    public void setAutoFireSystem(String autoFireSystem) {
        this.autoFireSystem = autoFireSystem;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLandArea() {
        return landArea;
    }

    public void setLandArea(BigDecimal landArea) {
        this.landArea = landArea;
    }

    public Integer getFloorCount() {
        return floorCount;
    }

    public void setFloorCount(Integer floorCount) {
        this.floorCount = floorCount;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public Integer getAboveGroundFloors() {
        return aboveGroundFloors;
    }

    public void setAboveGroundFloors(Integer aboveGroundFloors) {
        this.aboveGroundFloors = aboveGroundFloors;
    }

    public Integer getUndergroundFloors() {
        return undergroundFloors;
    }

    public void setUndergroundFloors(Integer undergroundFloors) {
        this.undergroundFloors = undergroundFloors;
    }

    public Integer getEmergencyExits() {
        return emergencyExits;
    }

    public void setEmergencyExits(Integer emergencyExits) {
        this.emergencyExits = emergencyExits;
    }

    public Integer getEvacuationStairs() {
        return evacuationStairs;
    }

    public void setEvacuationStairs(Integer evacuationStairs) {
        this.evacuationStairs = evacuationStairs;
    }

    public Integer getFireElevators() {
        return fireElevators;
    }

    public void setFireElevators(Integer fireElevators) {
        this.fireElevators = fireElevators;
    }

    public String getRefugeFloor() {
        return refugeFloor;
    }

    public void setRefugeFloor(String refugeFloor) {
        this.refugeFloor = refugeFloor;
    }

    public String getFireLevel() {
        return fireLevel;
    }

    public void setFireLevel(String fireLevel) {
        this.fireLevel = fireLevel;
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
                .append("buildingId", getBuildingId())
                .append("buildingCode", getBuildingCode())
                .append("buildingName", getBuildingName())
                .append("buildingType", getBuildingType())
                .append("autoFireSystem", getAutoFireSystem())
                .append("address", getAddress())
                .append("landArea", getLandArea())
                .append("area", getArea())
                .append("floorCount", getFloorCount())
                .append("buildingHeight", getBuildingHeight())
                .append("aboveGroundFloors", getAboveGroundFloors())
                .append("undergroundFloors", getUndergroundFloors())
                .append("emergencyExits", getEmergencyExits())
                .append("evacuationStairs", getEvacuationStairs())
                .append("fireElevators", getFireElevators())
                .append("refugeFloor", getRefugeFloor())
                .append("fireLevel", getFireLevel())
                .append("contactPerson", getContactPerson())
                .append("contactPhone", getContactPhone())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
