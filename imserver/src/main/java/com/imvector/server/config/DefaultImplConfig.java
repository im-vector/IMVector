package com.imvector.server.config;

import com.imvector.server.map.ILoginService;
import com.imvector.server.map.impl.LoginService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: vector.huang
 * @date: 2019/10/02 04:00
 */
@Configuration
public class DefaultImplConfig {

    /**
     * 登录服务默认实现
     *
     * @return 默认实现
     */
    @ConditionalOnMissingBean(LoginService.class)
    @Bean("defaultIMProtocolCodec")
    public ILoginService loginService() {
        return new LoginService();
    }

}
