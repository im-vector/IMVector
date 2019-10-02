package com.imvector.map.impl;

import com.imvector.logic.IIMLogicHandler;
import com.imvector.map.MapInboundHandler;
import com.imvector.proto.IIMPacket;
import com.imvector.utils.SpringUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登录处理
 *
 * @author: vector.huang
 * @date: 2019/04/25 15:50
 */
public class LoginHandler extends SimpleChannelInboundHandler<IIMPacket> {

    private final IIMLogicHandler logicHandler;
    private Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    public LoginHandler(IIMLogicHandler logicHandler) {
        this.logicHandler = logicHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.debug("连接进来了");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.debug("连接走了");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        logger.debug("连接注册了");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IIMPacket msg) throws Exception {
        logger.info("收到一条消息: {}", msg.getLogicServiceId());

        var handler = SpringUtils.getBean("MapService" + msg.getLogicServiceId(), MapInboundHandler.class);
        if (handler == null) {
            // 如果没有处理器，那么直接关闭
            logger.error("不存在映射层处理服务：{}", msg.getLogicServiceId());
            ctx.close();
            return;
        }
        var userDetail = handler.packetRead(ctx, msg);
        if (userDetail != null) {
            var logicHandlers = logicHandler.getLogicHandler(userDetail, ctx.channel());

            //登录成功，这个处理器就会替换为业务处理处理器
            //先移除不需要的处理器
            ctx.pipeline().remove(this);
            var idleHandler = ctx.pipeline().get(IdleStateHandler.class);
            if (idleHandler != null) {
                for (ChannelHandler channelHandler : logicHandlers) {
                    if (channelHandler instanceof IdleStateHandler) {
                        ctx.pipeline().remove(idleHandler);
                    }
                }
            }

            // 再添加全部业务处理器
            ctx.pipeline().addLast(logicHandlers);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.debug("收到了userEvent{}", evt);

        if (evt instanceof IdleStateEvent) {
            ctx.close();
            logger.debug("因为空闲太久，主动断开连接");
        }
    }

}
