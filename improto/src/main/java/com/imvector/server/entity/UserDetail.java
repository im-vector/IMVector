package com.imvector.server.entity;

import com.imvector.logic.IMClientPlatform;

import java.util.Objects;

/**
 * @author: vector.huang
 * @date: 2019/10/02 01:57
 */
public class UserDetail implements IMClientPlatform {

    private int userId;
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
