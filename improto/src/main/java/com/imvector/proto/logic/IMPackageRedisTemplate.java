package com.imvector.proto.logic;

import com.imvector.proto.impl.IMPacket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

/**
 * 每一帧数据的转码，IMPacket <=> byte[]
 * @author: vector.huang
 * @date: 2019/05/24 16:26
 */
@Component
@ConditionalOnClass(RedisConnectionFactory.class)
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
