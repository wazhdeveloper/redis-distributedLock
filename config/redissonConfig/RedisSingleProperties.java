package com.redis.lock.config.redissonConfig;

import lombok.Data;

/**
 * @author wazh
 * @description 保存redlock中使用的多个redis服务器地址
 * @since 2023-03-01-18:56
 */
@Data
public class RedisSingleProperties {

    private String address1;
    private String address2;
    private String address3;

}
