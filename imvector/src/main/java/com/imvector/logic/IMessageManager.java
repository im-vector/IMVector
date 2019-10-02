package com.imvector.logic;

import com.imvector.proto.IIMPacket;
import io.netty.channel.Channel;

/**
 * @author: vector.huang
 * @date: 2019/10/02 02:59
 */
public interface IMessageManager<T, P extends IIMPacket> {

    /**
     * 添加连接到业务处理层
     *
     * @param userDetail 用户详情
     * @param channel    连接
     */
    void addChannel(T userDetail, Channel channel);

    /**
     * 移除Channel
     *
     * @param userDetail 用户详情
     */
    void removeChannel(T userDetail);

    /**
     * 发送消息
     *
     * @param userDetail 用户详情
     * @param packet     发送的消息
     */
    void sendMessage(T userDetail, P packet);

}
