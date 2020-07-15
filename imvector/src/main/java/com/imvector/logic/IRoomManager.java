package com.imvector.logic;

import com.imvector.proto.IIMPacket;

/**
 * @author: vector.huang
 * @date: 2020/07/15 12:46
 */
public interface IRoomManager<T, P extends IIMPacket> {


    String createRoom(T userDetail, P pocket);

}
