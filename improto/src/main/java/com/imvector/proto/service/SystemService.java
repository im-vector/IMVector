package com.imvector.proto.service;

import com.imvector.logic.PacketInboundHandler;
import com.imvector.proto.impl.IMPacket;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.Packet;
import com.imvector.proto.system.IMSystem;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author: vector.huang
 * @date: 2019/04/25 13:57
 */
@Service("Service" + Packet.ServiceId.SYSTEM_VALUE)
public class SystemService implements PacketInboundHandler<UserDetail, IMPacket> {

    private Logger logger = LoggerFactory.getLogger(SystemService.class);

    @Override
    public void packetRead(UserDetail userDetail, ChannelHandlerContext ctx, IMPacket msg) throws Exception {

        switch (msg.getCommandId()) {
            case IMSystem.CommandId.SYSTEM_NOON_VALUE:
                //心跳，原封不动返回
                logger.debug("收到心跳了: {}", userDetail.getUserId());
                ctx.writeAndFlush(msg);
                break;
            default:
                logger.warn("未知请求：{}-{}", msg.getServiceId(), msg.getCommandId());
                break;
        }

    }
}
