package com.imvector.server.logic;

import com.imvector.proto.impl.IMPacket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

/**
 * @author: vector.huang
 * @date: 2019/05/24 16:26
 */
@Component
@ConditionalOnBean(RedisConnectionFactory.class)
public class IMPackageRedisTemplate extends RedisTemplate<String, Object> {

    public IMPackageRedisTemplate(RedisConnectionFactory connectionFactory) {

        setConnectionFactory(connectionFactory);
        setValueSerializer(new RedisSerializer<IMPacket>() {
            @Override
            public byte[] serialize(IMPacket header) throws SerializationException {
                return header.toByteArray();
            }

            @Override
            public IMPacket deserialize(byte[] bytes) throws SerializationException {
                return IMPacket.parseFrom(bytes);
            }
        });
    }
}
