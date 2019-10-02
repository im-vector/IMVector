package com.imvector.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: vector.huang
 * @date: 2019/10/02 03:47
 */
@SpringBootApplication(scanBasePackages = "com.imvector")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
