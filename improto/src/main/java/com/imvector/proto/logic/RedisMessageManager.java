package com.imvector.proto.logic;

import com.google.protobuf.InvalidProtocolBufferException;
import com.imvector.config.NettyConfig;
import com.imvector.proto.chat.Chat;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.impl.IMPacket;
import io.lettuce.core.internal.HostAndPort;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;

/**
 * redis 消息队列管理器
 *
 * @author: vector.huang
 * @date: 2019/05/24 14:35
 */
//@Component("redisMessageManager")
//@ConditionalOnClass(RedisConnectionFactory.class)
//@ConditionalOnMissingBean(name = "customMessageManager")
public class RedisMessageManager extends RedisMessageListenerContainer
        implements IRedisMessageManager<UserDetail, IMPacket>, MessageListener {

    private final Logger logger = LoggerFactory.getLogger(RedisMessageManager.class);

    /**
     * 组合本地链接
     */
    private final ProtoMemoryMessageManager protoMemoryMessageManager;

    /**
     * IM 服务的节点数
     */
    private final NettyConfig nettyConfig;
    private final IMPackageRedisTemplate redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 发送消息的
     */
    private final String imMsgChannel = "IM_MSG";
    private final String IM_VECTOR_USERS = "IM_VECTOR_USERS";

    public RedisMessageManager(RedisConnectionFactory redisConnectionFactory,
                               IMPackageRedisTemplate redisTemplate,
                               NettyConfig nettyConfig,
                               StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;

        protoMemoryMessageManager = new ProtoMemoryMessageManager();

        this.redisTemplate = redisTemplate;
        this.nettyConfig = nettyConfig;
        setConnectionFactory(redisConnectionFactory);

        if (nettyConfig.getNodeNum() > 1) {
            //分布式服务
            var topics = new ArrayList<ChannelTopic>();
            topics.add(new ChannelTopic(imMsgChannel));

            //这个方法不会抛出异常，如果订阅失败（例如，超时了，也不会出现错误，只是打印了个警告信息）
            //现在要求：当定义失败的时候，断开连接，请求重新登录
            //如果Redis 出错，好像会无限次重试，指导Redis 服务恢复，需要结合警告来弄
            //当出现Redis 服务错误的时候，人工恢复
            //如果Redis 出现错误的时候，整个系统是无法使用的，包含登录。
            addMessageListener(this, topics);
        }
    }

    @Override
    public void addChannel(UserDetail userDetail, Channel channel) {
        protoMemoryMessageManager.addChannel(userDetail, channel);

        // 告诉redis，我上线了
        if (nettyConfig.getNodeNum() > 1) {
            HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
            ops.put(IM_VECTOR_USERS, userDetail.getUserId() + "",
                    nettyConfig.getHost() + ":" + nettyConfig.getPort());
        }
    }

    @Override
    public void removeChannel(UserDetail userDetail) {
        protoMemoryMessageManager.removeChannel(userDetail);

        if (!onLine(userDetail) && nettyConfig.getNodeNum() > 1) {
            // 告诉redis，我下线了
            HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
            ops.delete(IM_VECTOR_USERS, userDetail.getUserId() + "");
        }
    }

    @Override
    public boolean onLine(UserDetail userDetail) {

        return protoMemoryMessageManager.onLine(userDetail);
    }

    /**
     * 发送消息到队列
     */
    @Override
    public boolean sendMessage(UserDetail userDetail, IMPacket packet) {

        //尝试本地发送
        var ok = protoMemoryMessageManager.sendMessage(userDetail, packet);
        if (ok) {
            return true;
        }

        // 本地没有发现，那么就去其他服务器找
        try {
            if (nettyConfig.getNodeNum() > 1) {
                //尝试分布式发送
                //可能在其他节点登录了
                redisTemplate.convertAndSend(imMsgChannel, packet);
            }
        } catch (Exception e) {
            //当和Redis 断开连接之后，就可能出现异常
            //org.springframework.data.redis.RedisSystemException:
            //Redis exception; nested exception is io.lettuce.core.RedisException:
            //java.io.IOException: Operation timed out
            //Unable to unsubscribe from subscriptions
            logger.error("发布信息到Redis 错误", e);
        }
        return false;
    }

    @Override
    public void sendMessageNotChannel(UserDetail userDetail, IMPacket packet, Channel channel) {

        protoMemoryMessageManager.sendMessageNotChannel(userDetail, packet, channel);
    }

    /**
     * 收到Redis 发布的消息
     *
     * @param message 消息
     * @param pattern 频道
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        var topic = new String(pattern);
        var headerBytes = message.getBody();
        var header = IMPacket.parseFrom(headerBytes);

        if (topic.equals(imMsgChannel)) {
            //发送消息过来了
            //版本号必须一致，不然客户端认为不兼容，就会断开连接，或者不处理
            try {
                var msgOut = Chat.MsgOut.parseFrom(header.getBody());

                //发送给谁的
                var to = msgOut.getTo();
                //尝试本地发送
                protoMemoryMessageManager.sendMessage(new UserDetail(to), header);
            } catch (InvalidProtocolBufferException e) {
                logger.error("解析MsgOut 出错", e);
            }
        } else {
            logger.warn("非法话题：{}", topic);
        }
    }

    @Override
    public HostAndPort getOnLine(UserDetail userDetail) {
        if (onLine(userDetail)) {
            return null;
        }

        HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
        String hostPort = ops.get(IM_VECTOR_USERS, userDetail.getUserId() + "");

        if (hostPort == null) {
            return null;
        }
        return HostAndPort.parse(hostPort);
    }
}

