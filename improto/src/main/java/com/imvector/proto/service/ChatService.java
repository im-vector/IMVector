package com.imvector.proto.service;

import com.imvector.logic.IMessageManager;
import com.imvector.logic.PacketInboundHandler;
import com.imvector.proto.impl.IMPacket;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.IMUtil;
import com.imvector.proto.Packet;
import com.imvector.proto.chat.Chat;
import com.imvector.utils.SpringUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: vector.huang
 * @date: 2019/05/24 13:57
 */
@Service("Service" + Packet.ServiceId.CHAT_VALUE)
public class ChatService implements PacketInboundHandler<UserDetail, IMPacket> {

    private final IMessageManager<UserDetail, IMPacket> messageManager;
    private final IChatDao chatDao;

    @Autowired
    public ChatService(IMessageManager<UserDetail, IMPacket> messageManager) {
        this.messageManager = messageManager;
        chatDao = SpringUtils.getBean(IChatDao.class);
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

        //1. 保存到数据库，返回接口为是否成功
        boolean already = false;
        if (chatDao != null) {
            already = chatDao.save(header.getSeq(), msgReq);
        }

        //2. 不是已经发送够的，就转发给对方（发布到redis）
        if (!already) {
            var msgOutBuilder = Chat.MsgOut.newBuilder();
            msgOutBuilder.setFrom(userId);
            msgOutBuilder.setTo(msgReq.getTo());
            msgOutBuilder.setMsgId(msgReq.getMsgId());

            msgOutBuilder.setChatType(msgReq.getChatType());
            msgOutBuilder.setMsgType(msgReq.getMsgType());

            msgOutBuilder.setUri(msgReq.getUri());
            msgOutBuilder.setContent(msgReq.getContent());

            var msgOut = IMUtil.newPacket(Packet.ServiceId.CHAT,
                    Chat.CommandId.CHAT_MSG_OUT,
                    msgOutBuilder);
            //异步的，很快，不会阻塞
            if (userId == msgReq.getTo()) {
                messageManager.sendMessageNotChannel(new UserDetail(msgReq.getTo()), msgOut, ctx.channel());
            } else {
                messageManager.sendMessage(new UserDetail(msgReq.getTo()), msgOut);
            }
        }


        //3. 给出响应，告诉发送方，服务器已经收到消息了
        var msgRespBuilder = Chat.MsgResp.newBuilder();
        msgRespBuilder.setMsgId(msgReq.getMsgId());

        var packetResp = IMUtil.copyPacket(header, msgRespBuilder);
        ctx.writeAndFlush(packetResp);

    }
}
