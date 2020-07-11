package com.imvector.proto.entity;

import io.netty.channel.Channel;

/**
 * 保存登录成功后的每一条连接的信息。
 * @author: vector.huang
 * @date: 2019/06/21 22:59
 */
public class ChannelSession {

    /**
     * 用户的详情信息
     */
    private UserDetail userDetail;
    /**
     * 连接对应的Channel
     */
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
