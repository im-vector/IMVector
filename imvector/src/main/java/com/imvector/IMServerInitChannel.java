package com.imvector;

import com.imvector.map.IIMMapHandler;
import com.imvector.proto.IIMProtocolCodec;
import io.netty.channel.socket.SocketChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author: vector.huang
 * @date: 2019/04/23 11:31
 */
@Component
public class IMServerInitChannel implements InitChannel {

    private final IIMProtocolCodec protocolCodec;
    private final IIMMapHandler mapHandler;

    public IMServerInitChannel(IIMProtocolCodec protocolCodec,
                               IIMMapHandler mapHandler) {
        this.protocolCodec = protocolCodec;
        this.mapHandler = mapHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) {

        ch.pipeline()
                // 编解码
                .addLast(protocolCodec.getProtocolCodec())
                // 过滤映射层
                .addLast(mapHandler.getMapHandler());
    }
}
