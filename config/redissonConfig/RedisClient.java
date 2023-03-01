package com.redis.lock.config.redissonConfig;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author wazh
 * @description : redisson 配置类，创建redisson客户端类到bean容器中
 * @since 2023-03-01-18:55
 */
@EnableConfigurationProperties(RedisProperties.class) //检测@ConfigurationProperties下的类，并将该类加到容器中，并使其能发挥作用
@Data
public class RedisClient {
    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedissonClient redissonClient1() {
        Config config = new Config();
        String address1 = redisProperties.getSingle().getAddress1();
        String node = address1.startsWith("redis://") ? address1 : "redis://" + address1;
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(node)
                .setTimeout(redisProperties.getPool().getConnTimeout())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle())
                .setConnectionPoolSize(redisProperties.getPool().getSize());
        if (redisProperties.getPassword() != null) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient2() {
        Config config = new Config();
        String address1 = redisProperties.getSingle().getAddress2();
        String node = address1.startsWith("redis://") ? address1 : "redis://" + address1;
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(node)
                .setTimeout(redisProperties.getPool().getConnTimeout())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle())
                .setConnectionPoolSize(redisProperties.getPool().getSize());
        if (redisProperties.getPassword() != null) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient3() {
        Config config = new Config();
        String address1 = redisProperties.getSingle().getAddress3();
        String node = address1.startsWith("redis://") ? address1 : "redis://" + address1;
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(node)
                .setTimeout(redisProperties.getPool().getConnTimeout())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle())
                .setConnectionPoolSize(redisProperties.getPool().getSize());
        if (redisProperties.getPassword() != null) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }
}
