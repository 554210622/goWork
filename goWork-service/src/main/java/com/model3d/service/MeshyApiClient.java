package com.model3d.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.model3d.config.MeshyConfig;
import com.model3d.dto.MeshyImageTo3DRequest;
import com.model3d.dto.MeshyTaskResponse;
import com.model3d.dto.MeshyTextTo3DRequest;
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
 * Meshy AI API客户端
 */
@Slf4j
@Component
public class MeshyApiClient {

    @Autowired
    private MeshyConfig meshyConfig;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MeshyApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 文本生成3D模型
     * API端点: POST /v2beta/text-to-3d
     */
    public MeshyTaskResponse textTo3D(MeshyTextTo3DRequest request) throws IOException, InterruptedException {
        String url = meshyConfig.getBaseUrl() + "/text-to-3d";
        String requestBody = objectMapper.writeValueAsString(request);
        
        log.info("发起文本生成3D请求到: {}", url);
        log.info("请求体: {}", requestBody);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + meshyConfig.getKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(meshyConfig.getTimeout()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        log.info("文本生成3D响应状态: {}, 内容: {}", response.statusCode(), response.body());
        
        if (response.statusCode() == 201) {
            // 201 Created 表示任务创建成功
            return objectMapper.readValue(response.body(), MeshyTaskResponse.class);
        } else if (response.statusCode() >= 400) {
            log.error("Meshy API调用失败: 状态码={}, 响应={}", response.statusCode(), response.body());
            throw new RuntimeException("Meshy API调用失败: " + response.statusCode() + " - " + response.body());
        } else {
            return objectMapper.readValue(response.body(), MeshyTaskResponse.class);
        }
    }

    /**
     * 图片生成3D模型
     * API端点: POST /v2beta/image-to-3d
     */
    public MeshyTaskResponse imageTo3D(MeshyImageTo3DRequest request) throws IOException, InterruptedException {
        String url = meshyConfig.getBaseUrl() + "/image-to-3d";
        String requestBody = objectMapper.writeValueAsString(request);
        
        log.info("发起图片生成3D请求到: {}", url);
        log.info("请求体: {}", requestBody);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + meshyConfig.getKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(meshyConfig.getTimeout()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        log.info("图片生成3D响应状态: {}, 内容: {}", response.statusCode(), response.body());
        
        if (response.statusCode() == 201) {
            // 201 Created 表示任务创建成功
            return objectMapper.readValue(response.body(), MeshyTaskResponse.class);
        } else if (response.statusCode() >= 400) {
            log.error("Meshy API调用失败: 状态码={}, 响应={}", response.statusCode(), response.body());
            throw new RuntimeException("Meshy API调用失败: " + response.statusCode() + " - " + response.body());
        } else {
            return objectMapper.readValue(response.body(), MeshyTaskResponse.class);
        }
    }

    /**
     * 查询任务状态
     * API端点: GET /v2beta/text-to-3d/{id} 或 GET /v2beta/image-to-3d/{id}
     */
    public MeshyTaskResponse getTaskStatus(String taskId, String taskType) throws IOException, InterruptedException {
        // 根据任务类型构建不同的URL
        String endpoint = taskType.equals("text") ? "/text-to-3d/" : "/image-to-3d/";
        String url = meshyConfig.getBaseUrl() + endpoint + taskId;
        
        log.info("查询任务状态: taskId={}, taskType={}, url={}", taskId, taskType, url);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + meshyConfig.getKey())
                .GET()
                .timeout(Duration.ofMillis(meshyConfig.getTimeout()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        log.info("任务状态查询响应: 状态码={}, 内容={}", response.statusCode(), response.body());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), MeshyTaskResponse.class);
        } else if (response.statusCode() >= 400) {
            log.error("任务状态查询失败: 状态码={}, 响应={}", response.statusCode(), response.body());
            throw new RuntimeException("任务状态查询失败: " + response.statusCode() + " - " + response.body());
        } else {
            return objectMapper.readValue(response.body(), MeshyTaskResponse.class);
        }
    }
}