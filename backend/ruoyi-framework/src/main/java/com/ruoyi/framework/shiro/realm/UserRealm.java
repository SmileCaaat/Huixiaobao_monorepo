package com.ruoyi.framework.shiro.realm;

import java.util.HashSet;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.user.BlackListException;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.RoleBlockedException;
import com.ruoyi.common.exception.user.UserAuditException;
import com.ruoyi.common.exception.user.UserBlockedException;
import com.ruoyi.common.exception.user.UserDeleteException;
import com.ruoyi.common.exception.user.UserLoginChannelException;
import com.ruoyi.common.exception.user.UserNotExistsException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.exception.user.UserPasswordRetryLimitExceedException;
import com.ruoyi.common.utils.ClientSafeMessage;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.framework.shiro.service.SysLoginService;
import com.ruoyi.framework.shiro.token.WxAuthToken;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysRoleService;

/**
 * 自定义Realm 处理登录 权限
 * 
 * @author ruoyi
 */
public class UserRealm extends AuthorizingRealm
{
    private static final Logger log = LoggerFactory.getLogger(UserRealm.class);

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysUserService userService;

    @Override
    public boolean supports(AuthenticationToken token)
    {
        // 网页密码登录与微信无密码登录分叉；WxAuthToken 不继承 UsernamePasswordToken
        if (token instanceof WxAuthToken)
        {
            return true;
        }
        return token != null && token.getClass() == UsernamePasswordToken.class;
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0)
    {
        SysUser user = ShiroUtils.getSysUser();
        // 角色列表
        Set<String> roles = new HashSet<String>();
        // 功能列表
        Set<String> menus = new HashSet<String>();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 管理员拥有所有权限
        if (user.isAdmin())
        {
            info.addRole("admin");
            info.addStringPermission("*:*:*");
        }
        else
        {
            roles = roleService.selectRoleKeys(user.getUserId());
            menus = menuService.selectPermsByUserId(user.getUserId());
            // 角色加入AuthorizationInfo认证对象
            info.setRoles(roles);
            // 权限加入AuthorizationInfo认证对象
            info.setStringPermissions(menus);
        }
        return info;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
    {
        if (token instanceof WxAuthToken)
        {
            return authenticateWx((WxAuthToken) token);
        }

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        String password = "";
        if (upToken.getPassword() != null)
        {
            password = new String(upToken.getPassword());
        }

        SysUser user = null;
        try
        {
            user = loginService.login(username, password);
        }
        catch (CaptchaException e)
        {
            throw new AuthenticationException(e.getMessage(), e);
        }
        catch (UserNotExistsException e)
        {
            throw new UnknownAccountException(e.getMessage(), e);
        }
        catch (UserPasswordNotMatchException e)
        {
            throw new IncorrectCredentialsException(e.getMessage(), e);
        }
        catch (UserPasswordRetryLimitExceedException e)
        {
            throw new ExcessiveAttemptsException(e.getMessage(), e);
        }
        catch (UserBlockedException | UserDeleteException e)
        {
            throw new LockedAccountException(e.getMessage(), e);
        }
        catch (UserAuditException | UserLoginChannelException e)
        {
            throw new LockedAccountException(e.getMessage(), e);
        }
        catch (RoleBlockedException e)
        {
            throw new LockedAccountException(e.getMessage(), e);
        }
        catch (BlackListException e)
        {
            throw new LockedAccountException(e.getMessage(), e);
        }
        catch (Exception e)
        {
            logLoginSystemError(username, e);
            throw new AuthenticationException(ClientSafeMessage.forLogin(safeMsg(e), e), e);
        }
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, password, getName());
        return info;
    }

    private AuthenticationInfo authenticateWx(WxAuthToken wxToken) throws AuthenticationException
    {
        String loginName = wxToken.getLoginName();
        SysUser user = userService.selectUserByLoginName(loginName);
        if (user == null)
        {
            throw new UnknownAccountException("用户不存在");
        }
        try
        {
            loginService.validateAuditStatus(user, loginName);
            loginService.validateMiniLoginAllowed(user, loginName);
            if (com.ruoyi.common.enums.UserStatus.DISABLE.getCode().equals(user.getStatus()))
            {
                throw new LockedAccountException("账号已停用");
            }
            loginService.setRolePermission(user);
            loginService.recordLoginInfo(user.getUserId());
        }
        catch (AuthenticationException e)
        {
            throw e;
        }
        catch (UserAuditException | UserLoginChannelException | UserBlockedException e)
        {
            throw new LockedAccountException(e.getMessage(), e);
        }
        catch (Exception e)
        {
            logLoginSystemError(loginName, e);
            throw new AuthenticationException(ClientSafeMessage.forLogin(safeMsg(e), e), e);
        }
        return new SimpleAuthenticationInfo(user, "", getName());
    }

    private static String safeMsg(Throwable e)
    {
        try
        {
            return e == null ? null : e.getMessage();
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    /**
     * Log full cause chain for login system failures. Never log passwords or captcha.
     */
    private void logLoginSystemError(String username, Throwable e)
    {
        StringBuilder chain = new StringBuilder();
        Throwable t = e;
        Throwable root = e;
        int depth = 0;
        while (t != null && depth < 20)
        {
            if (depth > 0)
            {
                chain.append(" <- ");
            }
            chain.append(t.getClass().getName());
            String m = safeMsg(t);
            if (m != null && m.length() > 0 && m.length() <= 240)
            {
                chain.append(": ").append(m.replaceAll("[\\r\\n]+", " "));
            }
            root = t;
            t = t.getCause();
            depth++;
        }
        StackTraceElement[] stack = root.getStackTrace();
        String location = (stack != null && stack.length > 0)
                ? stack[0].getClassName() + "." + stack[0].getMethodName()
                    + "(" + stack[0].getFileName() + ":" + stack[0].getLineNumber() + ")"
                : "unknown";
        log.error("LOGIN_SYSTEM_ERROR user={} type={} rootType={} location={} causeChain={}",
                username,
                e.getClass().getName(),
                root.getClass().getName(),
                location,
                chain.toString(),
                e);
    }

    /**
     * 清理指定用户授权信息缓存
     */
    public void clearCachedAuthorizationInfo(Object principal)
    {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        this.clearCachedAuthorizationInfo(principals);
    }

    /**
     * 清理所有用户授权信息缓存
     */
    public void clearAllCachedAuthorizationInfo()
    {
        Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
        if (cache != null)
        {
            for (Object key : cache.keys())
            {
                cache.remove(key);
            }
        }
    }
}
