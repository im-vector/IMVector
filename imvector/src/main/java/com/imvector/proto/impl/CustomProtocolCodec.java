package com.imvector.proto.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * 需要结合new LengthFieldBasedFrameDecoder(1024 * 1024,16, 4,0, 0,true) 使用
 * <p>
 * 自定义协议加密
 * <p>
 * 协议头+ProtoBuf内容                          编码后的内容，中间插入4个字节的长度
 * +---------+---------------+               +---------+-------------+---------------+
 * | 协议头   | Protobuf Data |<------------->| 协议头   | Length     | Protobuf Data |
 * | 固定字节 |  (300 bytes)  |               | 固定字节 | 0x0000AC02  |  (300 bytes)  |
 * +---------+---------------+               +---------+-------------+---------------+
 *
 * @author: vector.huang
 * @date: 2019/06/11 11:55
 */
public class CustomProtocolCodec extends ByteToMessageCodec<IMPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, IMPacket msg, ByteBuf out) throws Exception {
        //计算body length
        byte[] body = msg.getBody();

        //设置内容的长度
        out.ensureWritable(IMPacket.HEAD_LENGTH + body.length);

        out.writeInt(IMPacket.HEAD_LENGTH);
        out.writeInt(msg.getVersion());
        out.writeShort(msg.getServiceId());
        out.writeShort(msg.getCommandId());
        out.writeInt(msg.getSeq());
        out.writeInt(body.length);
        out.writeBytes(body);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //读取协议头
        IMPacket message = new IMPacket();

        //跳过头长度
        in.skipBytes(4);
        int version = in.readInt();
        short serviceId = in.readShort();
        short commandId = in.readShort();
        int seq = in.readInt();
        //跳过body 长度，剩下的都是body了
        in.skipBytes(4);

        message.setVersion(version);
        message.setServiceId(serviceId);
        message.setCommandId(commandId);
        message.setSeq(seq);
        byte[] body = new byte[in.readableBytes()];
        in.readBytes(body);
        message.setBody(body);

        out.add(message);
    }

}
