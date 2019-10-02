package com.imvector.map;

import com.imvector.proto.IIMPacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author: vector.huang
 * @date: 2019/04/25 14:18
 */
public interface MapInboundHandler<T, P extends IIMPacket> {

    /**
     * 读取一个消息包
     *
     * @param ctx    上下文
     * @param packet 消息包
     * @return 用户详情，当用户详情不为null 的时候，代表可以进入业务处理层
     * @throws Exception 异常
     */
    T packetRead(ChannelHandlerContext ctx,
                 P packet) throws Exception;

}
