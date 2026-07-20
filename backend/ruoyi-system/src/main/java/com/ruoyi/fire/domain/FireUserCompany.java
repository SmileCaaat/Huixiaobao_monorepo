package com.ruoyi.fire.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户公司关联对象 fire_user_company
 *
 * @author ruoyi
 */
public class FireUserCompany {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 公司ID */
    private Long companyId;

    /** 角色类型(0普通巡检员 1项目负责人 2管理员) */
    private String roleType;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private Date createTime;

    /** 用户名称（查询用） */
    private String userName;

    /** 登录名称（查询用） */
    private String loginName;

    /** 手机号（查询用） */
    private String phonenumber;

    /** 公司名称（查询用） */
    private String companyName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("companyId", getCompanyId())
                .append("roleType", getRoleType())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("userName", getUserName())
                .append("loginName", getLoginName())
                .append("phonenumber", getPhonenumber())
                .append("companyName", getCompanyName())
                .toString();
    }
}
