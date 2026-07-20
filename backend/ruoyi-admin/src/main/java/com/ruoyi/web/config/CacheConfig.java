package com.ruoyi.web.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

/**
 * 缓存配置
 * 
 * @author ruoyi
 */
@Configuration
public class CacheConfig extends CachingConfigurerSupport
{
    /**
     * 配置缓存管理器
     * 使用简单的内存缓存
     */
    @Bean
    @Override
    public CacheManager cacheManager()
    {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        // 配置缓存区域
        cacheManager.setCaches(Arrays.asList(
            // 维保模板缓存（永久缓存，模板数据不会变化）
            new ConcurrentMapCache("maintenanceTemplates"),
            // 其他缓存可以在这里添加
            new ConcurrentMapCache("systemTypes")
        ));
        
        return cacheManager;
    }
}
