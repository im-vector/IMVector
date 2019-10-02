package com.imvector.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 在代码中直接获取bean
 *
 * @author: vector.huang
 * @date: 2019/03/30 15:45
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    public static <T> T getBean(String beanName, Class<T> baseType) {
        return applicationContext.getBean(beanName, baseType);
    }

    public static <T> T getBean(Class<T> baseType) {
        return applicationContext.getBean(baseType);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> baseType) {
        return applicationContext.getBeansOfType(baseType);
    }
}
