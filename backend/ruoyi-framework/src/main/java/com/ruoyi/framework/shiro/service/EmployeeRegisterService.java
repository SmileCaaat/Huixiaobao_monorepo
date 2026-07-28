package com.ruoyi.framework.shiro.service;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.IpUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.domain.SysDeptRegisterInvite;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISysDeptRegisterInviteService;
import com.ruoyi.system.service.ISysUserService;

/**
 * Employee self-register via invite token/code + image captcha (no SMS).
 */
@Component
public class EmployeeRegisterService
{
    private static final int MAX_REG_PER_IP_HOUR = 20;
    private static final int MAX_REG_PER_PHONE_HOUR = 5;
    private static final int MAX_REG_PER_INVITE_HOUR = 30;

    private final Map<String, HourCounter> rateCounters = new ConcurrentHashMap<>();

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private ISysDeptRegisterInviteService inviteService;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * @return empty string on success; otherwise error message
     */
    @Transactional(rollbackFor = Exception.class)
    public String register(SysUser form, String inviteToken, String inviteCode, Map<String, Object> result)
    {
        String userName = form.getUserName();
        String phonenumber = form.getPhonenumber();
        String password = form.getPassword();

        if (StringUtils.isEmpty(userName))
        {
            return "\u59d3\u540d\u4e0d\u80fd\u4e3a\u7a7a";
        }
        if (StringUtils.isEmpty(phonenumber) || !phonenumber.matches("^1\\d{10}$"))
        {
            return "\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e";
        }
        if (StringUtils.isEmpty(password))
        {
            return "\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a";
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            return "\u5bc6\u7801\u957f\u5ea6\u5fc5\u987b\u57285\u523020\u4e2a\u5b57\u7b26\u4e4b\u95f4";
        }

        // Ignore client-side org ids; invite resolution is authoritative.
        form.setDeptId(null);

        String clientIp = safeClientIp();
        if (!allowRate("ip:" + clientIp, MAX_REG_PER_IP_HOUR))
        {
            return "\u6ce8\u518c\u8bf7\u6c42\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";
        }
        if (!allowRate("phone:" + phonenumber, MAX_REG_PER_PHONE_HOUR))
        {
            return "\u8be5\u624b\u673a\u53f7\u63d0\u4ea4\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";
        }

        if (userService.isPhonenumberTaken(phonenumber))
        {
            return "\u8be5\u624b\u673a\u53f7\u5df2\u6ce8\u518c";
        }

        String loginName = phonenumber;
        form.setLoginName(loginName);
        if (!userService.checkLoginNameUnique(form))
        {
            return "\u6ce8\u518c\u8d26\u53f7\u5df2\u5b58\u5728";
        }

        // After removing SMS, a valid invite is mandatory.
        SysDeptRegisterInvite invite = inviteService.validateForRegister(inviteToken, inviteCode);
        if (invite == null)
        {
            return "\u8bf7\u8f93\u5165\u6709\u6548\u7684\u90e8\u95e8\u9080\u8bf7\u7801";
        }
        if (!allowRate("invite:" + invite.getInviteId(), MAX_REG_PER_INVITE_HOUR))
        {
            return "\u8be5\u9080\u8bf7\u7801\u4f7f\u7528\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";
        }

        boolean autoPass = "0".equals(invite.getRegisterMode());

        SysUser user = new SysUser();
        user.setLoginName(loginName);
        user.setUserName(userName);
        user.setPhonenumber(phonenumber);
        user.setPwdUpdateDate(DateUtils.getNowDate());
        user.setSalt("");
        user.setPassword(passwordService.encryptPassword(password));
        user.setStatus("0");
        user.setAllowAdminLogin("0");
        user.setAllowMiniLogin("1");
        user.setDeptId(invite.getDeptId());
        user.setRegisterInviteId(invite.getInviteId());
        user.setRegisterSource(StringUtils.isNotEmpty(inviteToken) ? "qr" : "invite_code");

        if (autoPass)
        {
            user.setAuditStatus("0");
            user.setDispatchable("1");
            user.setAuditBy("system");
            user.setAuditTime(new Date());
            user.setAuditRemark("\u7cfb\u7edf\u81ea\u52a8");
        }
        else
        {
            user.setAuditStatus("1");
            user.setDispatchable("0");
        }

        boolean ok;
        try
        {
            ok = userService.registerUser(user);
        }
        catch (org.springframework.dao.DuplicateKeyException e)
        {
            String em = e.getMessage() == null ? "" : e.getMessage();
            if (em.contains("phonenumber") || em.contains("uk_sys_user_phonenumber"))
            {
                return "\u8be5\u624b\u673a\u53f7\u5df2\u6ce8\u518c";
            }
            if (em.contains("login_name") || em.contains("uk_"))
            {
                return "\u6ce8\u518c\u8d26\u53f7\u5df2\u5b58\u5728";
            }
            return "\u6ce8\u518c\u5931\u8d25,\u8bf7\u8054\u7cfb\u7cfb\u7edf\u7ba1\u7406\u4eba\u5458";
        }
        if (!ok)
        {
            return "\u6ce8\u518c\u5931\u8d25,\u8bf7\u8054\u7cfb\u7cfb\u7edf\u7ba1\u7406\u4eba\u5458";
        }

        SysUser registered = userService.selectUserByLoginName(loginName);
        if (registered == null)
        {
            return "\u6ce8\u518c\u5931\u8d25,\u8bf7\u8054\u7cfb\u7cfb\u7edf\u7ba1\u7406\u4eba\u5458";
        }

        if (autoPass)
        {
            assignMaintenanceMember(registered.getUserId());
        }
        inviteService.markUsed(invite.getInviteId());

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.REGISTER,
                MessageUtils.message("user.register.success")));

        if (result != null)
        {
            result.put("autoPassed", autoPass);
            result.put("auditStatus", user.getAuditStatus());
            result.put("userId", registered.getUserId());
        }
        return "";
    }

    public void assignMaintenanceMember(Long userId)
    {
        SysRole memberRole = roleMapper.checkRoleKeyUnique("maintenance_member");
        if (memberRole != null && memberRole.getRoleId() != null)
        {
            userService.insertUserAuth(userId, new Long[] { memberRole.getRoleId() });
        }
    }

    private String safeClientIp()
    {
        try
        {
            HttpServletRequest request = ServletUtils.getRequest();
            if (request != null)
            {
                return IpUtils.getIpAddr(request);
            }
        }
        catch (Exception ignored)
        {
        }
        return "unknown";
    }

    private boolean allowRate(String key, int maxPerHour)
    {
        long now = System.currentTimeMillis();
        HourCounter counter = rateCounters.computeIfAbsent(key, k -> new HourCounter());
        cleanupRate(now);
        return counter.tryIncrement(now, maxPerHour);
    }

    private void cleanupRate(long now)
    {
        Iterator<Map.Entry<String, HourCounter>> it = rateCounters.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, HourCounter> e = it.next();
            if (e.getValue().expired(now))
            {
                it.remove();
            }
        }
    }

    private static final class HourCounter
    {
        private long windowStart;
        private final AtomicInteger count = new AtomicInteger(0);

        synchronized boolean tryIncrement(long now, int max)
        {
            if (windowStart == 0 || now - windowStart > 3600_000L)
            {
                windowStart = now;
                count.set(0);
            }
            if (count.get() >= max)
            {
                return false;
            }
            count.incrementAndGet();
            return true;
        }

        synchronized boolean expired(long now)
        {
            return windowStart > 0 && now - windowStart > 7200_000L;
        }
    }
}
