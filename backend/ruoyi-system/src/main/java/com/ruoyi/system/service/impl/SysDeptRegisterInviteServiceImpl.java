package com.ruoyi.system.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.QrCodeUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDeptRegisterInvite;
import com.ruoyi.system.mapper.SysDeptRegisterInviteMapper;
import com.ruoyi.system.service.ISysDeptRegisterInviteService;
import com.ruoyi.system.service.ISysDeptService;

/**
 * ����ע������ʵ��
 */
@Service
public class SysDeptRegisterInviteServiceImpl implements ISysDeptRegisterInviteService
{
    private static final int TOKEN_BYTES = 32;
    private static final int INVITE_CODE_LEN = 8;
    private static final int EXPIRE_DAYS = 7;
    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private SysDeptRegisterInviteMapper inviteMapper;

    @Autowired
    private ISysDeptService deptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generate(Long deptId, String registerMode, Integer useLimit, String createBy)
    {
        SysDept dept = requireActiveDept(deptId);
        if (dept.getParentId() == null || dept.getParentId() == 0L)
        {
            throw new ServiceException("\u8bf7\u9009\u62e9\u5177\u4f53\u90e8\u95e8\u751f\u6210\u6ce8\u518c\u4e8c\u7ef4\u7801\uff0c\u4e0d\u80fd\u5bf9\u516c\u53f8\u6839\u8282\u70b9\u751f\u6210");
        }
        Long companyDeptId = resolveCompanyDeptId(dept);
        SysDept company = requireActiveDept(companyDeptId);

        inviteMapper.disableActiveByDeptId(deptId, createBy);

        String rawToken = randomToken();
        String inviteCode = uniqueInviteCode();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, EXPIRE_DAYS);

        SysDeptRegisterInvite invite = new SysDeptRegisterInvite();
        invite.setCompanyDeptId(companyDeptId);
        invite.setDeptId(deptId);
        invite.setTokenHash(sha256Hex(rawToken));
        invite.setInviteCode(inviteCode);
        invite.setRegisterMode(StringUtils.isEmpty(registerMode) ? "0" : registerMode);
        invite.setStatus("0");
        invite.setExpireTime(cal.getTime());
        invite.setUseCount(0);
        invite.setUseLimit(useLimit == null ? 0 : useLimit);
        invite.setCreateBy(createBy);
        inviteMapper.insertInvite(invite);

        Map<String, Object> data = toMetaMap(invite, company.getDeptName(), dept.getDeptName());
        data.put("rawToken", rawToken);
        data.put("inviteCode", inviteCode);
        String registerUrl = buildRegisterUrl(null, rawToken);
        data.put("registerUrl", registerUrl);
        data.put("qrBase64", QrCodeUtils.generateQrCodeBase64(registerUrl));
        return data;
    }

    @Override
    public Map<String, Object> getActiveMeta(Long deptId)
    {
        SysDeptRegisterInvite invite = inviteMapper.selectActiveByDeptId(deptId);
        if (invite == null)
        {
            return null;
        }
        Map<String, Object> data = toMetaMap(invite, invite.getCompanyName(), invite.getDeptName());
        // ������ token ʱ�޷�����ͬһ���ӣ����ض���ҳ��ʾ
        String hintUrl = "/register?inviteCode=" + invite.getInviteCode();
        data.put("registerUrlHint", hintUrl);
        data.put("qrBase64", QrCodeUtils.generateQrCodeBase64(hintUrl));
        data.put("hasRawToken", false);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disableByDeptId(Long deptId, String updateBy)
    {
        return inviteMapper.disableActiveByDeptId(deptId, updateBy);
    }

    @Override
    public Map<String, Object> resolve(String inviteToken, String inviteCode)
    {
        SysDeptRegisterInvite invite = findInvite(inviteToken, inviteCode);
        if (invite == null)
        {
            throw new ServiceException("\u9080\u8bf7\u65e0\u6548\u6216\u5df2\u5931\u6548");
        }
        String reason = validityReason(invite);
        Map<String, Object> data = new HashMap<>();
        data.put("valid", reason == null);
        data.put("message", reason == null ? "ok" : reason);
        data.put("companyName", invite.getCompanyName());
        data.put("deptName", invite.getDeptName());
        data.put("registerMode", invite.getRegisterMode());
        data.put("expireTime", invite.getExpireTime());
        data.put("inviteCode", invite.getInviteCode());
        // ������ deptId / companyDeptId�����۸�
        return data;
    }

    @Override
    public SysDeptRegisterInvite validateForRegister(String inviteToken, String inviteCode)
    {
        SysDeptRegisterInvite invite = findInvite(inviteToken, inviteCode);
        if (invite == null)
        {
            return null;
        }
        if (validityReason(invite) != null)
        {
            return null;
        }
        return invite;
    }

    @Override
    public void markUsed(Long inviteId)
    {
        if (inviteId != null)
        {
            inviteMapper.increaseUseCount(inviteId);
        }
    }

    @Override
    public String buildRegisterUrl(String siteRoot, String rawToken)
    {
        String path = "/register?inviteToken=" + rawToken;
        if (StringUtils.isEmpty(siteRoot))
        {
            return path;
        }
        String root = siteRoot.endsWith("/") ? siteRoot.substring(0, siteRoot.length() - 1) : siteRoot;
        return root + path;
    }

    private SysDeptRegisterInvite findInvite(String inviteToken, String inviteCode)
    {
        if (StringUtils.isNotEmpty(inviteToken))
        {
            return inviteMapper.selectByTokenHash(sha256Hex(inviteToken.trim()));
        }
        if (StringUtils.isNotEmpty(inviteCode))
        {
            return inviteMapper.selectByInviteCode(inviteCode.trim().toUpperCase());
        }
        return null;
    }

    private String validityReason(SysDeptRegisterInvite invite)
    {
        if (invite == null)
        {
            return "\u9080\u8bf7\u65e0\u6548\u6216\u5df2\u5931\u6548";
        }
        if (!"0".equals(invite.getStatus()))
        {
            return "\u9080\u8bf7\u7801\u5df2\u505c\u7528";
        }
        if (invite.getExpireTime() != null && invite.getExpireTime().before(new Date()))
        {
            return "\u9080\u8bf7\u7801\u5df2\u8fc7\u671f\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u91cd\u65b0\u751f\u6210";
        }
        if (invite.getUseLimit() != null && invite.getUseLimit() > 0
                && invite.getUseCount() != null && invite.getUseCount() >= invite.getUseLimit())
        {
            return "\u9080\u8bf7\u7801\u4f7f\u7528\u6b21\u6570\u5df2\u8fbe\u4e0a\u9650";
        }
        try
        {
            SysDept dept = requireActiveDept(invite.getDeptId());
            SysDept company = requireActiveDept(invite.getCompanyDeptId());
            Long resolvedCompany = resolveCompanyDeptId(dept);
            if (!invite.getCompanyDeptId().equals(resolvedCompany)
                    && !invite.getCompanyDeptId().equals(company.getDeptId()))
            {
                return "\u7ec4\u7ec7\u5173\u7cfb\u5df2\u53d8\u66f4\uff0c\u9080\u8bf7\u5931\u6548";
            }
            if (StringUtils.isNotEmpty(dept.getAncestors())
                    && !dept.getAncestors().contains(String.valueOf(invite.getCompanyDeptId()))
                    && !invite.getCompanyDeptId().equals(dept.getParentId())
                    && !invite.getCompanyDeptId().equals(dept.getDeptId()))
            {
                // ancestors ͨ������˾ id���������б��������� parent ��ƥ��
                Long walk = resolveCompanyDeptId(dept);
                if (!invite.getCompanyDeptId().equals(walk))
                {
                    return "\u90e8\u95e8\u4e0d\u5c5e\u4e8e\u9080\u8bf7\u516c\u53f8";
                }
            }
        }
        catch (ServiceException e)
        {
            return e.getMessage();
        }
        return null;
    }

    private SysDept requireActiveDept(Long deptId)
    {
        if (deptId == null)
        {
            throw new ServiceException("\u90e8\u95e8\u4e0d\u5b58\u5728");
        }
        SysDept dept = deptService.selectDeptById(deptId);
        if (dept == null || "2".equals(dept.getDelFlag()))
        {
            throw new ServiceException("\u90e8\u95e8\u4e0d\u5b58\u5728\u6216\u5df2\u5220\u9664");
        }
        if ("1".equals(dept.getStatus()))
        {
            throw new ServiceException("\u90e8\u95e8\u5df2\u505c\u7528");
        }
        return dept;
    }

    private Long resolveCompanyDeptId(SysDept dept)
    {
        if (dept.getParentId() == null || dept.getParentId() == 0L)
        {
            return dept.getDeptId();
        }
        if (StringUtils.isNotEmpty(dept.getAncestors()))
        {
            String[] parts = dept.getAncestors().split(",");
            for (String part : parts)
            {
                if (StringUtils.isEmpty(part) || "0".equals(part))
                {
                    continue;
                }
                try
                {
                    return Long.parseLong(part.trim());
                }
                catch (NumberFormatException ignored)
                {
                }
            }
        }
        return dept.getParentId();
    }

    private Map<String, Object> toMetaMap(SysDeptRegisterInvite invite, String companyName, String deptName)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("inviteId", invite.getInviteId());
        data.put("deptId", invite.getDeptId());
        data.put("companyDeptId", invite.getCompanyDeptId());
        data.put("companyName", companyName);
        data.put("deptName", deptName);
        data.put("inviteCode", invite.getInviteCode());
        data.put("registerMode", invite.getRegisterMode());
        data.put("status", invite.getStatus());
        data.put("expireTime", invite.getExpireTime());
        data.put("useCount", invite.getUseCount());
        data.put("useLimit", invite.getUseLimit());
        data.put("createTime", invite.getCreateTime());
        int remain = -1;
        if (invite.getUseLimit() != null && invite.getUseLimit() > 0)
        {
            remain = Math.max(0, invite.getUseLimit() - (invite.getUseCount() == null ? 0 : invite.getUseCount()));
        }
        data.put("remainCount", remain);
        return data;
    }

    private String randomToken()
    {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String uniqueInviteCode()
    {
        for (int i = 0; i < 20; i++)
        {
            String code = randomInviteCode();
            if (inviteMapper.selectByInviteCode(code) == null)
            {
                return code;
            }
        }
        throw new ServiceException("\u751f\u6210\u9080\u8bf7\u77ed\u7801\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5");
    }

    private String randomInviteCode()
    {
        char[] buf = new char[INVITE_CODE_LEN];
        for (int i = 0; i < INVITE_CODE_LEN; i++)
        {
            buf[i] = CODE_CHARS[RANDOM.nextInt(CODE_CHARS.length)];
        }
        return new String(buf);
    }

    public static String sha256Hex(String raw)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest)
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new ServiceException("Token \u6458\u8981\u5931\u8d25");
        }
    }
}
