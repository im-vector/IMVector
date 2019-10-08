package com.imvector.proto.logic;

import com.imvector.logic.IMessageManager;
import com.imvector.proto.IIMPacket;
import io.lettuce.core.internal.HostAndPort;

/**
 * @author: vector.huang
 * @date: 2019/10/02 02:59
 */
public interface IRedisMessageManager<T, P extends IIMPacket> extends IMessageManager<T, P> {

    /**
     * 获取用户在线的服务地址
     *
     * @param userDetail 用户详情
     * @return if 不在线 return null, 否则返回对应的地址
     */
    HostAndPort getOnLine(T userDetail);
}
