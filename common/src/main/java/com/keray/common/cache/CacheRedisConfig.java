package com.keray.common.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * @author by keray
 * date:2020/4/18 5:01 下午
 */
@Configuration
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
@Import(CacheProperties.class)
public class CacheRedisConfig {

    @Bean
    @Primary
    RedisCacheManager cacheManager(CacheProperties cacheProperties, @Qualifier("cacheRedisRedisTemplate") RedisTemplate cacheRedisRedisTemplate) {
        Resource resource = cacheProperties.getEhcache().getConfig();
        net.sf.ehcache.config.Configuration conf = null;
        if (resource != null) {
            conf = EhCacheManagerUtils.parseConfiguration(resource);
        }
        RedisCacheWriter redisCacheWriter = new ERedisCacheWriter(cacheRedisRedisTemplate.getConnectionFactory(), conf == null ? null : conf.getCacheConfigurations());
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(cacheRedisRedisTemplate.getValueSerializer()));
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }
}
