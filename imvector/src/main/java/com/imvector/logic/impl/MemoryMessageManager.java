package com.imvector.logic.impl;

import com.imvector.logic.IMessageManager;
import com.imvector.proto.IIMPacket;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: vector.huang
 * @date: 2019/10/02 02:57
 */
public class MemoryMessageManager<T, P extends IIMPacket> implements IMessageManager<T, P> {

    /**
     * 管理本地全部的Channel
     */
    private final Map<T, Channel> channels;

    public MemoryMessageManager() {
        channels = new HashMap<>();
    }

    @Override
    public void addChannel(T userDetail, Channel channel) {
        channels.put(userDetail, channel);
    }

    @Override
    public void removeChannel(T userDetail) {
        channels.remove(userDetail);
    }

    @Override
    public void sendMessage(T userDetail, P packet) {

        // 直接发送，如果不在线消息将会被忽略
        var channel = channels.get(userDetail);
        if (channel != null) {
            channel.writeAndFlush(packet);
        }

    }
}
