package com.imvector.server.map.impl;

import com.imvector.server.map.ILoginService;

/**
 * 默认的登录服务
 * @author: vector.huang
 * @date: 2019/10/02 12:03
 */
public class LoginService implements ILoginService {

    @Override
    public int login(String token) {
        try {
            return Integer.parseInt(token);
        } catch (Exception e) {
            return 0;
        }
    }
}
