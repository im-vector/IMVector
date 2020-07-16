package com.imvector.proto.logic;

import com.google.protobuf.InvalidProtocolBufferException;
import com.imvector.logic.IRoomManager;
import com.imvector.proto.chat.room.Room;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.impl.IMPacket;
import com.imvector.vo.RoomVo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: vector.huang
 * @date: 2020/07/15 12:54
 */
@Component("protoMemoryRoomManager")
@ConditionalOnMissingBean(name = {"customRoomManager"})
public class ProtoMemoryRoomManager implements IRoomManager<UserDetail, IMPacket> {

    private Map<Integer, Set<UserDetail>> rooms = new HashMap<>();
    private Map<String, Integer> roomIds = new HashMap<>();

    @Override
    public RoomVo createRoom(UserDetail userDetail, IMPacket pocket) {
        var roomNo = RandomStringUtils.randomAlphabetic(32);
        var roomId = RandomUtils.nextInt();
        if (roomIds.containsKey(roomNo)) {
            return null;
        }
        if (rooms.containsKey(roomId)) {
            return null;
        }
        var users = new HashSet<UserDetail>(10);
        users.add(userDetail);
        rooms.put(roomId, users);
        roomIds.put(roomNo, roomId);

        var room = new RoomVo();
        room.setRoomId(roomId);
        room.setRoomNo(roomNo);

        return room;
    }

    @Override
    public RoomVo joinRoom(UserDetail userDetail, IMPacket pocket) {
        Room.JoinRoomReq request = null;
        try {
            request = Room.JoinRoomReq.parseFrom(pocket.getBody());
        } catch (InvalidProtocolBufferException e) {
            return null;
        }

        var roomNo = request.getRoomNo();
        var roomId = roomIds.get(roomNo);
        var users = rooms.get(roomId);
        if (users == null) {
            return null;
        }
        users.add(userDetail);

        var room = new RoomVo();
        room.setRoomId(roomId);
        room.setRoomNo(roomNo);

        return room;
    }

    @Override
    public Set<UserDetail> getUsers(int to) {
        return rooms.get(to);
    }
}
