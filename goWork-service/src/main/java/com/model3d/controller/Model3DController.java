package com.model3d.controller;

import com.model3d.common.Result;
import com.model3d.dto.GenerateRequest;
import com.model3d.dto.TaskResponse;
import com.model3d.service.MeshyService;
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
//@Tag(name = "3D模型生成", description = "3D模型生成相关接口")
public class Model3DController {

    @Autowired
    private MeshyService meshyService;

    /**
     * 文本生成3D模型
     */
    @PostMapping("/text-to-3d")
//    @Operation(summary = "文本生成3D模型", description = "根据文本描述生成3D模型")
    public Result<TaskResponse> textTo3D(@Valid @RequestBody GenerateRequest request) {
        log.info("收到文本生成3D请求: userId={}, prompt={}", request.getUserId(), request.getPrompt());
        
        // 设置输入类型
        request.setInputType("text");
        request.setInputData(request.getPrompt());
        
        TaskResponse response = meshyService.textTo3D(request);
        return Result.success(response);
    }

    /**
     * 图片生成3D模型
     */
    @PostMapping("/image-to-3d")
//    @Operation(summary = "图片生成3D模型", description = "根据图片生成3D模型")
    public Result<TaskResponse> imageTo3D(@Valid @RequestBody GenerateRequest request) {
        log.info("收到图片生成3D请求: userId={}, imageUrl={}", request.getUserId(), request.getInputData());
        
        // 设置输入类型
        request.setInputType("image");
        
        if (request.getInputData() == null || request.getInputData().trim().isEmpty()) {
            return Result.error("图片URL不能为空");
        }
        
        TaskResponse response = meshyService.imageTo3D(request);
        return Result.success(response);
    }

    /**
     * 查询任务状态
     */
//    @GetMapping("/task/{taskId}")
//    @Operation(summary = "查询任务状态", description = "根据任务ID查询3D模型生成状态")
//    public Result<TaskResponse> getTaskStatus(
//            @Parameter(description = "任务ID") @PathVariable Long taskId) {
//        log.info("查询任务状态: taskId={}", taskId);
//
//        TaskResponse response = meshyService.getTaskStatus(taskId);
//        return Result.success(response);
//    }

//    /**
//     * 获取用户的所有任务
//     */
//    @GetMapping("/tasks")
//    @Operation(summary = "获取用户任务列表", description = "获取指定用户的所有3D模型生成任务")
//    public Result<List<TaskResponse>> getUserTasks(
//            @Parameter(description = "用户ID") @RequestParam Long userId) {
//        log.info("获取用户任务列表: userId={}", userId);
//
//        List<TaskResponse> responses = meshyService.getUserTasks(userId);
//        return Result.success(responses);
//    }

    /**
     * 轮询任务状态（用于前端定时查询）
     */
//    @GetMapping("/task/{taskId}/poll")
//    @Operation(summary = "轮询任务状态", description = "轮询查询任务状态，用于前端定时更新")
//    public Result<TaskResponse> pollTaskStatus(
//            @Parameter(description = "任务ID") @PathVariable Long taskId) {
//        log.debug("轮询任务状态: taskId={}", taskId);
//
//        TaskResponse response = meshyService.getTaskStatus(taskId);
//        return Result.success(response);
//    }
}