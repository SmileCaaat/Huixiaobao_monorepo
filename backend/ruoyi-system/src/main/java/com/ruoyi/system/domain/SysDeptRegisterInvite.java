package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 部门注册邀请/二维码 sys_dept_register_invite
 */
public class SysDeptRegisterInvite extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long inviteId;

    /** 公司节点 dept_id */
    private Long companyDeptId;

    /** 目标部门 dept_id */
    private Long deptId;

    /** Token SHA-256 */
    private String tokenHash;

    /** 短邀请码 */
    private String inviteCode;

    /** 0自动通过 / 1人工审核 */
    private String registerMode;

    /** 0启用 / 1停用 */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private Integer useCount;

    /** 0=不限 */
    private Integer useLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUseTime;

    /** 展示用：公司名 */
    private String companyName;

    /** 展示用：部门名 */
    private String deptName;

    public Long getInviteId()
    {
        return inviteId;
    }

    public void setInviteId(Long inviteId)
    {
        this.inviteId = inviteId;
    }

    public Long getCompanyDeptId()
    {
        return companyDeptId;
    }

    public void setCompanyDeptId(Long companyDeptId)
    {
        this.companyDeptId = companyDeptId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getTokenHash()
    {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash)
    {
        this.tokenHash = tokenHash;
    }

    public String getInviteCode()
    {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }

    public String getRegisterMode()
    {
        return registerMode;
    }

    public void setRegisterMode(String registerMode)
    {
        this.registerMode = registerMode;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public Integer getUseCount()
    {
        return useCount;
    }

    public void setUseCount(Integer useCount)
    {
        this.useCount = useCount;
    }

    public Integer getUseLimit()
    {
        return useLimit;
    }

    public void setUseLimit(Integer useLimit)
    {
        this.useLimit = useLimit;
    }

    public Date getLastUseTime()
    {
        return lastUseTime;
    }

    public void setLastUseTime(Date lastUseTime)
    {
        this.lastUseTime = lastUseTime;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }
}
