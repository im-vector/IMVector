package com.imvector.server.service;

import com.imvector.logic.IMessageManager;
import com.imvector.logic.PacketInboundHandler;
import com.imvector.proto.impl.IMPacket;
import com.imvector.server.entity.UserDetail;
import com.imvector.server.proto.IMUtil;
import com.imvector.server.proto.Packet;
import com.imvector.server.proto.chat.Chat;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: vector.huang
 * @date: 2019/05/24 13:57
 */
@Service("Service" + Packet.ServiceId.CHAT_VALUE)
public class ChatService implements PacketInboundHandler<UserDetail, IMPacket> {

    private final IMessageManager messageManager;

    @Autowired
    public ChatService(
            IMessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public void packetRead(UserDetail userDetail, ChannelHandlerContext ctx, IMPacket header) throws Exception {
        switch (header.getCommandId()) {
            case Chat.CommandId.CHAT_MSG_VALUE:
                //发送消息过来了
                msgReq(userDetail.getUserId(), ctx, header);
                break;
            default:
                break;

        }
    }

    /**
     * 读取用户发送过来的信息
     * 1. 判断合法性，msgId
     * 2. 保存到数据库
     * 3. 转发给对方（发布到redis）
     * 4. 响应
     */
    private void msgReq(int userId, ChannelHandlerContext ctx, IMPacket header) throws Exception {

        //获取msg req
        var msgReq = Chat.MsgReq.parseFrom(header.getBody());

        //1. 判断合法性
        var msgId = msgReq.getMsgId();
        //把UserId 读取出来
        var msgUserId = msgId >> 32;
        if (msgUserId != userId) {
            //非法的msgId
            var msgRespBuilder = Chat.MsgResp.newBuilder();
            msgRespBuilder.setMsgId(msgReq.getMsgId());
            msgRespBuilder.setMsg("非法msgId");
            msgRespBuilder.setStatus(Packet.Status.ERR_CLIENT);

            var packetResp = IMUtil.copyHeader(header, msgRespBuilder);

            ctx.writeAndFlush(packetResp);
            return;
        }

        //2. 保存到数据库
//        imMessageService.save(userId, header, msgReq);

        //3. 转发给对方（发布到redis）（发送给自己的就忽略）
        if (msgReq.getTo() != userId) {
            var msgOutBuilder = Chat.MsgOut.newBuilder();
            msgOutBuilder.setFrom(userId);
            msgOutBuilder.setTo(msgReq.getTo());
            msgOutBuilder.setMsgId(msgReq.getMsgId());

            msgOutBuilder.setChatType(msgReq.getChatType());
            msgOutBuilder.setMsgType(msgReq.getMsgType());

            msgOutBuilder.setUri(msgReq.getUri());
            msgOutBuilder.setContent(msgReq.getContent());

            var msgOut = IMUtil.newHeader(Packet.ServiceId.CHAT,
                    Chat.CommandId.CHAT_MSG_OUT,
                    msgOutBuilder);
            //异步的，很快，不会阻塞
            messageManager.sendMessage(msgReq.getTo(), msgOut);
        }

        //4. 给出响应，告诉发送方，服务器已经收到消息了
        var msgRespBuilder = Chat.MsgResp.newBuilder();
        msgRespBuilder.setMsgId(msgReq.getMsgId());

        var packetResp = IMUtil.copyHeader(header, msgRespBuilder);
        ctx.writeAndFlush(packetResp);

    }
}
