package com.imvector.proto.entity;

import com.imvector.logic.IMClientPlatform;

import java.util.Objects;

/**
 * @author: vector.huang
 * @date: 2019/10/02 01:57
 */
public class UserDetail implements IMClientPlatform {

    /**
     * 用户ID
     */
    private int userId;
    /**
     * 终端系列好，不同终端支持同时登录
     */
    private int platformSeq;
    /**
     * 使用的客户端版本
     */
    private int version;

    public UserDetail() {
    }

    /**
     * 快速构建，用于发送消息
     * @param userId 用户Id
     */
    public UserDetail(int userId) {
        this.userId = userId;
    }

    @Override
    public int getPlatformSeq() {
        return platformSeq;
    }

    public void setPlatformSeq(int platformSeq) {
        this.platformSeq = platformSeq;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * 用户ID 一样就认为相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetail)) return false;
        UserDetail that = (UserDetail) o;
        return getUserId() == that.getUserId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }
}
