package com.redis.lock.controller;

import com.redis.lock.service.DistributedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DistributedController {
    @Autowired
    private DistributedService distributedService;

    @GetMapping("/distributeLock/saleWithoutReentrancy")
    public String saleWithoutReentrancy() {
        return distributedService.saleWithoutReentrancy();
    }

    @GetMapping("/distributeLock/saleWithReentrancy")
    public String saleWithReentrancy() {
        return distributedService.saleWithReentrancy();
    }

    @GetMapping("/distributeLock/saleWithRedisson")
    public String saleWithRedisson() {
        return distributedService.saleWithRedisson();
    }
}
