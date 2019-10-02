package com.imvector.server.logic;

import com.google.protobuf.InvalidProtocolBufferException;
import com.imvector.config.NettyConfig;
import com.imvector.logic.IMessageManager;
import com.imvector.proto.impl.IMPacket;
import com.imvector.server.entity.ChannelSession;
import com.imvector.server.entity.UserDetail;
import com.imvector.server.proto.chat.Chat;
import com.imvector.server.proto.system.IMSystem;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Component
public class RedisMessageManager extends RedisMessageListenerContainer
        implements IMessageManager<UserDetail, IMPacket>, MessageListener {

    /**
     * 管理本地全部的Channel
     */
    private final Map<Integer, ChannelSession> channels;
    private final IMPackageRedisTemplate redisTemplate;
    /**
     * 节点随机嘛，用来标识这个节点的
     */
    private final String nodeSeq;
    /**
     * IM 服务的节点数
     */
    private final NettyConfig nettyConfig;
    private Logger logger = LoggerFactory.getLogger(RedisMessageManager.class);
    /**
     * 发送消息的
     */
    private String imMsgChannel = "IM_MSG";
    /**
     * 登录的
     */
    private String imLoginChannel = "IM_LOGIN";

    public RedisMessageManager(RedisConnectionFactory redisConnectionFactory,
                               IMPackageRedisTemplate redisTemplate, NettyConfig nettyConfig) {
        this.redisTemplate = redisTemplate;
        this.nettyConfig = nettyConfig;
        setConnectionFactory(redisConnectionFactory);
        channels = new ConcurrentHashMap<>();

        //获取随机数
        nodeSeq = System.currentTimeMillis() + "" + Math.random();

        if (nettyConfig.getNodeNum() > 1) {
            //分布式服务
            var topics = new ArrayList<ChannelTopic>();
            topics.add(new ChannelTopic(imMsgChannel));
            topics.add(new ChannelTopic(imLoginChannel));

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

        //本地保存
        var channelSession = new ChannelSession();
        channelSession.setUserId(userDetail.getUserId());
        channelSession.setVersion(userDetail.getVersion());
        channelSession.setChannel(channel);

        channels.put(userDetail.getUserId(), channelSession);
        logger.info("在线用户个数: {}", channels.size());

    }

    @Override
    public void removeChannel(UserDetail userDetail) {
        //移除
        channels.remove(userDetail.getUserId());
        logger.info("在线用户个数: {}", channels.size());
    }

    /**
     * 发送消息到队列
     */
    @Override
    public void sendMessage(UserDetail userDetail, IMPacket msg) {

        //尝试本地发送
        var session = channels.get(userDetail.getUserId());
        if (session != null) {
            var channel = session.getChannel();
            channel.writeAndFlush(msg);
            return;
        }

        try {
            if (nettyConfig.getNodeNum() > 1) {
                //尝试分布式发送
                //可能在其他节点登录了
                redisTemplate.convertAndSend(imMsgChannel, msg);
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
                var session = channels.get(to);
                if (session != null) {
                    var channel = session.getChannel();
                    //版本号必须一致
                    header.setVersion(session.getVersion());
                    channel.writeAndFlush(header);
                }
            } catch (InvalidProtocolBufferException e) {
                logger.error("解析MsgOut 出错", e);
            }
        } else if (topic.equals(imLoginChannel)) {
            //其他终端登录了
            try {
                var logoutOut = IMSystem.LogoutOut.parseFrom(header.getBody());
                if (nodeSeq.equals(logoutOut.getSeq())) {
                    //自己登录，不是其他设备哦
                    return;
                }
                //这个用户需要退出了，但是不能是自己哦
                var userId = logoutOut.getUserId();

                //尝试本地发送
                var session = channels.get(userId);
                if (session != null) {
                    var channel = session.getChannel();
                    //版本号必须一致
                    header.setVersion(session.getVersion());
                    channel.writeAndFlush(header);
                    channel.close();
                }
            } catch (InvalidProtocolBufferException e) {
                logger.error("解析LogoutOut 出错", e);
            }

        } else {
            logger.warn("非法话题：{}", topic);
        }
    }
}

