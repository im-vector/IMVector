package com.imvector.logic;

import io.netty.channel.Channel;

/**
 * @author: vector.huang
 * @date: 2019/10/02 02:59
 */
public interface IMessageManager<T> {

    /**
     * 添加连接到业务处理层
     *
     * @param userDetail 用户详情
     */
    void addChannel(T userDetail, Channel channel);

    void removeChannel(T userDetail);

}
