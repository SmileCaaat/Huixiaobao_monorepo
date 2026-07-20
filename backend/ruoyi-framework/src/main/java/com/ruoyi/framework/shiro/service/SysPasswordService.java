package com.ruoyi.framework.shiro.service;

import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.ShiroConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.exception.user.UserPasswordRetryLimitExceedException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysUserService;

/**
 * 登录密码方法
 *
 * <p>
 * 密码存储策略：BCrypt（加盐由BCrypt自动处理）。
 * </p>
 * <p>
 * 渐进式迁移：老用户第一次登录时用旧MD5验证，成功后自动升级为BCrypt，无需重置密码。
 * </p>
 * 
 * @author ruoyi
 */
@Component
public class SysPasswordService {
    @Autowired
    private CacheManager cacheManager;

    private Cache<String, AtomicInteger> loginRecordCache;

    @Value(value = "${user.password.maxRetryCount}")
    private String maxRetryCount;

    /** BCrypt 加密器 */
    private final BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder();

    /**
     * 用户服务（仅用于登录成功后自动升级MD5密码为BCrypt，使用 @Lazy 避免循环依赖）
     */
    @Lazy
    @Autowired
    private ISysUserService userService;

    @PostConstruct
    public void init() {
        loginRecordCache = cacheManager.getCache(ShiroConstants.LOGIN_RECORD_CACHE);
    }

    public void validate(SysUser user, String password) {
        String loginName = user.getLoginName();

        AtomicInteger retryCount = loginRecordCache.get(loginName);

        if (retryCount == null) {
            retryCount = new AtomicInteger(0);
            loginRecordCache.put(loginName, retryCount);
        }
        if (retryCount.incrementAndGet() > Integer.valueOf(maxRetryCount).intValue()) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount)));
            throw new UserPasswordRetryLimitExceedException(Integer.valueOf(maxRetryCount).intValue());
        }

        if (!matches(user, password)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.count", retryCount)));
            loginRecordCache.put(loginName, retryCount);
            throw new UserPasswordNotMatchException();
        } else {
            clearLoginRecordCache(loginName);
            // 如果是旧MD5格式，登录成功后自动升级为BCrypt
            if (!isBCryptHash(user.getPassword())) {
                upgradeToBCrypt(user, password);
            }
        }
    }

    /**
     * 判断密码是否匹配（支持BCrypt和旧MD5两种格式）
     */
    public boolean matches(SysUser user, String rawPassword) {
        String stored = user.getPassword();
        if (stored == null) {
            return false;
        }
        if (isBCryptHash(stored)) {
            // 新式 BCrypt 匹配
            return bCryptEncoder.matches(rawPassword, stored);
        }
        // 旧式 MD5 匹配（loginName + password + salt）
        String legacyHash = new Md5Hash(user.getLoginName() + rawPassword + user.getSalt()).toHex();
        return stored.equals(legacyHash);
    }

    /**
     * 加密密码（BCrypt）
     */
    public String encryptPassword(String password) {
        return bCryptEncoder.encode(password);
    }

    /**
     * 已废弃：旧山字签名，为兼容改动前的调用方。
     * 现在直接使用 BCrypt 加密，lineName 和 salt 参数不再使用。
     *
     * @deprecated 请改用 {@link #encryptPassword(String)}
     */
    @Deprecated
    public String encryptPassword(String loginName, String password, String salt) {
        return encryptPassword(password);
    }

    public void clearLoginRecordCache(String loginName) {
        loginRecordCache.remove(loginName);
    }

    // -----------------------------------------------------------------------
    // 私有辅助方法
    // -----------------------------------------------------------------------

    /** 判断存储的哈希是否为BCrypt格式 */
    private boolean isBCryptHash(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    /** MD5登录成功后，将该账号密码升级为BCrypt */
    private void upgradeToBCrypt(SysUser user, String rawPassword) {
        try {
            SysUser upgrade = new SysUser();
            upgrade.setUserId(user.getUserId());
            upgrade.setPassword(bCryptEncoder.encode(rawPassword));
            upgrade.setSalt("");
            userService.resetUserPwd(upgrade);
        } catch (Exception e) {
            // 升级失败不影响登录，下次登录时会再次尝试
        }
    }
}
