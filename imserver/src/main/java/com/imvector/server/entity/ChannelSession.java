package com.imvector.server.entity;

import io.netty.channel.Channel;

/**
 * @author: vector.huang
 * @date: 2019/06/21 22:59
 */
public class ChannelSession {

    private int userId;
    private int version;
    private Channel channel;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
