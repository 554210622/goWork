package com.model3d;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 3D模型生成服务启动类
 */
@SpringBootApplication
@MapperScan("com.model3d.mapper")
@EnableAsync
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("3D Model Generation 服务启动成功！");
        System.out.println("Health Check: http://localhost:8080/actuator/health");
    }
}