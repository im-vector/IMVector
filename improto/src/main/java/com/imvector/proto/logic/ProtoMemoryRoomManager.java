package com.imvector.proto.logic;

import com.imvector.logic.IRoomManager;
import com.imvector.proto.entity.UserDetail;
import com.imvector.proto.impl.IMPacket;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: vector.huang
 * @date: 2020/07/15 12:54
 */
@Component("protoMemoryRoomManager")
@ConditionalOnMissingBean(name = {"customRoomManager"})
public class ProtoMemoryRoomManager implements IRoomManager<UserDetail, IMPacket> {

    private Map<String, List<UserDetail>> rooms = new HashMap<>();

    @Override
    public String createRoom(UserDetail userDetail, IMPacket pocket) {
        var roomNo = RandomStringUtils.randomAlphabetic(32);
        var users = new ArrayList<UserDetail>(10);
        users.add(userDetail);
        rooms.put(roomNo,users);

        return roomNo;
    }
}
