package com.imvector.sample;

import com.imvector.config.NettyConfig;
import com.imvector.proto.logic.IMPackageRedisTemplate;
import com.imvector.proto.logic.RedisMessageManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author: vector.huang
 * @date: 2019/10/09 23:17
 */
@Component("customMessageManager")
public class CustomMessageManager extends RedisMessageManager {

    public CustomMessageManager(RedisConnectionFactory redisConnectionFactory,
                                IMPackageRedisTemplate redisTemplate,
                                NettyConfig nettyConfig,
                                StringRedisTemplate stringRedisTemplate) {

        super(redisConnectionFactory, redisTemplate, nettyConfig, stringRedisTemplate);
    }
}
