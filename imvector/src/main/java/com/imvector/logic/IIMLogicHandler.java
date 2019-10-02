package com.imvector.logic;

import com.imvector.InitChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

/**
 * @author: vector.huang
 * @date: 2019/10/02 01:49
 */
public interface IIMLogicHandler<T> {

    /**
     * 获取业务层处理器
     * 参考 {@link InitChannel} 的实现
     *
     * @param userDetail 用户详情，需要处理的用户
     * @param channel    对应的连接
     * @return 业务层处理器
     */
    ChannelHandler[] getLogicHandler(T userDetail, Channel channel);

}
