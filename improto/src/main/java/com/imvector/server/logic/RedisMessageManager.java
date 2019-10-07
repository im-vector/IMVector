package com.imvector.server.logic;

import com.google.protobuf.InvalidProtocolBufferException;
import com.imvector.config.NettyConfig;
import com.imvector.logic.IMessageManager;
import com.imvector.proto.impl.IMPacket;
import com.imvector.server.entity.ChannelSession;
import com.imvector.server.entity.UserDetail;
import com.imvector.server.proto.IMUtil;
import com.imvector.server.proto.Packet;
import com.imvector.server.proto.chat.Chat;
import com.imvector.server.proto.system.IMSystem;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * redis 消息队列管理器
 *
 * @author: vector.huang
 * @date: 2019/05/24 14:35
 */
@Component("redisMessageManager")
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnMissingBean(name = "customMessageManager")
public class RedisMessageManager extends RedisMessageListenerContainer
        implements IMessageManager<UserDetail, IMPacket>, MessageListener {

    /**
     * 管理本地全部的Channel
     */
    private final Map<UserDetail, Map<Integer, ChannelSession>> channels;

    private final IMPackageRedisTemplate redisTemplate;
    /**
     * IM 服务的节点数
     */
    private final NettyConfig nettyConfig;
    private Logger logger = LoggerFactory.getLogger(RedisMessageManager.class);
    /**
     * 发送消息的
     */
    private String imMsgChannel = "IM_MSG";

    public RedisMessageManager(RedisConnectionFactory redisConnectionFactory,
                               IMPackageRedisTemplate redisTemplate, NettyConfig nettyConfig) {
        this.redisTemplate = redisTemplate;
        this.nettyConfig = nettyConfig;
        setConnectionFactory(redisConnectionFactory);
        channels = new ConcurrentHashMap<>();

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

        var userChannels = channels.computeIfAbsent(userDetail, k -> new ConcurrentHashMap<>());

        // 如果还存在相同平台的连接，直接断开
        var oldSession = userChannels.get(userDetail.getPlatformSeq());
        if (oldSession != null) {
            // 存在，1. 发送登出信息，2. 断开连接
            var logoutOut = IMSystem.LogoutOut.newBuilder();
            logoutOut.setUserId(userDetail.getUserId());
            logoutOut.setStatus(IMSystem.LogoutStatus.OTHER_DEVICE);
            var packet = IMUtil.newPacket(Packet.ServiceId.SYSTEM, IMSystem.CommandId.SYSTEM_LOGOUT, logoutOut);
            var version = oldSession.getUserDetail().getVersion();
            packet.setVersion(version);
            // 发送
            oldSession.getChannel().writeAndFlush(version);
            // 断开
            oldSession.getChannel().close();
        }

        var session = new ChannelSession();
        session.setUserDetail(userDetail);
        session.setChannel(channel);

        userChannels.put(userDetail.getPlatformSeq(), session);
    }

    @Override
    public void removeChannel(UserDetail userDetail) {

        var userChannels = channels.get(userDetail);
        if (userChannels == null) {
            return;
        }
        userChannels.remove(userDetail.getPlatformSeq());

        // 如果没有了，整个移除
        if (userChannels.isEmpty()) {
            channels.remove(userDetail);
        }
    }

    /**
     * 发送消息到本地队列
     */
    private boolean sendLocalMessage(UserDetail userDetail, IMPacket packet) {
        //尝试本地发送
        var userChannels = channels.get(userDetail);
        if (userChannels == null) {
            return false;
        }

        // 需要保证全部客户端连接到同一台服务器
        // 全部平台都发送过去
        userChannels.forEach((k, session) -> {
            // 需要版本号一致
            packet.setVersion(session.getUserDetail().getVersion());
            session.getChannel().writeAndFlush(packet);
        });
        return true;
    }

    /**
     * 发送消息到队列
     */
    @Override
    public void sendMessage(UserDetail userDetail, IMPacket packet) {

        //尝试本地发送
        var ok = sendLocalMessage(userDetail, packet);
        if (ok) {
            return;
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
    }

    @Override
    public void sendMessageNotChannel(UserDetail userDetail, IMPacket packet, Channel channel) {
        // 只会出现在发送消息给自己的情况，所以不需要redis
        //尝试本地发送
        var userChannels = channels.get(userDetail);
        if (userChannels == null) {
            return;
        }

        // 需要保证全部客户端连接到同一台服务器
        // 全部平台都发送过去
        userChannels.forEach((k, session) -> {
            if (channel == session.getChannel()) {
                return;
            }
            // 需要版本号一致
            packet.setVersion(session.getUserDetail().getVersion());
            session.getChannel().writeAndFlush(packet);
        });
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
                sendLocalMessage(new UserDetail(to), header);
            } catch (InvalidProtocolBufferException e) {
                logger.error("解析MsgOut 出错", e);
            }
        } else {
            logger.warn("非法话题：{}", topic);
        }
    }
}

