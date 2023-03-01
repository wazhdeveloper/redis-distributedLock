package com.redis.lock.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author wazh
 * @description
 * @since 2023-03-01-19:21
 */
@RestController
@Slf4j
public class RedLockController {

    public static final String CACHE_KEY_REDLOCK = "WAZH_REDLOCK";
    @Autowired
    RedissonClient redissonClient1;
    @Autowired
    RedissonClient redissonClient2;
    @Autowired
    RedissonClient redissonClient3;

    @GetMapping("/multiLock")
    public String multiLock() {
        String taskThreadID = Thread.currentThread().getId()+"";
        //得到三个redis客户端接入口
        RLock lock1 = redissonClient1.getLock(CACHE_KEY_REDLOCK);
        RLock lock2 = redissonClient2.getLock(CACHE_KEY_REDLOCK);
        RLock lock3 = redissonClient3.getLock(CACHE_KEY_REDLOCK);
        RedissonMultiLock redissonMultiLock = new RedissonMultiLock(lock1, lock2, lock3);
        redissonMultiLock.lock();
        try
        {
            log.info("come in biz multilock:{}",taskThreadID);
            try { TimeUnit.SECONDS.sleep(30); } catch (InterruptedException e) { e.printStackTrace(); }
            log.info("task is over multilock:{}",taskThreadID);
        }catch (Exception e){
            e.printStackTrace();
            log.error("multilock exception:{}",e.getCause()+"\t"+e.getMessage());
        }finally {
            redissonMultiLock.unlock();
            log.info("释放分布式锁成功key:{}",CACHE_KEY_REDLOCK);
        }
        return "multilock task is over: "+taskThreadID;
    }

}
