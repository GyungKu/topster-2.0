package com.sparta.topster.global.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class RedisUtil {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public String getData(String key) {
        try {
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            return valueOperations.get(key);
        } catch (Exception e) {
            log.error("RedisUtil getData Error");
            return null;
        }
    }

    public void setDataExpire(String key, String value, long duration) {
        try {
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            Duration expireDuration = Duration.ofSeconds(duration);
            valueOperations.set(key, value, expireDuration);
        } catch (Exception e) {
            log.error("RedisUtil setDataExpire Error");
        }
    }

    public Long increment(String key, long delta) {
        try {
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            return valueOperations.increment(key, delta);
        } catch (Exception e) {
            log.error("RedisUtil increment Error");
            return null;
        }
    }

    public void setExpire(String key, long timeout) {
        redisTemplate.expire(key, timeout, java.util.concurrent.TimeUnit.SECONDS);
    }

    public void deleteData(String key){
        try {
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            valueOperations.getAndDelete(key);
        }catch (Exception e){
            log.error("deleteData Error");
            e.getMessage();
        }
    }
}