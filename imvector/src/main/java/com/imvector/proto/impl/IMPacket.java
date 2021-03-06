package com.imvector.proto.impl;

import com.imvector.proto.IIMPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 默认协议参考了mars
 *
 * @author: vector.huang
 * @date: 2019/06/11 09:26
 */
public class IMPacket implements IIMPacket<Short> {

    /**
     * 固定的 4*5=20
     */
    public static final int HEAD_LENGTH = 20;
    /**
     * 版本需要一致才会处理，默认为0
     */
    private int version;
    /**
     * 指令Id - 组合成一个cmdId
     */
    private short serviceId;
    private short commandId;
    /**
     * 也不知道干嘛的，暂时不用传递
     * 应该是客户端用来做唯一判断的
     */
    private int seq;

    private byte[] body;


    public IMPacket() {
    }

    public IMPacket(short serviceId, short commandId, byte[] body) {
        this.serviceId = serviceId;
        this.commandId = commandId;
        this.body = body;
    }

    public static IMPacket parseFrom(byte[] bytes) {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        var msg = new IMPacket();

        readInt(inputStream);
        msg.setVersion(readInt(inputStream));
        msg.setServiceId(readShort(inputStream));
        msg.setCommandId(readShort(inputStream));
        msg.setSeq(readInt(inputStream));
        readInt(inputStream);
        msg.setBody(inputStream.readAllBytes());

        return msg;
    }

    private static void writeInt(int value, ByteArrayOutputStream out) {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value) & 0xFF);
    }

    private static void writeShort(int value, ByteArrayOutputStream out) {
        out.write((value >> 8) & 0xFF);
        out.write((value) & 0xFF);
    }

    private static int readInt(ByteArrayInputStream in) {

        return ((in.read() & 0xFF) << 24)
                | ((in.read() & 0xFF) << 16)
                | ((in.read() & 0xFF) << 8)
                | (in.read() & 0xFF);

    }

    private static short readShort(ByteArrayInputStream in) {

        return (short) (((in.read() & 0xFF) << 8)
                | (in.read() & 0xFF));
    }

    @Override
    public Short getLogicServiceId() {
        return serviceId;
    }

    public byte[] toByteArray() {

        ByteArrayOutputStream stream =
                new ByteArrayOutputStream(HEAD_LENGTH + body.length);

        writeInt(HEAD_LENGTH, stream);
        writeInt(version, stream);
        writeShort(serviceId, stream);
        writeShort(commandId, stream);
        writeInt(seq, stream);
        writeInt(body.length, stream);

        stream.writeBytes(body);

        return stream.toByteArray();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public short getServiceId() {
        return serviceId;
    }

    public void setServiceId(short serviceId) {
        this.serviceId = serviceId;
    }

    public short getCommandId() {
        return commandId;
    }

    public void setCommandId(short commandId) {
        this.commandId = commandId;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

}
