package com.imvector.server.config;

import com.imvector.logic.IMessageManager;
import com.imvector.logic.impl.MemoryMessageManager;
import com.imvector.server.logic.ProtoMemoryMessageManager;
import com.imvector.server.map.ILoginService;
import com.imvector.server.map.impl.LoginService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author: vector.huang
 * @date: 2019/10/02 04:00
 */
@Configuration
public class ServerDefaultImplConfig {


//    @ConditionalOnMissingBean(IMessageManager.class)
//    @Bean
//    @Order(Integer.MAX_VALUE - 1000)
//    public IMessageManager iMessageManager() {
//        return new ProtoMemoryMessageManager();
//    }

    /**
     * 登录服务默认实现
     *
     * @return 默认实现
     */
    @ConditionalOnMissingBean(LoginService.class)
    @Bean
    public ILoginService loginService() {
        return new LoginService();
    }

}
