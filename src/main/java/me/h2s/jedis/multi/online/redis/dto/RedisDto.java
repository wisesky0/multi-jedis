package me.h2s.jedis.multi.online.redis.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

public class RedisDto {

    @ApiModelProperty(required = true, value = "redis key", example = "mykey")
    private String key;

    @ApiModelProperty(required = true, value = "redis value of key", example = "this is value of key !!!")
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RedisDto{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
