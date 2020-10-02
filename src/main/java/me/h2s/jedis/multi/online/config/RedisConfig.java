package me.h2s.jedis.multi.online.config;

import me.h2s.jedis.multi.online.config.custom.redis.JedisConnectionConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
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
@EnableAutoConfiguration(exclude = RedisAutoConfiguration.class)
public class RedisConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.redis-local")
    public RedisProperties redisLocalProperties() {
        return new RedisProperties();
    }

    @Bean
    public JedisConnectionConfiguration jedisLocalConnectionConfiguration(@Qualifier("redisLocalProperties") RedisProperties redisLocalProperties, ObjectProvider<RedisSentinelConfiguration> redisSentinelConfigurations, ObjectProvider<RedisClusterConfiguration> clusterConfigurationctProvider) {
        return new JedisConnectionConfiguration(redisLocalProperties, redisSentinelConfigurations, clusterConfigurationctProvider);
    }

    @Bean
    public JedisConnectionFactory redisLocalConnectionFactory(@Qualifier("jedisLocalConnectionConfiguration") JedisConnectionConfiguration jedisLocalConnectionConfiguration, ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) throws UnknownHostException {
        return jedisLocalConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }

    @Bean
    public RedisTemplate<Object, Object> redisLocalTemplate(@Qualifier("redisLocalConnectionFactory") RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisLocalTemplate(@Qualifier("redisLocalConnectionFactory") RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.redis")
    public RedisProperties redisGlobalProperties() {
        return new RedisProperties();
    }

    @Bean
    public JedisConnectionConfiguration jedisGlobalConnectionConfiguration(@Qualifier("redisGlobalProperties") RedisProperties redisGlobalProperties, ObjectProvider<RedisSentinelConfiguration> redisSentinelConfigurations, ObjectProvider<RedisClusterConfiguration> clusterConfigurationctProvider) {
        return new JedisConnectionConfiguration(redisGlobalProperties, redisSentinelConfigurations, clusterConfigurationctProvider);
    }
    @Bean (name = {"redisConnectionFactory", "redisGlobalConnectionFactory"})
    public JedisConnectionFactory redisGlobalConnectionFactory(@Qualifier("jedisGlobalConnectionConfiguration") JedisConnectionConfiguration jedisLocalConnectionConfiguration, ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) throws UnknownHostException {
        return jedisLocalConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }

    @Bean(name = {"redisTemplate", "redisGlobalTemplate"})
    public RedisTemplate<Object, Object> redisGlobalTemplate(@Qualifier("redisGlobalConnectionFactory") RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean(name ={"stringRedisTemplate","stringRedisGlobalTemplate"})
    public StringRedisTemplate stringRedisGlobalTemplate(@Qualifier("redisGlobalConnectionFactory") RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
