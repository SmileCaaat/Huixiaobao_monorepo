package com.ruoyi.fire.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 维保签到对象 fire_check_in
 * 
 * @author ruoyi
 */
public class FireCheckIn extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 签到ID */
    private Long checkInId;

    /** 所属客户ID */
    private Long companyId;

    /** 所属客户名称 */
    @Excel(name = "所属客户")
    private String companyName;

    /** 关联任务ID */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 任务名称 */
    @Excel(name = "任务名称")
    private String taskName;

    /** 签到用户ID */
    private Long userId;

    /** 签到用户姓名 */
    @Excel(name = "签到人")
    private String userName;

    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "签到时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkInTime;

    /** 经度 */
    @Excel(name = "经度")
    private Double longitude;

    /** 纬度 */
    @Excel(name = "纬度")
    private Double latitude;

    /** 签到地址 */
    @Excel(name = "签到地址")
    private String address;

    /** 签到类型（0签到 1签退） */
    @Excel(name = "签到类型", readConverterExp = "0=签到,1=签退")
    private String checkInType;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 签到图片列表 */
    private List<FireCheckInImage> images;

    /** 地址录入方式（auto/manual），仅请求校验用，不入库 */
    private String addressMode;

    /** 自动定位展示地址（不入库） */
    private String locatedAddress;

    /** 手动输入地址（不入库） */
    private String manualAddress;

    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCheckInType() {
        return checkInType;
    }

    public void setCheckInType(String checkInType) {
        this.checkInType = checkInType;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public List<FireCheckInImage> getImages() {
        return images;
    }

    public void setImages(List<FireCheckInImage> images) {
        this.images = images;
    }

    public String getAddressMode() {
        return addressMode;
    }

    public void setAddressMode(String addressMode) {
        this.addressMode = addressMode;
    }

    public String getLocatedAddress() {
        return locatedAddress;
    }

    public void setLocatedAddress(String locatedAddress) {
        this.locatedAddress = locatedAddress;
    }

    public String getManualAddress() {
        return manualAddress;
    }

    public void setManualAddress(String manualAddress) {
        this.manualAddress = manualAddress;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("checkInId", getCheckInId())
                .append("taskId", getTaskId())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("checkInTime", getCheckInTime())
                .append("longitude", getLongitude())
                .append("latitude", getLatitude())
                .append("address", getAddress())
                .append("checkInType", getCheckInType())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
