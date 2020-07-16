package com.imvector.logic;

import com.imvector.proto.IIMPacket;
import com.imvector.vo.RoomVo;

import java.util.Set;

/**
 * @author: vector.huang
 * @date: 2020/07/15 12:46
 */
public interface IRoomManager<T, P extends IIMPacket> {

    RoomVo createRoom(T userDetail, P pocket);

    RoomVo joinRoom(T userDetail, P pocket);

    Set<T> getUsers(int to);
}
