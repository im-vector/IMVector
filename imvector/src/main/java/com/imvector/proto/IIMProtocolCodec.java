package com.imvector.proto;

import com.imvector.InitChannel;
import io.netty.channel.ChannelHandler;

/**
 * @author: vector.huang
 * @date: 2019/10/02 00:47
 */
public interface IIMProtocolCodec {


    /**
     * 获取协议编解码器，用于对帧数据进行编解码
     * 将按顺序加入到pipeline 中
     * 参考 {@link InitChannel} 的实现
     * @return 编解码器
     */
    ChannelHandler[] getProtocolCodec();


}
