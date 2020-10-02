package me.h2s.jedis.multi.online.redis.controller;


import me.h2s.jedis.multi.online.config.GlobalRedisConfig.GlobalStringRedisTemplate;
import me.h2s.jedis.multi.online.redis.dto.RedisDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("global-redis")
public class GlobalRedisController {

    Logger log = LoggerFactory.getLogger(GlobalRedisController.class);
    ValueOperations<String, String> valueOperations;

    public GlobalRedisController(ObjectProvider<GlobalStringRedisTemplate> objectProvider) {
        objectProvider.ifAvailable(globalStringRedisTemplate -> {
            this.valueOperations = globalStringRedisTemplate.opsForValue();
        });
    }

    @GetMapping("redis")
    public String getRedis(@RequestParam(value = "key", defaultValue = "mykey") String redisKey) {
        return valueOperations.get(redisKey);
    }

    @PutMapping("redis")
    public void putRedis(@RequestBody RedisDto redisDto){
        log.error("redis put parameter : {}", redisDto);
        valueOperations.set(redisDto.getKey(), redisDto.getValue());
    }
}
