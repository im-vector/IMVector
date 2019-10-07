package com.imvector.proto.map.impl;

import com.imvector.proto.impl.IMPacket;
import com.imvector.proto.map.ILoginService;
import com.imvector.proto.Packet;
import com.imvector.proto.system.IMSystem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 默认的登录服务
 *
 * @author: vector.huang
 * @date: 2019/10/02 12:03
 */
@Component("loginService")
@ConditionalOnMissingBean(name = "customLoginService")
public class LoginService implements ILoginService {

    private int toUserId(String token) {
        try {
            return Integer.parseInt(token);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public IMSystem.LoginResp login(IMPacket packet) throws Exception {

        var resp = IMSystem.LoginResp.newBuilder();
        // IMClient 平台判断
        int imClientPlatform = packet.getSeq();
        if(imClientPlatform < 0 || imClientPlatform > 15){
            resp.setStatus(Packet.Status.ERR_CLIENT);
            resp.setMsg("不支持的客户端");
            return resp.build();
        }

        var login = IMSystem.LoginReq.parseFrom(packet.getBody());

        String token = login.getToken();
        int userId = toUserId(token);
        if (userId < 0) {
            resp.setStatus(Packet.Status.ERR_CLIENT);
        } else {
            resp.setStatus(Packet.Status.OK);
            // 无存储，重启就是0 开始
            resp.setMaxSeq(0);
            resp.setUserId(userId);
        }

        return resp.build();
    }
}
