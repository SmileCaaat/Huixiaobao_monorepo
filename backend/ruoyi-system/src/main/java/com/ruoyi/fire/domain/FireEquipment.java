package com.ruoyi.fire.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 消防设备表 fire_equipment
 * 
 * @author ruoyi
 */
public class FireEquipment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 设备ID */
    private Long equipmentId;

    /** 设备编码 */
    private String equipmentCode;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String equipmentName;

    private String equipmentType;

    /** 所属建筑ID */
    private Long buildingId;

    /** 所属建筑名称 */
    @Excel(name = "所属建筑")
    private String buildingName;

    /** 所属客户ID（查询参数） */
    private Long companyId;

    /** 所属客户名称 */
    @Excel(name = "所属客户", prompt = "必填项，且必须为系统中已存在客户")
    private String companyName;

    /** 所在楼层 */
    @Excel(name = "所在楼层")
    private String floorNo;

    /** 系统类型ID（查询参数） */
    private Long systemTypeId;

    /** 系统名称 */
    @Excel(name = "系统名称", combo = { "消防供配电设施", "火灾自动报警系统", "火灾报警控制器", "烟感探测器", "温感探测器", "手动火灾报警按钮", "消火栓按钮", "声光警报器",
            "输入模块", "输出模块", "输入输出模块", "火灾显示盘", "可燃气体探测报警系统", "可燃气体探测器", "消防供水设施", "消防水池", "消防水箱", "消防水泵", "稳压设备",
            "水泵接合器", "消防水管网", "消火栓(消防炮)灭火系统", "室内消火栓箱", "消火栓栓头", "消防水带", "消防水枪", "消火栓泵", "室外消火栓", "自动喷水灭火系统", "喷淋头",
            "湿式报警阀", "干式报警阀", "预作用报警阀", "雨淋报警阀", "水流指示器", "信号阀", "末端试水装置", "喷淋泵", "稳压泵", "气压罐", "气体灭火系统", "七氟丙烷灭火系统",
            "IG541混合气体灭火系统", "二氧化碳灭火系统", "气溶胶灭火系统", "气瓶", "瓶头阀", "选择阀", "喷嘴", "气体灭火控制器", "紧急启停按钮", "放气指示灯",
            "应急照明和疏散指示标志", "应急照明灯", "疏散指示标志灯", "应急照明控制器", "集中电源", "分配电装置", "防火分隔设施", "防火门", "防火卷帘", "防火窗", "防火封堵材料",
            "防烟排烟系统", "排烟风机", "正压送风机", "排烟口", "送风口", "排烟防火阀", "防火阀", "补风系统", "灭火器", "泡沫灭火系统", "泡沫液储罐", "泡沫比例混合器",
            "泡沫产生器", "泡沫炮", "泡沫消火栓", "细水雾灭火系统", "干粉灭火系统", "固定消防炮灭火系统", "电气火灾监控系统", "远程监控系统", "应急广播系统", "消防应急广播",
            "消防专用电话", "消防电话", "消防电梯系统", "厨房设备自动灭火装置", "电动汽车充电桩灭火系统", "隧道消防系统", "大空间智能灭火系统", "其他" })
    private String systemName;

    /** 项目类别 */
    @Excel(name = "项目类别", combo = { "消防供配电设施", "火灾自动报警系统", "可燃气体探测报警系统", "消防供水设施", "消火栓(消防炮)灭火系统", "自动喷水灭火系统", "气体灭火系统",
            "应急照明和疏散指示标志", "防火分隔设施", "防烟排烟系统", "灭火器", "细水雾灭火系统", "电气火灾监控系统", "远程监控系统", "应急广播系统", "消防专用电话", "消防电梯系统",
            "泡沫灭火系统" })
    private String projectCategory;

    /** 具体位置 */
    @Excel(name = "具体位置")
    private String location;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String model;

    /** 设备数量 */
    @Excel(name = "设备数量", cellType = ColumnType.NUMERIC)
    private Integer quantity;

    /** 设备图片 */
    private String image;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;

    /** 安装日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date installDate;

    /** 有效日期（到期日期） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 上次检查日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastCheckDate;

    /** 下次检查日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date nextCheckDate;

    /** 设备状态(normal=正常 warning=预警 fault=故障 expired=过期) */
    private String equipmentStatus;

    /** 使用状态（0=启用 1=停用） */
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    @Size(min = 0, max = 50, message = "设备编码长度不能超过50个字符")
    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    @NotBlank(message = "设备名称不能为空")
    @Size(min = 0, max = 100, message = "设备名称长度不能超过100个字符")
    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
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

    public String getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(String floorNo) {
        this.floorNo = floorNo;
    }

    public Long getSystemTypeId() {
        return systemTypeId;
    }

    public void setSystemTypeId(Long systemTypeId) {
        this.systemTypeId = systemTypeId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getProjectCategory() {
        return projectCategory;
    }

    public void setProjectCategory(String projectCategory) {
        this.projectCategory = projectCategory;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public Date getInstallDate() {
        return installDate;
    }

    public void setInstallDate(Date installDate) {
        this.installDate = installDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Date getLastCheckDate() {
        return lastCheckDate;
    }

    public void setLastCheckDate(Date lastCheckDate) {
        this.lastCheckDate = lastCheckDate;
    }

    public Date getNextCheckDate() {
        return nextCheckDate;
    }

    public void setNextCheckDate(Date nextCheckDate) {
        this.nextCheckDate = nextCheckDate;
    }

    public String getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(String equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
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
                .append("equipmentId", getEquipmentId())
                .append("equipmentCode", getEquipmentCode())
                .append("equipmentName", getEquipmentName())
                .append("equipmentType", getEquipmentType())
                .append("buildingId", getBuildingId())
                .append("buildingName", getBuildingName())
                .append("floorNo", getFloorNo())
                .append("systemName", getSystemName())
                .append("projectCategory", getProjectCategory())
                .append("location", getLocation())
                .append("manufacturer", getManufacturer())
                .append("model", getModel())
                .append("quantity", getQuantity())
                .append("image", getImage())
                .append("productionDate", getProductionDate())
                .append("installDate", getInstallDate())
                .append("expireDate", getExpireDate())
                .append("lastCheckDate", getLastCheckDate())
                .append("nextCheckDate", getNextCheckDate())
                .append("equipmentStatus", getEquipmentStatus())
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
