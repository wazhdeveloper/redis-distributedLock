package com.redis.lock.lock;

import cn.hutool.core.util.IdUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author wazh
 * @description: 利用lua脚本，实现自研分布式锁【可重入性】
 * @since 2023-02-28-16:01
 */
public class RedisDistributedLock implements Lock {
    private StringRedisTemplate stringRedisTemplate;
    private String lockName;
    private String uuidValue;
    private Long expireTime;

    public RedisDistributedLock(StringRedisTemplate stringRedisTemplate, String lockName, String uuid) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockName = lockName;
        this.uuidValue = uuid + " : " + Thread.currentThread().getId();
        this.expireTime = 30L;
    }

    @Override
    public void lock() {
        boolean b = tryLock();
        if (b) System.out.println("分布式锁: " + lockName + " 添加成功!");
    }

    @Override
    public boolean tryLock() {
        try {
            return tryLock(-1L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (time == -1L) {
            String luaScript = "" +
                    "if redis.call('exists',KEYS[1]) == 0 or redis.call('hexists',keys[1],ARGV[1]) == 1 then" +
                    "    redis.call('hincrby',KEYS[1],ARGV[1],1)" +
                    "    redis.call('expire',KEYS[1],ARGV[2])" +
                    "    return 1" +
                    "else " +
                    "    return 0" +
                    "end";
            while (Boolean.FALSE.equals(stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Boolean.class), Collections.singletonList(lockName), uuidValue, String.valueOf(expireTime)))) {
                try {
                    TimeUnit.MILLISECONDS.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            renewExpire();  //自动续期
            return true;
        }
        return false;
    }

    @Override
    public void unlock() {
        String luaScript = "if redis.call('hexists',KEYS[1],ARGV[1]) == 0 then" +
                "    return nil" +
                "elseif redis.call('hincrby',KEYS[1],ARGV[1],-1) == 0 then" +
                "    return redis.call('del',KEYS[1])" +
                "else" +
                "    return 0" +
                "end";
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Collections.singletonList(lockName), uuidValue);
        if (execute == null) {
            System.out.println(lockName + "doesn't exists!");
        }
    }

    @ApiOperation("自动延期")
    private void renewExpire() {
        String script =
                "if redis.call('HEXISTS',KEYS[1],ARGV[1]) == 1 then     " +
                        "return redis.call('expire',KEYS[1],ARGV[2]) " +
                        "else     " +
                        "return 0 " +
                        "end";

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuidValue, String.valueOf(expireTime))) {
                    renewExpire();
                }
            }
        }, (this.expireTime * 1000) / 3); //指代10秒自动续期一次
    }


    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }
}
