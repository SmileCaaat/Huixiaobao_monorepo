package com.ruoyi.system.service;

import java.util.Map;
import com.ruoyi.system.domain.SysDeptRegisterInvite;

/**
 * 部门注册邀请/二维码服务
 */
public interface ISysDeptRegisterInviteService
{
    /**
     * 生成或重新生成邀请（旧码停用，新码 7 天）
     *
     * @return 含 rawToken（仅本次）、qrBase64、元数据
     */
    Map<String, Object> generate(Long deptId, String registerMode, Integer useLimit, String createBy);

    /** 当前有效邀请元数据（不含 raw token）+ 二维码图（基于重新拼装的公开链接占位，下载需已有 raw 时另存） */
    Map<String, Object> getActiveMeta(Long deptId);

    /** 停用当前有效码 */
    int disableByDeptId(Long deptId, String updateBy);

    /** 解析 token 或短码，返回展示信息；无效抛异常 */
    Map<String, Object> resolve(String inviteToken, String inviteCode);

    /** 校验邀请可用于自动/人工注册；返回 invite 实体，无效返回 null */
    SysDeptRegisterInvite validateForRegister(String inviteToken, String inviteCode);

    /** 注册成功后增加使用次数 */
    void markUsed(Long inviteId);

    /** 根据公开站点根地址组装注册 URL（仅当持有 rawToken 时） */
    String buildRegisterUrl(String siteRoot, String rawToken);
}
