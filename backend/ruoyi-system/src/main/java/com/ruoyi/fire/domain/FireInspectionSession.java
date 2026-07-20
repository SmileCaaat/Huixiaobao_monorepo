package com.ruoyi.fire.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 巡检会话对象 fire_inspection_session
 * 
 * @author ruoyi
 */
public class FireInspectionSession extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long sessionId;

    /** 用户ID */
    private Long userId;

    /** 用户姓名 */
    private String userName;

    /** 巡检公司ID */
    private Long companyId;

    /** 公司名称 */
    private String companyName;

    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkInTime;

    /** 签到经度 */
    private Double checkInLongitude;

    /** 签到纬度 */
    private Double checkInLatitude;

    /** 签到地址 */
    private String checkInAddress;

    /** 签到备注 */
    private String checkInRemark;

    /** 签退时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkOutTime;

    /** 签退经度 */
    private Double checkOutLongitude;

    /** 签退纬度 */
    private Double checkOutLatitude;

    /** 签退地址 */
    private String checkOutAddress;

    /** 签退备注 */
    private String checkOutRemark;

    /** 状态(0已签到 1已签退) */
    private String status;

    /** 会话日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date sessionDate;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
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

    public String getCheckInAddress() {
        return checkInAddress;
    }

    public void setCheckInAddress(String checkInAddress) {
        this.checkInAddress = checkInAddress;
    }

    public String getCheckInRemark() {
        return checkInRemark;
    }

    public void setCheckInRemark(String checkInRemark) {
        this.checkInRemark = checkInRemark;
    }

    public Date getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Date checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Double getCheckOutLongitude() {
        return checkOutLongitude;
    }

    public void setCheckOutLongitude(Double checkOutLongitude) {
        this.checkOutLongitude = checkOutLongitude;
    }

    public Double getCheckOutLatitude() {
        return checkOutLatitude;
    }

    public void setCheckOutLatitude(Double checkOutLatitude) {
        this.checkOutLatitude = checkOutLatitude;
    }

    public String getCheckOutAddress() {
        return checkOutAddress;
    }

    public void setCheckOutAddress(String checkOutAddress) {
        this.checkOutAddress = checkOutAddress;
    }

    public String getCheckOutRemark() {
        return checkOutRemark;
    }

    public void setCheckOutRemark(String checkOutRemark) {
        this.checkOutRemark = checkOutRemark;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("sessionId", getSessionId())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("companyId", getCompanyId())
                .append("companyName", getCompanyName())
                .append("checkInTime", getCheckInTime())
                .append("checkInAddress", getCheckInAddress())
                .append("checkOutTime", getCheckOutTime())
                .append("status", getStatus())
                .append("sessionDate", getSessionDate())
                .append("createTime", getCreateTime())
                .toString();
    }
}
