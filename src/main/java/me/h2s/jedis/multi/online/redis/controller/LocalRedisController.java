package me.h2s.jedis.multi.online.redis.controller;

import me.h2s.jedis.multi.online.redis.dto.RedisDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("local-redis")
public class LocalRedisController {

    Logger log = LoggerFactory.getLogger(LocalRedisController.class);
    ValueOperations<String, String> valueOperations;
    RedisConnection redisConnection;

    public LocalRedisController(@Qualifier("stringRedisLocalTemplate") StringRedisTemplate stringRedisTemplate, @Qualifier("redisLocalConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        this.valueOperations = stringRedisTemplate.opsForValue();
        this.redisConnection = redisConnectionFactory.getConnection();
    }

    @GetMapping("ping")
    public String pingRedis() {
        return redisConnection.ping();
    }

    @GetMapping("redis")
    public String getRedis(@RequestParam String redisKey) {
        return valueOperations.get(redisKey);
    }

    @PutMapping("redis")
    public void putRedis(@RequestBody RedisDto redisDto){
        log.error("redis put parameter : {}", redisDto);
        valueOperations.set(redisDto.getKey(), redisDto.getValue());
    }
}
