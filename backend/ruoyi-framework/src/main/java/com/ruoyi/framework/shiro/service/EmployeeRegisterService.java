package com.ruoyi.framework.shiro.service;

import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.domain.SysDeptRegisterInvite;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISmsCodeService;
import com.ruoyi.system.service.ISysDeptRegisterInviteService;
import com.ruoyi.system.service.ISysUserService;

/**
 * Employee self-register (invite token/code + SMS). Creates SysUser only.
 */
@Component
public class EmployeeRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private ISmsCodeService smsCodeService;

    @Autowired
    private ISysDeptRegisterInviteService inviteService;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * @return empty string on success; otherwise error message
     */
    @Transactional(rollbackFor = Exception.class)
    public String register(SysUser form, String inviteToken, String inviteCode, String smsCode, Map<String, Object> result)
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
        if (StringUtils.isEmpty(smsCode))
        {
            return "\u8bf7\u8f93\u5165\u77ed\u4fe1\u9a8c\u8bc1\u7801";
        }
        if (!smsCodeService.verifyAndConsume(phonenumber, smsCode))
        {
            return "\u77ed\u4fe1\u9a8c\u8bc1\u7801\u9519\u8bef\u6216\u5df2\u8fc7\u671f";
        }

        // Ignore client-side org ids; invite token resolution is authoritative.
        form.setDeptId(null);

        SysUser phoneExists = userService.selectUserByPhoneNumber(phonenumber);
        if (phoneExists != null)
        {
            return "\u8be5\u624b\u673a\u53f7\u5df2\u6ce8\u518c";
        }

        String loginName = StringUtils.isNotEmpty(form.getLoginName()) ? form.getLoginName() : phonenumber;
        form.setLoginName(loginName);
        if (!userService.checkLoginNameUnique(form))
        {
            return "\u6ce8\u518c\u8d26\u53f7\u5df2\u5b58\u5728";
        }

        SysDeptRegisterInvite invite = inviteService.validateForRegister(inviteToken, inviteCode);
        boolean autoPass = invite != null && "0".equals(invite.getRegisterMode());

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
        if (invite != null)
        {
            user.setDeptId(invite.getDeptId());
            user.setRegisterInviteId(invite.getInviteId());
            user.setRegisterSource(StringUtils.isNotEmpty(inviteToken) ? "qr" : "invite_code");
        }
        else
        {
            user.setRegisterSource("direct");
        }

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

        boolean ok = userService.registerUser(user);
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
        if (invite != null)
        {
            inviteService.markUsed(invite.getInviteId());
        }

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
}
