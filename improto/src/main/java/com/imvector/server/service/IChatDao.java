package com.imvector.server.service;

import com.imvector.server.proto.chat.Chat;

/**
 * @author: vector.huang
 * @date: 2019/10/07 11:35
 */
public interface IChatDao {


    /**
     * 保存一条消息
     * @param seq 系列码，用于判断是否重复保存过
     * @param msgReq 消息
     * @return 是否重复
     */
    boolean save(int seq, Chat.MsgReq msgReq);
}
