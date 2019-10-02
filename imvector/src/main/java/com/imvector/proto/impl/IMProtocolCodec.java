package com.imvector.proto.impl;

import com.imvector.proto.IIMProtocolCodec;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * IIMProtocolCodec 的默认实现
 *
 * @author: vector.huang
 * @date: 2019/10/02 01:00
 */
public class IMProtocolCodec implements IIMProtocolCodec {

    @Override
    public ChannelHandler[] getProtocolCodec() {
        return new ChannelHandler[]{
                // 获取《基于字段值长度》的帧
                new LengthFieldBasedFrameDecoder(1024 * 1024, 16, 4, 0, 0, true),
                // Protobuf 编解码
                new CustomProtocolCodec()
        };
    }
}
