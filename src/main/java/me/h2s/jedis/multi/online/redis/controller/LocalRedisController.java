package me.h2s.jedis.multi.online.redis.controller;

import me.h2s.jedis.multi.online.config.LocalRedisConfig.LocalStringRedisTemplate;
import me.h2s.jedis.multi.online.redis.dto.RedisDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("local-redis")
public class LocalRedisController {

    Logger log = LoggerFactory.getLogger(LocalRedisController.class);
    ValueOperations<String, String> valueOperations;

    public LocalRedisController(ObjectProvider<LocalStringRedisTemplate> objectProvider) {
        objectProvider.ifAvailable(localStringRedisTemplate -> {
            this.valueOperations = localStringRedisTemplate.opsForValue();
        });
    }

    @GetMapping("redis")
    public String getRedis(@RequestParam(value= "key", defaultValue = "mykey") String redisKey) {
        return valueOperations.get(redisKey);
    }

    @PutMapping("redis")
    public void putRedis(@RequestBody RedisDto redisDto){
        log.error("redis put parameter : {}", redisDto);
        valueOperations.set(redisDto.getKey(), redisDto.getValue());
    }
}
