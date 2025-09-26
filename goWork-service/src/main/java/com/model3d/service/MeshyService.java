package com.model3d.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.model3d.dto.*;
import com.model3d.entity.ModelTask;
import com.model3d.mapper.ModelTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Meshy AI 3D模型生成服务
 */
@Slf4j
@Service
public class MeshyService {

    @Autowired
    private MeshyApiClient meshyApiClient;

    @Autowired
    private ModelTaskMapper modelTaskMapper;

    /**
     * 文本生成3D模型
     */
    public TaskResponse textTo3D(GenerateRequest request) {
        try {
            // 构建Meshy API请求
            MeshyTextTo3DRequest meshyRequest = new MeshyTextTo3DRequest();
            meshyRequest.setPrompt(request.getPrompt());
            
            // 设置生成模式
            if ("high".equals(request.getQuality())) {
                meshyRequest.setMode("refine"); // 高质量使用refine模式
            } else {
                meshyRequest.setMode("preview"); // 默认预览模式
            }
            
            // 设置艺术风格
            if (request.getStyle() != null && !request.getStyle().trim().isEmpty()) {
                meshyRequest.setArt_style(request.getStyle());
            }
            
            // 设置负面提示词
            if (request.getNegativePrompt() != null && !request.getNegativePrompt().trim().isEmpty()) {
                meshyRequest.setNegative_prompt(request.getNegativePrompt());
            }
            
            // 设置种子值
            if (request.getSeed() != null) {
                meshyRequest.setSeed(request.getSeed());
            }
            
            // 设置拓扑结构
            if (request.getTopology() != null && !request.getTopology().trim().isEmpty()) {
                meshyRequest.setTopology(request.getTopology());
            }
            
            // 设置目标多边形数量
            if (request.getTargetPolycount() != null) {
                meshyRequest.setTarget_polycount(request.getTargetPolycount());
            }

            // 调用Meshy API
            MeshyTaskResponse meshyResponse = meshyApiClient.textTo3D(meshyRequest);

            // 保存任务到数据库
            ModelTask task = new ModelTask();
            task.setUserId(request.getUserId());
            task.setTaskId(meshyResponse.getId());
            task.setPrompt(request.getPrompt());
            task.setInputType("text");
            task.setInputData(request.getPrompt());
            task.setTaskType("text"); // 设置任务类型用于API查询
            task.setStatus(meshyResponse.getStatus());
            task.setCreatedAt(LocalDateTime.now());

            modelTaskMapper.insert(task);

            // 构建响应
            TaskResponse response = new TaskResponse();
            response.setId(task.getId());
            response.setTaskId(meshyResponse.getId());
            response.setPrompt(request.getPrompt());
            response.setInputType("text");
            response.setStatus(meshyResponse.getStatus());
            response.setCreatedAt(task.getCreatedAt());

            log.info("文本生成3D任务创建成功: taskId={}, meshyTaskId={}", task.getId(), meshyResponse.getId());

            return response;

        } catch (Exception e) {
            log.error("文本生成3D失败", e);
            throw new RuntimeException("文本生成3D失败: " + e.getMessage());
        }
    }

    /**
     * 图片生成3D模型
     */
    public TaskResponse imageTo3D(GenerateRequest request) {
        try {
            // 构建Meshy API请求
            MeshyImageTo3DRequest meshyRequest = new MeshyImageTo3DRequest();
            meshyRequest.setImage_url(request.getInputData());
            
            // 设置是否启用原始UV
            if (request.getEnableOriginalUv() != null) {
                meshyRequest.setEnable_original_uv(request.getEnableOriginalUv());
            } else {
                meshyRequest.setEnable_original_uv(true); // 默认启用原始UV
            }
            
            // 设置种子值
            if (request.getSeed() != null) {
                meshyRequest.setSeed(request.getSeed());
            }
            
            // 设置拓扑结构
            if (request.getTopology() != null && !request.getTopology().trim().isEmpty()) {
                meshyRequest.setTopology(request.getTopology());
            }
            
            // 设置目标多边形数量
            if (request.getTargetPolycount() != null) {
                meshyRequest.setTarget_polycount(request.getTargetPolycount());
            }

            // 调用Meshy API
            MeshyTaskResponse meshyResponse = meshyApiClient.imageTo3D(meshyRequest);

            // 保存任务到数据库
            ModelTask task = new ModelTask();
            task.setUserId(request.getUserId());
            task.setTaskId(meshyResponse.getId());
            task.setPrompt(request.getPrompt());
            task.setInputType("image");
            task.setInputData(request.getInputData());
            task.setTaskType("image"); // 设置任务类型用于API查询
            task.setStatus(meshyResponse.getStatus());
            task.setCreatedAt(LocalDateTime.now());

            modelTaskMapper.insert(task);

            // 构建响应
            TaskResponse response = new TaskResponse();
            response.setId(task.getId());
            response.setTaskId(meshyResponse.getId());
            response.setPrompt(request.getPrompt());
            response.setInputType("image");
            response.setStatus(meshyResponse.getStatus());
            response.setCreatedAt(task.getCreatedAt());

            log.info("图片生成3D任务创建成功: taskId={}, meshyTaskId={}", task.getId(), meshyResponse.getId());

            return response;

        } catch (Exception e) {
            log.error("图片生成3D失败", e);
            throw new RuntimeException("图片生成3D失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务状态
     */
    public TaskResponse getTaskStatus(Long taskId) {
        try {
            // 从数据库查询任务
            ModelTask task = modelTaskMapper.selectById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskId);
            }

            // 查询Meshy API任务状态
            MeshyTaskResponse meshyResponse = meshyApiClient.getTaskStatus(task.getTaskId(), task.getTaskType());

            // 更新数据库任务状态
            task.setStatus(meshyResponse.getStatus());
            if ("SUCCEEDED".equals(meshyResponse.getStatus()) && meshyResponse.getResult() != null) {
                task.setModelUrl(meshyResponse.getResult().getModel_url());
                task.setPreviewUrl(meshyResponse.getResult().getThumbnail_url());
                task.setCompletedAt(LocalDateTime.now());
            } else if ("FAILED".equals(meshyResponse.getStatus())) {
                task.setErrorMessage(meshyResponse.getError());
                task.setCompletedAt(LocalDateTime.now());
            }

            modelTaskMapper.updateById(task);

            // 构建响应
            TaskResponse response = new TaskResponse();
            response.setId(task.getId());
            response.setTaskId(task.getTaskId());
            response.setPrompt(task.getPrompt());
            response.setInputType(task.getInputType());
            response.setStatus(task.getStatus());
            response.setModelUrl(task.getModelUrl());
            response.setPreviewUrl(task.getPreviewUrl());
            response.setErrorMessage(task.getErrorMessage());
            response.setCreatedAt(task.getCreatedAt());
            response.setCompletedAt(task.getCompletedAt());

            log.info("任务状态查询成功: taskId={}, status={}", taskId, task.getStatus());

            return response;

        } catch (Exception e) {
            log.error("查询任务状态失败: taskId={}", taskId, e);
            throw new RuntimeException("查询任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有任务
     */
    public java.util.List<TaskResponse> getUserTasks(Long userId) {
        try {
            QueryWrapper<ModelTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .orderByDesc("created_at");

            java.util.List<ModelTask> tasks = modelTaskMapper.selectList(queryWrapper);

            return tasks.stream().map(task -> {
                TaskResponse response = new TaskResponse();
                response.setId(task.getId());
                response.setTaskId(task.getTaskId());
                response.setPrompt(task.getPrompt());
                response.setInputType(task.getInputType());
                response.setStatus(task.getStatus());
                response.setModelUrl(task.getModelUrl());
                response.setPreviewUrl(task.getPreviewUrl());
                response.setErrorMessage(task.getErrorMessage());
                response.setCreatedAt(task.getCreatedAt());
                response.setCompletedAt(task.getCompletedAt());
                return response;
            }).collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            log.error("获取用户任务失败: userId={}", userId, e);
            throw new RuntimeException("获取用户任务失败: " + e.getMessage());
        }
    }
}