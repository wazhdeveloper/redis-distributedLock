package com.redis.lock.service;

import cn.hutool.core.util.IdUtil;
import com.redis.lock.lock.RedisDistributedLock;
import com.redis.lock.lock.RedisLockFactory;
import io.swagger.annotations.ApiOperation;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
public class DistributedService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String port;
    @Autowired
    private RedisLockFactory redisLockFactory;
    @Autowired
    private Redisson redisson;

    @ApiOperation("v9.0 利用redisson实现单机分布式锁")
    public String saleWithRedisson() {
        String resMessage = "";
        RLock lock = redisson.getLock("wazhRedisLock");
        lock.lock();
        try {
            //TODO 业务逻辑:取货操作，取一下，redis减一
            String s = stringRedisTemplate.opsForValue().get("inventory001");
            int value = s == null ? 0 : Integer.parseInt(s);
            if (value > 0) {
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(value - 1));
                resMessage = "取货成功，库存剩余：" + (value - 1);
            } else {
                resMessage = "取货失败，库存已无货，提醒商家重新上货";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 防止误删 ？ 直接写在unlock()里不香吗？
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return resMessage;
    }

    @ApiOperation("v7.0 具有重入性的分布式锁")
    public String saleWithReentrancy() {
        String resMessage = "";
//        String lockName = "wazhDistributedLock";
        Lock lock = redisLockFactory.getRedisLock("redis");
        lock.lock();
        try {
            //TODO 业务逻辑:取货操作，取一下，redis减一
            String s = stringRedisTemplate.opsForValue().get("inventory001");
            int value = s == null ? 0 : Integer.parseInt(s);
            if (value > 0) {
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(value - 1));
                resMessage = "取货成功，库存剩余：" + (value - 1);
            } else {
                resMessage = "取货失败，库存已无货，提醒商家重新上货";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return resMessage;
    }

    @ApiOperation("v6.0 未实现可重入性")
    public String saleWithoutReentrancy() {
        String resMessage = "";
        String key = "wazhDistributedLock";
        String uuidValue = IdUtil.simpleUUID().toLowerCase() + "       :" + Thread.currentThread().getName();
        while (Boolean.FALSE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue, 30L, TimeUnit.SECONDS))) { //自旋重试，直到拿到锁
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            //TODO 业务逻辑:取货操作，取一下，redis减一
            String s = stringRedisTemplate.opsForValue().get("inventory001");
            int value = s == null ? 0 : Integer.parseInt(s);
            if (value > 0) {
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(value - 1));
                resMessage = "取货成功，库存剩余：" + (value - 1);
            } else {
                resMessage = "取货失败，库存已无货，请通知商家上货";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //lua脚本，实现原子操作
            String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then redis.call('del',KEYS[1]) else return 0 end";
            stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Collections.singletonList(key), uuidValue);
        }
        return resMessage + "\t" + "服务端口号：" + port;
    }
}
