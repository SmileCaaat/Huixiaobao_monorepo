package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysDeptRegisterInvite;

/**
 * ²¿ÃÅ×¢²áÑûÇë Mapper
 */
public interface SysDeptRegisterInviteMapper
{
    SysDeptRegisterInvite selectInviteById(Long inviteId);

    SysDeptRegisterInvite selectActiveByDeptId(Long deptId);

    SysDeptRegisterInvite selectByTokenHash(String tokenHash);

    SysDeptRegisterInvite selectByInviteCode(String inviteCode);

    List<SysDeptRegisterInvite> selectInviteList(SysDeptRegisterInvite query);

    int insertInvite(SysDeptRegisterInvite invite);

    int updateInvite(SysDeptRegisterInvite invite);

    int disableInvite(@Param("inviteId") Long inviteId, @Param("updateBy") String updateBy);

    int disableActiveByDeptId(@Param("deptId") Long deptId, @Param("updateBy") String updateBy);

    int increaseUseCount(Long inviteId);
}
