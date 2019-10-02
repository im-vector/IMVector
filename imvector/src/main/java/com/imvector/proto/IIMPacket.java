package com.imvector.proto;

/**
 * 协议，代表每一帧的数据
 *
 * @author: vector.huang
 * @date: 2019/10/02 00:31
 */
public interface IIMPacket<T> {

    /**
     * 根据返回的serviceId 获取业务层服务处理器
     * 例如，返回34，那么将获取容器name为LogicServiceId34 的bean
     * @return 请求帧的logicServiceId
     */
    T getLogicServiceId();
}
