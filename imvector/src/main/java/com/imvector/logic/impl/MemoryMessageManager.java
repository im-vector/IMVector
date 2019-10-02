package com.imvector.logic.impl;

import com.imvector.logic.IMessageManager;
import io.netty.channel.Channel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: vector.huang
 * @date: 2019/10/02 02:57
 */
@Component
@ConditionalOnMissingBean(IMessageManager.class)
public class MemoryMessageManager<T> implements IMessageManager<T> {

    /**
     * 管理本地全部的Channel
     */
    private final Map<T, Channel> channels;

    public MemoryMessageManager() {
        channels = new HashMap<>();
    }

    @Override
    public void addChannel(T userDetail, Channel channel) {
        channels.put(userDetail, channel);
    }

    @Override
    public void removeChannel(T userDetail) {
        channels.remove(userDetail);

    }
}
