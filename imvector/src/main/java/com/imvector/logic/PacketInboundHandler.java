package com.imvector.logic;

import com.imvector.proto.IIMPacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author: vector.huang
 * @date: 2019/04/25 14:18
 */
public interface PacketInboundHandler<T, P extends IIMPacket> {

    /**
     * 读取一个消息包
     *
     * @param userDetail 用户详情
     * @param ctx    上下文
     * @param packet 消息包
     * @throws Exception 异常
     */
    void packetRead(T userDetail,
                    ChannelHandlerContext ctx,
                    P packet) throws Exception;

}
