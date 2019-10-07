package com.imvector.proto.entity;

import io.netty.channel.Channel;

/**
 * @author: vector.huang
 * @date: 2019/06/21 22:59
 */
public class ChannelSession {

    private UserDetail userDetail;
    private Channel channel;

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
