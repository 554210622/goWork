package com.model3d.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.model3d.config.TripoConfig;
import com.model3d.dto.TripoImageToModelRequest;
import com.model3d.dto.TripoTaskResponse;
import com.model3d.dto.TripoTextToModelRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Tripo AI API客户端
 */
@Slf4j
@Component
public class TripoApiClient {

    @Autowired
    private TripoConfig tripoConfig;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TripoApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 文本生成3D模型
     * API端点: POST /task
     */
    public TripoTaskResponse textToModel(TripoTextToModelRequest request) throws IOException, InterruptedException {
        String url = tripoConfig.getBaseUrl() + "/task";
        String requestBody = objectMapper.writeValueAsString(request);
        
        log.info("发起文本生成3D请求到: {}", url);
        log.info("请求体: {}", requestBody);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + tripoConfig.getKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(tripoConfig.getTimeout()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        log.info("文本生成3D响应状态: {}, 内容: {}", response.statusCode(), response.body());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), TripoTaskResponse.class);
        } else {
            log.error("Tripo API调用失败: 状态码={}, 响应={}", response.statusCode(), response.body());
            throw new RuntimeException("Tripo API调用失败: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * 图像生成3D模型
     * API端点: POST /task
     */
    public TripoTaskResponse imageToModel(TripoImageToModelRequest request) throws IOException, InterruptedException {
        String url = tripoConfig.getBaseUrl() + "/task";
        String requestBody = objectMapper.writeValueAsString(request);
        
        log.info("发起图像生成3D请求到: {}", url);
        log.info("请求体: {}", requestBody);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + tripoConfig.getKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(tripoConfig.getTimeout()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        log.info("图像生成3D响应状态: {}, 内容: {}", response.statusCode(), response.body());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), TripoTaskResponse.class);
        } else {
            log.error("Tripo API调用失败: 状态码={}, 响应={}", response.statusCode(), response.body());
            throw new RuntimeException("Tripo API调用失败: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * 查询任务状态
     * API端点: GET /task/{task_id}
     */
    public TripoTaskResponse getTaskStatus(String taskId) throws IOException, InterruptedException {
        String url = tripoConfig.getBaseUrl() + "/task/" + taskId;
        
        log.info("查询任务状态: taskId={}, url={}", taskId, url);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + tripoConfig.getKey())
                .GET()
                .timeout(Duration.ofMillis(tripoConfig.getTimeout()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        log.info("任务状态查询响应: 状态码={}, 内容={}", response.statusCode(), response.body());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), TripoTaskResponse.class);
        } else {
            log.error("任务状态查询失败: 状态码={}, 响应={}", response.statusCode(), response.body());
            throw new RuntimeException("任务状态查询失败: " + response.statusCode() + " - " + response.body());
        }
    }
}