package com.imvector;

import io.netty.channel.socket.SocketChannel;

/**
 * @author: vector.huang
 * @date: 2019/03/18 13:17
 */
public interface InitChannel {

    /**
     * 当有连接连接过来，需要初始化Channel，例如，添加这种Handler
     */
    void initChannel(SocketChannel ch);

}
