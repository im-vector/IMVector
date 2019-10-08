package com.imvector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: vector.huang
 * @date: 2019/04/23 11:16
 */
@Component
@ConfigurationProperties("imvector")
public class NettyConfig {

    /**
     * Netty 监听的端口
     */
    private int port = 9087;
    /**
     * IM 服务的阶段数量
     */
    private int nodeNum = 1;

    /**
     * 节点的主机 127.0.0.1
     */
    private String host = "127.0.0.1";

    public NettyConfig() {
    }

    public NettyConfig(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
