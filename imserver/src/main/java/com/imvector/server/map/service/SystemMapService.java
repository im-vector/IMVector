package com.imvector.server.map.service;

import com.imvector.map.MapInboundHandler;
import com.imvector.proto.impl.IMPacket;
import com.imvector.server.entity.UserDetail;
import com.imvector.server.map.ILoginService;
import com.imvector.server.proto.IMUtil;
import com.imvector.server.proto.Packet;
import com.imvector.server.proto.system.IMSystem;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author: vector.huang
 * @date: 2019/04/25 13:57
 */
@Service("MapService" + Packet.ServiceId.SYSTEM_VALUE)
public class SystemMapService implements MapInboundHandler<UserDetail, IMPacket> {

    private final ILoginService loginService;
    private Logger logger = LoggerFactory.getLogger(SystemMapService.class);

    public SystemMapService(ILoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public UserDetail packetRead(ChannelHandlerContext ctx, IMPacket msg) throws Exception {

        //处理心跳
        if (msg.getCommandId() == IMSystem.CommandId.SYSTEM_NOON_VALUE) {
            //心跳，原封不动返回
            logger.debug("收到心跳了");
            ctx.writeAndFlush(msg);
            return null;
        }

        if (msg.getCommandId() != IMSystem.CommandId.SYSTEM_LOGIN_VALUE) {
            //未登录，还想处理业务
            ctx.close();
            return null;
        }

        var loginResult = login(msg);
        int userId = (int) loginResult[0];
        long msgId = (long) loginResult[1];

        //响应
        var login = IMSystem.LoginResp.newBuilder();
        login.setStatus(userId > 0 ? Packet.Status.OK : Packet.Status.ERR_CLIENT);
        login.setMaxMsgId(msgId);

        var resp = IMUtil.copyPacket(msg, login);

        ctx.writeAndFlush(resp);

        if (userId <= 0) {
            //登录出错了
            logger.info("用户登录失败");
            ctx.close();
            return null;
        }

        logger.info("用户登录成功,{}", userId);
        return new UserDetail(userId, msg.getVersion());

    }

    /**
     * 登录
     */
    private Object[] login(IMPacket msg) throws Exception {
        var login = IMSystem.LoginReq.parseFrom(msg.getBody());
        var userId = loginService.login(login.getToken());
        var msgId = 0L;
        if (msgId == 0L) {
            msgId = ((long) userId) << 32;
            msgId += 1;
        }
        return new Object[]{userId, msgId};
    }
}
