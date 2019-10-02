package com.imvector.logic.impl;

import com.imvector.logic.IMessageManager;
import com.imvector.logic.PacketInboundHandler;
import com.imvector.proto.IIMPacket;
import com.imvector.utils.SpringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单机实现，内存处理
 *
 * @author: vector.huang
 * @date: 2019/04/22 17:42
 */
public class IMServiceHandler<T> extends SimpleChannelInboundHandler<IIMPacket> {

    private Logger logger = LoggerFactory.getLogger(IMServiceHandler.class);

    /**
     * 连接的用户id
     */
    private T userDetail;

    public IMServiceHandler(T userDetail, Channel channel) {
        this.userDetail = userDetail;

        SpringUtils.getBean(IMessageManager.class)
                .addChannel(userDetail, channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端连接成功");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IIMPacket msg) throws Exception {
        logger.info("收到一条消息: {}", msg.getLogicServiceId());

        var handler = SpringUtils.getBean("Service" + msg.getLogicServiceId(), PacketInboundHandler.class);
        handler.packetRead(userDetail, ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端断开连接");

        //这个方法不会抛出异常，如果订阅失败（例如，超时了，也不会出现错误，只是打印了个警告信息）
        //这里倒无所谓，因为只是远程无法断开，实际也是断开了

        SpringUtils.getBean(IMessageManager.class)
                .removeChannel(userDetail);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("发生了异常，主动断开连接：{}", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info("收到了userEvent: " + evt);

        if (evt instanceof IdleStateEvent) {
            //主动关闭，还是会回调channelInactive的
            ctx.close();
            logger.debug("因为空闲太久，主动断开连接");
        }
    }
}
