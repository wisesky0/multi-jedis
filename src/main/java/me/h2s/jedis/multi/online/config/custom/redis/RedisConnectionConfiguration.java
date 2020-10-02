package me.h2s.jedis.multi.online.config.custom.redis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

abstract class RedisConnectionConfiguration {
    private final RedisProperties properties;
    private final RedisSentinelConfiguration sentinelConfiguration;
    private final RedisClusterConfiguration clusterConfiguration;

    protected RedisConnectionConfiguration(RedisProperties properties, ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider, ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        this.properties = properties;
        this.sentinelConfiguration = (RedisSentinelConfiguration)sentinelConfigurationProvider.getIfAvailable();
        this.clusterConfiguration = (RedisClusterConfiguration)clusterConfigurationProvider.getIfAvailable();
    }

    protected final RedisStandaloneConfiguration getStandaloneConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.hasText(this.properties.getUrl())) {
            RedisConnectionConfiguration.ConnectionInfo connectionInfo = this.parseUrl(this.properties.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(this.properties.getHost());
            config.setPort(this.properties.getPort());
            config.setPassword(RedisPassword.of(this.properties.getPassword()));
        }

        config.setDatabase(this.properties.getDatabase());
        return config;
    }

    protected final RedisSentinelConfiguration getSentinelConfig() {
        if (this.sentinelConfiguration != null) {
            return this.sentinelConfiguration;
        } else {
            Sentinel sentinelProperties = this.properties.getSentinel();
            if (sentinelProperties != null) {
                RedisSentinelConfiguration config = new RedisSentinelConfiguration();
                config.master(sentinelProperties.getMaster());
                config.setSentinels(this.createSentinels(sentinelProperties));
                if (this.properties.getPassword() != null) {
                    config.setPassword(RedisPassword.of(this.properties.getPassword()));
                }

                if (sentinelProperties.getPassword() != null) {
                    config.setSentinelPassword(RedisPassword.of(sentinelProperties.getPassword()));
                }

                config.setDatabase(this.properties.getDatabase());
                return config;
            } else {
                return null;
            }
        }
    }

    protected final RedisClusterConfiguration getClusterConfiguration() {
        if (this.clusterConfiguration != null) {
            return this.clusterConfiguration;
        } else if (this.properties.getCluster() == null) {
            return null;
        } else {
            Cluster clusterProperties = this.properties.getCluster();
            RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
            if (clusterProperties.getMaxRedirects() != null) {
                config.setMaxRedirects(clusterProperties.getMaxRedirects());
            }

            if (this.properties.getPassword() != null) {
                config.setPassword(RedisPassword.of(this.properties.getPassword()));
            }

            return config;
        }
    }

    protected final RedisProperties getProperties() {
        return this.properties;
    }

    private List<RedisNode> createSentinels(Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList();
        Iterator var3 = sentinel.getNodes().iterator();

        while(var3.hasNext()) {
            String node = (String)var3.next();

            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
            } catch (RuntimeException var6) {
                throw new IllegalStateException("Invalid redis sentinel property '" + node + "'", var6);
            }
        }

        return nodes;
    }

    protected RedisConnectionConfiguration.ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                throw new RedisUrlSyntaxException(url);
            } else {
                boolean useSsl = "rediss".equals(scheme);
                String password = null;
                if (uri.getUserInfo() != null) {
                    password = uri.getUserInfo();
                    int index = password.indexOf(58);
                    if (index >= 0) {
                        password = password.substring(index + 1);
                    }
                }

                return new RedisConnectionConfiguration.ConnectionInfo(uri, useSsl, password);
            }
        } catch (URISyntaxException var7) {
            throw new RedisUrlSyntaxException(url, var7);
        }
    }

    static class ConnectionInfo {
        private final URI uri;
        private final boolean useSsl;
        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.password = password;
        }

        boolean isUseSsl() {
            return this.useSsl;
        }

        String getHostName() {
            return this.uri.getHost();
        }

        int getPort() {
            return this.uri.getPort();
        }

        String getPassword() {
            return this.password;
        }
    }
}
