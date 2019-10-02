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
    private int port = 8082;
    /**
     * IM 服务的阶段数量
     */
    private int nodeNum = 1;

    /**
     * 节点的各个主机，这个给客户端用的哦，需要做负载均衡
     */
    private String[] hosts;

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

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }
}
