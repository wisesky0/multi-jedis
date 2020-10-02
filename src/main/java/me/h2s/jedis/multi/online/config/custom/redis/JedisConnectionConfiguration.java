package me.h2s.jedis.multi.online.config.custom.redis;


import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.net.UnknownHostException;
import java.time.Duration;

public class JedisConnectionConfiguration extends RedisConnectionConfiguration {

    public JedisConnectionConfiguration(RedisProperties properties, ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration, ObjectProvider<RedisClusterConfiguration> clusterConfiguration) {
        super(properties, sentinelConfiguration, clusterConfiguration);
    }

    public JedisConnectionFactory redisConnectionFactory(ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) throws UnknownHostException {
        return this.createJedisConnectionFactory(builderCustomizers);
    }

    private JedisConnectionFactory createJedisConnectionFactory(ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
        JedisClientConfiguration clientConfiguration = this.getJedisClientConfiguration(builderCustomizers);
        if (this.getSentinelConfig() != null) {
            return new JedisConnectionFactory(this.getSentinelConfig(), clientConfiguration);
        } else {
            return this.getClusterConfiguration() != null ? new JedisConnectionFactory(this.getClusterConfiguration(), clientConfiguration) : new JedisConnectionFactory(this.getStandaloneConfig(), clientConfiguration);
        }
    }

    private JedisClientConfiguration getJedisClientConfiguration(ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
        JedisClientConfigurationBuilder builder = this.applyProperties(JedisClientConfiguration.builder());
        Pool pool = this.getProperties().getJedis().getPool();
        if (pool != null) {
            this.applyPooling(pool, builder);
        }

        if (StringUtils.hasText(this.getProperties().getUrl())) {
            this.customizeConfigurationFromUrl(builder);
        }

        builderCustomizers.orderedStream().forEach((customizer) -> {
            customizer.customize(builder);
        });
        return builder.build();
    }

    private JedisClientConfigurationBuilder applyProperties(JedisClientConfigurationBuilder builder) {
        if (this.getProperties().isSsl()) {
            builder.useSsl();
        }

        if (this.getProperties().getTimeout() != null) {
            Duration timeout = this.getProperties().getTimeout();
            builder.readTimeout(timeout).connectTimeout(timeout);
        }

        if (StringUtils.hasText(this.getProperties().getClientName())) {
            builder.clientName(this.getProperties().getClientName());
        }

        return builder;
    }

    private void applyPooling(Pool pool, JedisClientConfigurationBuilder builder) {
        builder.usePooling().poolConfig(this.jedisPoolConfig(pool));
    }

    private JedisPoolConfig jedisPoolConfig(Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if (pool.getTimeBetweenEvictionRuns() != null) {
            config.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns().toMillis());
        }

        if (pool.getMaxWait() != null) {
            config.setMaxWaitMillis(pool.getMaxWait().toMillis());
        }

        return config;
    }

    private void customizeConfigurationFromUrl(JedisClientConfigurationBuilder builder) {
        ConnectionInfo connectionInfo = this.parseUrl(this.getProperties().getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }

    }
}
