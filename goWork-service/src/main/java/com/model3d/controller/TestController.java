package com.model3d.controller;

import com.model3d.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "3D Model Generation Service");
        data.put("version", "1.0.0");
        return Result.success("服务运行正常", data);
    }

    /**
     * 数据库连接测试
     */
    @GetMapping("/db")
    public Result<String> testDatabase() {
        // 这里后续会添加数据库连接测试逻辑
        return Result.success("数据库连接正常");
    }

    /**
     * Redis连接测试
     */
    @GetMapping("/redis")
    public Result<String> testRedis() {
        // 这里后续会添加Redis连接测试逻辑
        return Result.success("Redis连接正常");
    }
}