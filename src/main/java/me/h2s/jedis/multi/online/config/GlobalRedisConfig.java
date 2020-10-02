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
@ConditionalOnProperty(prefix=GlobalRedisConfig.REDIS_GLOBAL, name = "host")
@EnableConfigurationProperties(GlobalRedisConfig.GlobalRedisProperties.class)
public class GlobalRedisConfig {

    static final String REDIS_GLOBAL = "spring.redis-global";
    Logger log = LoggerFactory.getLogger(GlobalRedisConfig.class);

    @Bean
    public GlobalJedisConnectionConfiguration globalJedisConnectionConfiguration(GlobalRedisProperties globalRedisProperties, ObjectProvider<RedisSentinelConfiguration> redisSentinelConfigurations, ObjectProvider<RedisClusterConfiguration> clusterConfigurationctProvider) {
        log.debug("global-redis-configuration : {}", globalRedisProperties);
        return new GlobalJedisConnectionConfiguration(globalRedisProperties, redisSentinelConfigurations, clusterConfigurationctProvider);
    }

    @Bean
    public GlobalJedisConnectionFactoryWrapper globalRedisConnectionFactory(GlobalJedisConnectionConfiguration globalJedisConnectionConfiguration, ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) throws UnknownHostException {
        JedisConnectionFactory jedisConnectionFactory = globalJedisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
        return  new GlobalJedisConnectionFactoryWrapper(jedisConnectionFactory);
    }

    @Bean
    public GlobalRedisTemplate<Object, Object> globalRedisTemplate(GlobalJedisConnectionFactoryWrapper jedisConnectionFactoryWrapper) throws UnknownHostException {
        GlobalRedisTemplate<Object, Object> template = new GlobalRedisTemplate();
        template.setConnectionFactory(jedisConnectionFactoryWrapper.getJedisConnectionFactory());
        return template;
    }

    @Bean
    public GlobalStringRedisTemplate globalStringRedisTemplate(GlobalJedisConnectionFactoryWrapper jedisConnectionFactoryWrapper) throws UnknownHostException {
        GlobalStringRedisTemplate template = new GlobalStringRedisTemplate(jedisConnectionFactoryWrapper.getJedisConnectionFactory());
        return template;
    }

    @ConfigurationProperties(prefix = REDIS_GLOBAL)
    public static class GlobalRedisProperties extends RedisProperties {
        public GlobalRedisProperties() {
            super();
        }

        @lombok.SneakyThrows
        public String toString() {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this) ;
        }
    }

    public static class GlobalJedisConnectionConfiguration extends JedisConnectionConfiguration {
        public GlobalJedisConnectionConfiguration(GlobalRedisProperties globalRedisProperties, ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration, ObjectProvider<RedisClusterConfiguration> clusterConfiguration) {
            super(globalRedisProperties, sentinelConfiguration, clusterConfiguration);
        }
    }

    public static class GlobalJedisConnectionFactoryWrapper {
        private JedisConnectionFactory jedisConnectionFactory;
        public GlobalJedisConnectionFactoryWrapper(JedisConnectionFactory jedisConnectionFactory) {
            this.jedisConnectionFactory = jedisConnectionFactory;
        }

        public JedisConnectionFactory getJedisConnectionFactory() {
            return jedisConnectionFactory;
        }
    }

    public static class GlobalRedisTemplate<K, V> extends RedisTemplate<K, V> {
        public GlobalRedisTemplate() {
            super();
        }
    }

    public static class GlobalStringRedisTemplate extends StringRedisTemplate {
        public GlobalStringRedisTemplate(RedisConnectionFactory connectionFactory) {
            super(connectionFactory);
        }
    }

}
