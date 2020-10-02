package me.h2s.jedis.multi.online.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.h2s.jedis.multi.online.config.custom.redis.JedisConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@ConditionalOnProperty(prefix=LocalRedisConfig.REDIS_LOCAL, name = "host")
@EnableConfigurationProperties(LocalRedisConfig.LocalRedisProperties.class)
public class LocalRedisConfig {

    static final String REDIS_LOCAL = "spring.redis-local";

    Logger log = LoggerFactory.getLogger(LocalRedisConfig.class);

    @Bean
    public LocalJedisConnectionConfiguration localJedisConnectionConfiguration(LocalRedisProperties localRedisProperties, ObjectProvider<RedisSentinelConfiguration> redisSentinelConfigurations, ObjectProvider<RedisClusterConfiguration> clusterConfigurationctProvider) {
        log.debug("local-redis-configuration : {}", localRedisProperties);
        return new LocalJedisConnectionConfiguration(localRedisProperties, redisSentinelConfigurations, clusterConfigurationctProvider);
    }

    @Bean
    public LocalJedisConnectionFactoryWrapper localRedisConnectionFactory(LocalJedisConnectionConfiguration localJedisConnectionConfiguration, ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) throws UnknownHostException {
        JedisConnectionFactory jedisConnectionFactory = localJedisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
        return new LocalJedisConnectionFactoryWrapper(jedisConnectionFactory);
    }

    @Bean
    public LocalRedisTemplate<Object, Object> localRedisTemplate(LocalJedisConnectionFactoryWrapper jedisConnectionFactoryWrapper) throws UnknownHostException {
        LocalRedisTemplate<Object, Object> template = new LocalRedisTemplate();
        template.setConnectionFactory(jedisConnectionFactoryWrapper.getJedisConnectionFactory());
        return template;
    }

    @Bean
    public LocalStringRedisTemplate localStringRedisTemplate(LocalJedisConnectionFactoryWrapper jedisConnectionFactoryWrapper) throws UnknownHostException {
        LocalStringRedisTemplate template = new LocalStringRedisTemplate(jedisConnectionFactoryWrapper.getJedisConnectionFactory());
        return template;
    }

    @ConfigurationProperties(prefix = REDIS_LOCAL)
    public static class LocalRedisProperties extends RedisProperties {
        public LocalRedisProperties() {
            super();
        }

        @lombok.SneakyThrows
        public String toString() {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this) ;
        }
    }

    public static class LocalJedisConnectionConfiguration extends JedisConnectionConfiguration {
        public LocalJedisConnectionConfiguration(LocalRedisProperties localRedisProperties, ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration, ObjectProvider<RedisClusterConfiguration> clusterConfiguration) {
            super(localRedisProperties, sentinelConfiguration, clusterConfiguration);
        }
    }

    public static class LocalJedisConnectionFactoryWrapper {
        private JedisConnectionFactory jedisConnectionFactory;

        public LocalJedisConnectionFactoryWrapper(JedisConnectionFactory jedisConnectionFactory) {
            this.jedisConnectionFactory = jedisConnectionFactory;
        }

        public JedisConnectionFactory getJedisConnectionFactory() {
            return jedisConnectionFactory;
        }
    }

    public static class LocalRedisTemplate<K, V> extends RedisTemplate<K, V> {
        public LocalRedisTemplate() {
            super();
        }
    }

    public static class LocalStringRedisTemplate extends StringRedisTemplate {
        public LocalStringRedisTemplate(RedisConnectionFactory connectionFactory) {
            super(connectionFactory);
        }
    }
}
