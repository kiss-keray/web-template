package com.keray.common.config;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * redis配置
 *
 * @author keray
 */
@Configuration
@ConditionalOnProperty("spring.redis.host")
@ConfigurationProperties("spring.redis")
public class RedisConfig {

    @Resource(name = "redisObjectMapper")
    @Lazy
    private ObjectMapper om;

    @Setter
    @Getter
    private Integer apiDb = 1;

    @Setter
    @Getter
    private Integer cacheDb = 2;
    @Setter
    @Getter
    private Integer redissonDb = 4;

    @Setter
    @Getter
    private Integer importantDb = 5;


    private final Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer(Object.class);

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

        return container;
    }


    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    Receiver receiver() {
        return new Receiver(new CountDownLatch(1));
    }

    @Bean
    @Primary
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return redisTemplateFactory(redisConnectionFactory);
    }

    @Bean(name = "apiRedisRedisTemplate")
    public RedisTemplate apiRedisRedisTemplate(RedisProperties redisProperties) {
        return redisTemplateFactory(redisProperties, apiDb);
    }

    @Bean(name = "cacheRedisRedisTemplate")
    public RedisTemplate cacheRedisRedisTemplate(RedisProperties redisProperties) {
        return redisTemplateFactory(redisProperties, cacheDb);
    }


    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/11/28 5:32 PM</h3>
     * 重要的Redis数据  不允许清除
     * </p>
     *
     * @param redisProperties
     * @return <p> {@link RedisTemplate} </p>
     * @throws
     */
    @Bean(name = "persistenceRedisTemplate")
    public RedisTemplate persistenceRedisTemplate(RedisProperties redisProperties) {
        return redisTemplateFactory(redisProperties, importantDb);
    }

    // redisson
    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress(StrUtil.format("redis://{}:{}", redisProperties.getHost(), redisProperties.getPort()))
                .setPassword(redisProperties.getPassword())
                .setDatabase(redissonDb)
                .setTimeout(30_000);
        config.setTransportMode(TransportMode.NIO);
        config.setCodec(new JsonJacksonCodec(om));
        return Redisson.create(config);
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPassword(redisProperties.getPassword());
        config.setPort(redisProperties.getPort());
        config.setDatabase(redisProperties.getDatabase());
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(true);
        return factory;
    }

    @Bean
    public RedissonConnectionFactory redissonConnectionFactory(@Qualifier("redissonClient") RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }

    public RedisTemplate redisTemplateFactory(RedisProperties redisProperties, Integer database) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPassword(redisProperties.getPassword());
        config.setPort(redisProperties.getPort());
        //默认0号库，现在这里是1号库
        config.setDatabase(database);
        //手动创建工厂，这样做的目的就是划分库
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(true);
        factory.afterPropertiesSet();
        return redisTemplateFactory(factory);
    }


    public RedisTemplate redisTemplateFactory(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(factory);
        jacksonSeial.setObjectMapper(om);
        // 值采用json序列化
        template.setValueSerializer(jacksonSeial);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        // 设置hash key 和value序列化模式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSeial);
        template.afterPropertiesSet();
        return template;
    }


    public class Receiver {


        private CountDownLatch latch;

        public Receiver(CountDownLatch latch) {
            this.latch = latch;
        }

        public void receiveMessage(String message) {
            latch.countDown();
        }
    }


}
