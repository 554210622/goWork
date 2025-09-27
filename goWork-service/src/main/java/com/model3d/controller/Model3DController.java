package com.model3d.controller;

import com.model3d.common.Result;
import com.model3d.dto.GenerateRequest;
import com.model3d.dto.TaskResponse;
import com.model3d.service.TripoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 3D模型生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/model3d")
@Tag(name = "3D模型生成", description = "3D模型生成相关接口")
public class Model3DController {

    @Autowired
    private TripoService tripoService;
    /**
     * 文本生成3D模型
     */
    @PostMapping("/text-to-3d")
    @Operation(summary = "文本生成3D模型", description = "根据文本描述生成3D模型")
    public Result<TaskResponse> textTo3D(@Valid @RequestBody GenerateRequest request) {
        log.info("收到文本生成3D请求: userId={}, prompt={}", request.getUserId(), request.getPrompt());
        
        // 设置输入类型
        request.setInputType("text");
        request.setInputData(request.getPrompt());
        
        TaskResponse response = tripoService.textTo3D(request);
        return Result.success(response);
    }

    /**
     * 图片生成3D模型
     */
    @PostMapping("/image-to-3d")
    @Operation(summary = "图片生成3D模型", description = "根据图片生成3D模型")
    public Result<TaskResponse> imageTo3D(@Valid @RequestBody GenerateRequest request) {
        log.info("收到图片生成3D请求: userId={}, imageUrl={}", request.getUserId(), request.getInputData());
        
        // 设置输入类型
        request.setInputType("image");
        
        if (request.getInputData() == null || request.getInputData().trim().isEmpty()) {
            return Result.error("图片URL不能为空");
        }
        
        TaskResponse response = tripoService.imageTo3D(request);
        return Result.success(response);
    }

    /**
     * 查询任务状态 - 使用本地任务ID
     */
    @GetMapping("/task/{localTaskId}")
    @Operation(summary = "查询任务状态", description = "根据本地任务ID查询3D模型生成状态")
    public Result<TaskResponse> getTaskStatus(
            @Parameter(description = "本地任务ID") @PathVariable Long localTaskId) {
        log.info("查询任务状态: localTaskId={}", localTaskId);

        TaskResponse response = tripoService.getTaskStatus(localTaskId);
        return Result.success(response);
    }

    /**
     * 获取用户的所有任务
     */
    @GetMapping("/tasks")
    @Operation(summary = "获取用户任务列表", description = "获取指定用户的所有3D模型生成任务")
    public Result<List<TaskResponse>> getUserTasks(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.info("获取用户任务列表: userId={}", userId);

        List<TaskResponse> responses = tripoService.getUserTasks(userId);
        return Result.success(responses);
    }

    /**
     * 轮询任务状态（用于前端定时查询）- 使用本地任务ID
     */
    @GetMapping("/task/{localTaskId}/poll")
    @Operation(summary = "轮询任务状态", description = "轮询查询任务状态，用于前端定时更新")
    public Result<TaskResponse> pollTaskStatus(
            @Parameter(description = "本地任务ID") @PathVariable Long localTaskId) {
        log.debug("轮询任务状态: localTaskId={}", localTaskId);

        TaskResponse response = tripoService.getTaskStatus(localTaskId);
        return Result.success(response);
    }

    /**
     * 根据Tripo任务ID查询状态（备用接口）
     */
    @GetMapping("/tripo-task/{tripoTaskId}")
    @Operation(summary = "根据Tripo任务ID查询状态", description = "使用Tripo的原始任务ID查询状态")
    public Result<TaskResponse> getTaskStatusByTripoId(
            @Parameter(description = "Tripo任务ID") @PathVariable String tripoTaskId) {
        log.info("根据Tripo任务ID查询状态: tripoTaskId={}", tripoTaskId);

        TaskResponse response = tripoService.getTaskStatusByTripoId(tripoTaskId);
        return Result.success(response);
    }
}