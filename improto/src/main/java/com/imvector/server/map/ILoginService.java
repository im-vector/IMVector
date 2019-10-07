package com.imvector.server.map;

import com.imvector.proto.impl.IMPacket;
import com.imvector.server.proto.system.IMSystem;

/**
 * @author: vector.huang
 * @date: 2019/10/02 11:59
 */
public interface ILoginService {

    IMSystem.LoginResp login(IMPacket packet) throws Exception;

}
