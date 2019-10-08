package com.imvector.proto.logic;

import com.imvector.logic.IMessageManager;
import com.imvector.proto.IMUtil;
import com.imvector.proto.Packet;
import com.imvector.proto.entity.ChannelSession;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.impl.IMPacket;
import com.imvector.proto.system.IMSystem;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: vector.huang
 * @date: 2019/10/07 18:50
 */
@Component("protoMemoryMessageManager")
@ConditionalOnMissingBean(name = {"customMessageManager", "redisMessageManager"})
public class ProtoMemoryMessageManager implements IMessageManager<UserDetail, IMPacket> {

    /**
     * 管理本地全部的Channel
     */
    private final Map<UserDetail, Map<Integer, ChannelSession>> channels;


    public ProtoMemoryMessageManager() {
        channels = new ConcurrentHashMap<>();
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
            oldSession.getChannel().writeAndFlush(packet);
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

        var session = userChannels.get(userDetail.getPlatformSeq());
        if (session != null
                && session.getUserDetail() == userDetail) {
            // 需要同一个才能移除，防止移除错误的
            userChannels.remove(userDetail.getPlatformSeq());
            // 如果没有了，整个移除
            if (userChannels.isEmpty()) {
                channels.remove(userDetail);
            }
        }

    }

    @Override
    public boolean onLine(UserDetail userDetail) {
        return channels.containsKey(userDetail);
    }

    /**
     * 发送消息到队列
     */
    @Override
    public boolean sendMessage(UserDetail userDetail, IMPacket packet) {
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


}
