package com.imvector.map.impl;

import com.imvector.logic.IIMLogicHandler;
import com.imvector.map.IIMMapHandler;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author: vector.huang
 * @date: 2019/10/02 01:13
 */
@Component("iMMapHandler")
@ConditionalOnMissingBean(name = "customIMMapHandler")
public class IMMapHandler implements IIMMapHandler {

    private final IIMLogicHandler logicHandler;

    public IMMapHandler(IIMLogicHandler logicHandler) {
        this.logicHandler = logicHandler;
    }

    @Override
    public ChannelHandler[] getMapHandler() {
        return new ChannelHandler[]{
                //90 秒未完成登录，将会断开
                new IdleStateHandler(0, 90, 0, TimeUnit.SECONDS),
                //登录处理
                new LoginHandler(logicHandler)
        };
    }
}
