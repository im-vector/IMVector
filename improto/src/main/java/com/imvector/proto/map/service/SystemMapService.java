package com.imvector.proto.map.service;

import com.imvector.logic.IMessageManager;
import com.imvector.map.MapInboundHandler;
import com.imvector.proto.IMUtil;
import com.imvector.proto.Packet;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.impl.IMPacket;
import com.imvector.proto.logic.IRedisMessageManager;
import com.imvector.proto.map.ILoginService;
import com.imvector.proto.system.IMSystem;
import com.imvector.utils.SpringUtils;
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
    public UserDetail packetRead(ChannelHandlerContext ctx, IMPacket packet) throws Exception {

        //处理心跳
        if (packet.getCommandId() == IMSystem.CommandId.SYSTEM_NOON_VALUE) {
            //心跳，原封不动返回
            logger.debug("收到心跳了");
            ctx.writeAndFlush(packet);
            return null;
        }

        if (packet.getCommandId() != IMSystem.CommandId.SYSTEM_LOGIN_VALUE) {
            //未登录，还想处理业务
            ctx.close();
            return null;
        }

        var loginResult = loginService.login(packet);
        int userId = loginResult.getUserId();

        if (loginResult.getStatus() != Packet.Status.OK) {
            //登录出错了
            logger.info("用户登录失败");
            ctx.close();
            return null;
        }

        logger.info("用户登录成功,{}", userId);
        var userDetail = new UserDetail();
        userDetail.setUserId(userId);
        userDetail.setVersion(packet.getVersion());

        // 用户判断不同的客户端，范围 [0,15]
        userDetail.setPlatformSeq(packet.getSeq());

        //判断是否在线
        var messageManager = SpringUtils.getBean(IMessageManager.class);
        if (messageManager instanceof IRedisMessageManager) {
            var bool = messageManager.onLine(userDetail);
            if (!bool) {
                var hostPost = ((IRedisMessageManager) messageManager).getOnLine(userDetail);
                if (hostPost != null) {
                    // 说明在线，其他终端在其他地方上线了
                    var builder = loginResult.newBuilderForType();
                    builder.setHost(hostPost.getHostText());
                    builder.setPort(hostPost.getPort());
                    var resp = IMUtil.copyPacket(packet, loginResult);
                    ctx.writeAndFlush(resp);

                    // 不能进入业务逻辑层，客户端收到切换主机的时候主动断开连接
                    ctx.close();
                    return null;
                }
            }
        }

        var resp = IMUtil.copyPacket(packet, loginResult);
        ctx.writeAndFlush(resp);
        return userDetail;
    }

}
