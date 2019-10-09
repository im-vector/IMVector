package com.imvector;

import com.imvector.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author: vector.huang
 * @date: 2019/03/18 13:13
 */
@Component
@EnableConfigurationProperties(NettyConfig.class)
public class IMServer implements ApplicationListener<ApplicationEvent> {

    private final NettyConfig nettyConfig;
    private final InitChannel initChannel;

    private Logger logger = LoggerFactory.getLogger(IMServer.class);

    private ChannelFuture channelFuture;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    public IMServer(NettyConfig nettyConfig, InitChannel initChannel) {
        this.nettyConfig = nettyConfig;
        this.initChannel = initChannel;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        if (event instanceof ApplicationStartedEvent) {
            var context = ((ApplicationStartedEvent) event).getApplicationContext();
            if (context instanceof AnnotationConfigApplicationContext) {
                //不需要Servlet 也启动完成
                try {
                    //不可以阻塞，否则SpringBoot 就无法启动了
                    run(nettyConfig.getPort(), initChannel);
                } catch (Exception e) {
                    shutdownGracefully();
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (event instanceof ContextClosedEvent) {
            //SpringBoot 关闭的同时，关闭Netty
            channelFuture.channel().close();
            shutdownGracefully();
        }
    }

    /**
     * 关闭事件
     */
    private void shutdownGracefully() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
    }

    /**
     * 运行IM服务
     */
    private void run(int port, InitChannel initChannel) throws Exception {

        logger.info("开始启动IMServer...");

        /*
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
         * Netty 提供了许多不同的 EventLoopGroup 的实现用来处理不同的传输。
         * 在这个例子中我们实现了一个服务端的应用，因此会有2个 NioEventLoopGroup 会被使用。
         * 第一个经常被叫做‘boss’，用来接收进来的连接。
         * 第二个经常被叫做‘worker’，用来处理已经被接收的连接。
         * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
         * 如何知道多少个线程已经被使用，如何映射到已经创建的 Channel上都需要依赖于 EventLoopGroup 的实现，
         * 并且可以通过构造函数来配置他们的关系。
         */
        //boss 多线程事件循环器
        bossGroup = new NioEventLoopGroup();
        //worker 多线程事件循环器
        workerGroup = new NioEventLoopGroup();

        /*
         * ServerBootstrap 是一个启动 NIO 服务的辅助启动类。
         * 你可以在这个服务中直接使用 Channel，但是这会是一个复杂的处理过程，
         * 在很多情况下你并不需要这样做。
         */
        var b = new ServerBootstrap();
        //加入事件循环器
        b.group(bossGroup, workerGroup);
        /*
         * 当客户端连接上来的时候就会产生新的Channel
         * NioServerSocketChannel 类指定了该新的 Channel 如何连接进来
         * Nio就是异步连接进来
         */
        b.channel(NioServerSocketChannel.class);

        /*
         * 当有新的Channel 连接进来之后就会调用该ChannelInitializer来初始化该Channel
         * 主要工作就是添加很多ChannelHandler 到PipelineChannel上，用来处理数据
         * 当你的程序变的复杂时，可能你会增加更多的处理类到 pipeline 上
         */
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                /*
                 * 当有新的Channel需要处理的时候
                 * 把Handler 添加到PipelineChannel 上
                 */
                initChannel.initChannel(ch);
            }
        });
        /*
         * 你可以设置这里指定的 Channel 实现的配置参数。
         * 我们正在写一个TCP/IP 的服务端，因此我们被允许
         * 设置 socket 的参数选项比如tcpNoDelay 和 keepAlive。
         * 请参考 ChannelOption 和详细的 ChannelConfig 实现的接口文档
         * 以此可以对ChannelOption 的有一个大概的认识。
         */
        b.option(ChannelOption.SO_BACKLOG, 128);
        /*
         * 你关注过 option() 和 childOption() 吗？
         * option() 是提供给NioServerSocketChannel 用来接收进来的连接。
         * childOption() 是提供给由父管道 ServerChannel 接收到的连接，
         * 在这个例子中也是 NioServerSocketChannel。
         */
        b.childOption(ChannelOption.SO_KEEPALIVE, true);
        b.childOption(ChannelOption.TCP_NODELAY, true);

        //绑定端口，开始接收进来的连接
        //sync 等待绑定成功
        var f = b.bind(port);
        f.addListener(future -> {
            if (future.isSuccess()) {
                logger.info("IMServer 启动成功，绑定端口为：" + nettyConfig.getPort());
            } else {
                //例如，端口被占用了:Address already in use
                logger.info("IMServer 启动失败：" + future.cause().getMessage());
            }
        });
        //阻塞绑定端口，只有绑定成功了才能往下走
        f.sync();
        /*
         * 等待服务器  socket 关闭 。
         * 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
         */
        channelFuture = f.channel().closeFuture();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                logger.info("成功关闭IMServer");
            } else {
                logger.info("关闭IMServer 启动失败：" + future.cause().getMessage());
            }
        });
    }
}
