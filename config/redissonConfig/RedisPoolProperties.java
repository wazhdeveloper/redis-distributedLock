package com.redis.lock.config.redissonConfig;

import lombok.Data;

/**
 * @author wazh
 * @description redis池化，需要使用的redis配置
 * @since 2023-03-01-18:56
 */
@Data
public class RedisPoolProperties {

    private int maxIdle;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int connTimeout = 10000;

    private int soTimeout;

    /**
     * 池大小
     */
    private  int size;
}
