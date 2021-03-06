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
     * 用户是否在线
     *
     * @param userDetail 指定用户
     * @return 是否在线
     */
    boolean onLine(T userDetail);

    /**
     * 发送消息
     *
     * @param userDetail 用户详情
     * @param packet     发送的消息
     */
    boolean sendMessage(T userDetail, P packet);

    /**
     * 发送消息，排除指定平台
     *
     * @param userDetail 用户详情
     * @param packet     发送的消息
     * @param channel    排除的Channel
     */
    void sendMessageNotChannel(T userDetail, P packet, Channel channel);
}
