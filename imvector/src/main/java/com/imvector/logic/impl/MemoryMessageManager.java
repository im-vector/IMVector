package com.imvector.logic.impl;

import com.imvector.logic.IMClientPlatform;
import com.imvector.logic.IMessageManager;
import com.imvector.proto.IIMPacket;
import io.netty.channel.Channel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: vector.huang
 * @date: 2019/10/02 02:57
 */
@Component("memoryMessageManager")
@ConditionalOnMissingBean(name = {"customMessageManager", "redisMessageManager", "protoMemoryMessageManager"})
public class MemoryMessageManager<T extends IMClientPlatform, P extends IIMPacket> implements IMessageManager<T, P> {

    /**
     * 管理本地全部的Channel
     */
    private final Map<T, Map<Integer, Channel>> channels;

    public MemoryMessageManager() {
        channels = new ConcurrentHashMap<>();
    }

    @Override
    public void addChannel(T userDetail, Channel channel) {

        var userChannels = channels.computeIfAbsent(userDetail, k -> new HashMap<>());

        // 如果还存在相同平台的连接，直接断开
        var oldChannel = userChannels.get(userDetail.getPlatformSeq());
        if (oldChannel != null) {
            oldChannel.close();
        }

        userChannels.put(userDetail.getPlatformSeq(), channel);
    }

    @Override
    public void removeChannel(T userDetail) {

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

    @Override
    public boolean onLine(T userDetail) {
        return channels.containsKey(userDetail);
    }

    @Override
    public boolean sendMessage(T userDetail, P packet) {

        // 直接发送，如果不在线消息将会被忽略
        var userChannels = channels.get(userDetail);
        if (userChannels == null) {
            return false;
        }

        // 全部平台都发送过去
        userChannels.forEach((k, channel) -> {
            channel.writeAndFlush(packet);
        });

        return true;
    }

    @Override
    public void sendMessageNotChannel(T userDetail, P packet, Channel channel) {

        // 直接发送，如果不在线消息将会被忽略
        var userChannels = channels.get(userDetail);
        if (userChannels == null) {
            return;
        }

        // 全部平台都发送过去
        userChannels.forEach((k, localChannel) -> {
            if (channel != localChannel) {
                localChannel.writeAndFlush(packet);
            }
        });
    }
}
