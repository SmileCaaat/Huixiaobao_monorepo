package com.ruoyi.fire.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户公司关联对象 fire_user_company（项目成员）
 * roleType: 0组员 1项目负责人 2客户管理员 3维保组长
 */
public class FireUserCompany {
    private Long id;
    private Long userId;
    private Long companyId;
    /** 0组员 1项目负责人 2客户管理员 3维保组长 */
    private String roleType;
    /** inner/outer/all */
    private String bizLine;
    /** 0有效 1退出 */
    private String status;
    private Date joinTime;
    private Date leaveTime;
    private String createBy;
    private Date createTime;
    private String userName;
    private String loginName;
    private String phonenumber;
    private String companyName;

    public static final String ROLE_MEMBER = "0";
    public static final String ROLE_PROJECT_MANAGER = "1";
    public static final String ROLE_COMPANY_ADMIN = "2";
    public static final String ROLE_TEAM_LEADER = "3";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }
    public String getBizLine() { return bizLine; }
    public void setBizLine(String bizLine) { this.bizLine = bizLine; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }
    public Date getLeaveTime() { return leaveTime; }
    public void setLeaveTime(Date leaveTime) { this.leaveTime = leaveTime; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", id).append("userId", userId).append("companyId", companyId)
                .append("roleType", roleType).append("bizLine", bizLine).append("status", status)
                .toString();
    }
}
