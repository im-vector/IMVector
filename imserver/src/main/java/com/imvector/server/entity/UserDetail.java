package com.imvector.server.entity;

/**
 * @author: vector.huang
 * @date: 2019/10/02 01:57
 */
public class UserDetail {

    private int userId;
    /**
     * 使用的客户端版本
     */
    private int version;

    public UserDetail() {
    }

    public UserDetail(int userId) {
        this.userId = userId;
    }

    public UserDetail(int userId, int version) {
        this.userId = userId;
        this.version = version;
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
}
