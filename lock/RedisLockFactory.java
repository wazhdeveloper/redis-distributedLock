package com.redis.lock.lock;

import cn.hutool.core.util.IdUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;

/**
 * @author wazh
 * @description 分布式锁工厂
 * @since 2023-02-28-19:02
 */
@Component
public class RedisLockFactory {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static String uuid;

    public RedisLockFactory() {
        uuid = IdUtil.simpleUUID();
    }

    public Lock getRedisLock(String name) {
        if (name.equalsIgnoreCase("REDIS")) {
            String lockName = "redisLock";
            return new RedisDistributedLock(stringRedisTemplate, lockName, uuid);
        } else if (name.equalsIgnoreCase("ZOOKEPPER")) {
            //TODO
        } else if (name.equalsIgnoreCase("Mysql")) {
            //TODO
        }
        return null;
    }
}
