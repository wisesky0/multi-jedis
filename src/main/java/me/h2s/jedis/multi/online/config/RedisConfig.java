package me.h2s.jedis.multi.online.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.h2s.jedis.multi.online.config.custom.redis.JedisConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.UnknownHostException;

@Configuration
@ConditionalOnProperty(prefix="spring.redis", name = "host")
public class RedisConfig {
    Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    @ConfigurationProperties("spring.redis")
    RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public JedisConnectionConfiguration jedisConnectionConfiguration(@Qualifier("redisProperties") RedisProperties redisProperties, ObjectProvider<RedisSentinelConfiguration> redisSentinelConfigurations, ObjectProvider<RedisClusterConfiguration> clusterConfigurationctProvider) throws JsonProcessingException {
        log.debug("redis-configuration : {}", getJsonString(redisProperties));
        return new JedisConnectionConfiguration(redisProperties, redisSentinelConfigurations, clusterConfigurationctProvider);
    }

    private String getJsonString(RedisProperties redisProperties) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(redisProperties);
        } catch (Exception e) {
            return redisProperties.toString();
        }
    }

    @Bean
    public JedisConnectionFactory redisConnectionFactory(@Qualifier("jedisConnectionConfiguration") JedisConnectionConfiguration jedisConnectionConfiguration, ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) throws UnknownHostException {
        return jedisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
