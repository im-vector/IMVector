package com.imvector.proto.service;

import com.imvector.logic.IRoomManager;
import com.imvector.logic.PacketInboundHandler;
import com.imvector.proto.IMUtil;
import com.imvector.proto.Packet;
import com.imvector.proto.chat.room.Room;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.impl.IMPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

/**
 * @author: vector.huang
 * @date: 2020/07/15 12:41
 */
@Service("Service" + Packet.ServiceId.ROOM_VALUE)
public class RoomService implements PacketInboundHandler<UserDetail, IMPacket> {

    private final IRoomManager roomManager;

    public RoomService(IRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void packetRead(UserDetail userDetail, ChannelHandlerContext ctx, IMPacket packet) throws Exception {
        switch (packet.getCommandId()) {
            case Room.CommandId.CREATE_ROOM_VALUE:
                // 创建聊天室
                var roomNo = roomManager.createRoom(userDetail, packet);

                //3. 给出响应，告诉发送方，服务器已经收到消息了
                var msgRespBuilder = Room.CreateRoomResp.newBuilder();
                msgRespBuilder.setStatus(roomNo == null ? Packet.Status.ERR_CLIENT : Packet.Status.OK);
                msgRespBuilder.setRoomNo(roomNo);
                var packetResp = IMUtil.copyPacket(packet, msgRespBuilder);
                ctx.writeAndFlush(packetResp);
                break;
            default:
                break;

        }
    }
}
