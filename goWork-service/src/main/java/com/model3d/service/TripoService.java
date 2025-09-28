package com.model3d.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.model3d.dto.*;
import com.model3d.entity.ModelTask;
import com.model3d.mapper.ModelTaskMapper;
import com.model3d.service.FileTransferService;
import com.model3d.service.OssService;
import com.model3d.service.TripoApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Tripo AI 3D模型生成服务
 */
@Slf4j
@Service
public class TripoService {

    @Autowired
    private TripoApiClient tripoApiClient;

    @Autowired
    private ModelTaskMapper modelTaskMapper;

    @Autowired
    private FileTransferService fileTransferService;

    @Autowired
    private OssService ossService;

    /**
     * 文本生成3D模型 - 异步实现
     */
    public TaskResponse textTo3D(GenerateRequest request) {
        try {
            // 1. 立即创建任务记录（状态：PENDING）
            ModelTask task = new ModelTask();
            task.setUserId(request.getUserId());
            task.setPrompt(request.getPrompt());
            task.setInputType("text");
            task.setInputData(request.getPrompt());
            task.setTaskType("text");
            task.setGenerationStage("single"); // Tripo是单阶段生成
            task.setStatus("PENDING"); // 初始状态：等待处理
            task.setCreatedAt(LocalDateTime.now());

            modelTaskMapper.insert(task);

            // 2. 异步调用Tripo API（不阻塞当前线程）
            processTextTo3DAsync(task.getId(), request);

            // 3. 立即返回任务信息（不等待Tripo API响应）
            TaskResponse response = new TaskResponse();
            response.setId(task.getId());
            response.setTaskId(null); // Tripo任务ID稍后异步获取
            response.setPrompt(request.getPrompt());
            response.setInputType("text");
            response.setStatus("PENDING");
            response.setCreatedAt(task.getCreatedAt());

            log.info("文本生成3D任务创建成功（异步）: taskId={}", task.getId());

            return response;

        } catch (Exception e) {
            log.error("文本生成3D任务创建失败", e);
            throw new RuntimeException("文本生成3D任务创建失败: " + e.getMessage());
        }
    }

    /**
     * 异步处理文本生成3D - 在后台线程执行
     */
    @Async("model3dTaskExecutor")
    public CompletableFuture<Void> processTextTo3DAsync(Long taskId, GenerateRequest request) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("开始异步处理文本生成3D: taskId={}", taskId);

                // 更新状态为处理中
                ModelTask task = modelTaskMapper.selectById(taskId);
                task.setStatus("IN_PROGRESS");
                modelTaskMapper.updateById(task);

                // 构建Tripo API请求
                TripoTextToModelRequest tripoRequest = buildTextToModelRequest(request);
                TripoTaskResponse tripoResponse = tripoApiClient.textToModel(tripoRequest);

                // 检查响应
                if (tripoResponse.getCode() != 0) {
                    throw new RuntimeException("Tripo API返回错误: " + tripoResponse.getMessage());
                }

                // 更新任务信息
                task.setTaskId(tripoResponse.getData().getTaskId());
                task.setStatus("RUNNING");
                modelTaskMapper.updateById(task);

                log.info("文本生成3D任务提交成功: taskId={}, tripoTaskId={}",
                        taskId, tripoResponse.getData().getTaskId());

                // 等待任务完成
                waitForTaskCompletion(tripoResponse.getData().getTaskId(), taskId);

            } catch (Exception e) {
                log.error("文本生成3D异步处理失败: taskId={}", taskId, e);

                // 更新任务状态为失败
                ModelTask task = modelTaskMapper.selectById(taskId);
                task.setStatus("FAILED");
                task.setErrorMessage("Tripo API调用失败: " + e.getMessage());
                task.setCompletedAt(LocalDateTime.now());
                modelTaskMapper.updateById(task);
            }
        });
    }

    /**
     * 构建文本生成3D模型请求参数
     */
    private TripoTextToModelRequest buildTextToModelRequest(GenerateRequest request) {
        TripoTextToModelRequest tripoRequest = new TripoTextToModelRequest();

        // 必需参数
        tripoRequest.setPrompt(request.getPrompt());

        // 模型版本 (优先使用用户指定的版本)
        String modelVersion = request.getModelVersion();
        if (modelVersion == null || modelVersion.trim().isEmpty()) {
            modelVersion = "v2.5-20250123"; // 默认版本
        }
        tripoRequest.setModelVersion(modelVersion);

        // 可选参数
        if (request.getNegativePrompt() != null && !request.getNegativePrompt().trim().isEmpty()) {
            tripoRequest.setNegativePrompt(request.getNegativePrompt());
        }

        // 种子参数 (优先使用具体的种子，其次使用通用种子)
        if (request.getImageSeed() != null) {
            tripoRequest.setImageSeed(request.getImageSeed());
        } else if (request.getSeed() != null) {
            tripoRequest.setImageSeed(request.getSeed());
        }

        if (request.getModelSeed() != null) {
            tripoRequest.setModelSeed(request.getModelSeed());
        } else if (request.getSeed() != null) {
            tripoRequest.setModelSeed(request.getSeed());
        }

        if (request.getTextureSeed() != null) {
            tripoRequest.setTextureSeed(request.getTextureSeed());
        }

        // 风格参数
        if (request.getStyle() != null && !request.getStyle().trim().isEmpty()) {
            tripoRequest.setStyle(request.getStyle());
        }

        // 面数限制 (优先使用faceLimit，其次使用targetPolycount)
        if (request.getFaceLimit() != null) {
            tripoRequest.setFaceLimit(request.getFaceLimit());
        } else if (request.getTargetPolycount() != null) {
            tripoRequest.setFaceLimit(request.getTargetPolycount());
        }

        // 纹理和材质设置
        tripoRequest.setTexture(request.getTexture() != null ? request.getTexture() : true);
        tripoRequest.setPbr(request.getPbr() != null ? request.getPbr() : true);

        // 纹理质量
        String textureQuality = request.getTextureQuality();
        if (textureQuality == null || textureQuality.trim().isEmpty()) {
            textureQuality = "standard"; // 默认质量
        }
        tripoRequest.setTextureQuality(textureQuality);

        // 其他高级选项
        tripoRequest.setAutoSize(request.getAutoSize() != null ? request.getAutoSize() : false);
        tripoRequest.setQuad(request.getQuad() != null ? request.getQuad() : false);
        tripoRequest.setSmartLowPoly(request.getSmartLowPoly() != null ? request.getSmartLowPoly() : false);
        tripoRequest.setGenerateParts(request.getGenerateParts() != null ? request.getGenerateParts() : false);

        // 压缩选项
        if (request.getCompress() != null && !request.getCompress().trim().isEmpty()) {
            tripoRequest.setCompress(request.getCompress());
        }

        // 几何质量
        if (request.getGeometryQuality() != null && !request.getGeometryQuality().trim().isEmpty()) {
            tripoRequest.setGeometryQuality(request.getGeometryQuality());
        }

        return tripoRequest;
    }

    /**
     * 等待任务完成
     */
    private void waitForTaskCompletion(String tripoTaskId, Long localTaskId) throws Exception {
        int maxAttempts = 60; // 最多等待10分钟（每10秒检查一次）
        int attempt = 0;

        while (attempt < maxAttempts) {
            Thread.sleep(10000); // 等待10秒
            attempt++;

            TripoTaskResponse response = tripoApiClient.getTaskStatus(tripoTaskId);

            if (response.getCode() != 0) {
                throw new RuntimeException("查询任务状态失败: " + response.getMessage());
            }

            String status = response.getData().getStatus();
            log.info("等待任务完成: tripoTaskId={}, attempt={}, status={}", tripoTaskId, attempt, status);

            if ("success".equals(status)) {
                // 更新任务为转存中状态
                ModelTask task = safeGetTaskById(localTaskId);
                if (task == null) {
                    log.error("无法获取任务信息，可能是数据库连接问题: taskId={}", localTaskId);
                    return;
                }

                task.setStatus("TRANSFERRING"); // 新增状态：正在转存到OSS
                task.setCompletedAt(LocalDateTime.now());
                safeUpdateTask(task);

                String modelUrl = null, baseModelUrl = null, pbrModelUrl = null;
                String previewUrl = null, textureImageUrl = null, normalMapUrl = null;
                String metallicMapUrl = null, roughnessMapUrl = null;

                if (response.getData().getOutput() != null) {
                    TripoTaskResponse.TripoOutput output = response.getData().getOutput();

                    // 获取Tripo的临时URL
                    modelUrl = output.getModel();
                    baseModelUrl = output.getBaseModel();
                    pbrModelUrl = output.getPbrModel();
                    textureImageUrl = output.getTextureImage();
                    normalMapUrl = output.getNormalMap();
                    metallicMapUrl = output.getMetallicMap();
                    roughnessMapUrl = output.getRoughnessMap();
                    previewUrl = output.getRenderedImage();
                }

                log.info("任务完成: tripoTaskId={}, 开始同步转存到OSS", tripoTaskId);
                log.info("Tripo返回的文件URL: modelUrl={}, pbrModelUrl={}, previewUrl={}",
                    modelUrl, pbrModelUrl, previewUrl);

                // 同步转存文件到OSS，确保完成后再更新数据库
                transferFilesToOssSync(task, modelUrl, baseModelUrl, pbrModelUrl,
                    previewUrl, textureImageUrl, normalMapUrl,
                    metallicMapUrl, roughnessMapUrl);

                return;
            } else if ("failed".equals(status) || "banned".equals(status) || "expired".equals(status)) {
                throw new RuntimeException("Tripo任务失败，状态: " + status);
            }
            // 继续等待其他状态（queued, running）
        }

        throw new RuntimeException("任务超时: tripoTaskId=" + tripoTaskId);
    }

    /**
     * 同步转存文件到OSS - 确保完成后再更新数据库
     */
    private void transferFilesToOssSync(ModelTask task, String modelUrl, String baseModelUrl,
                                      String pbrModelUrl, String previewUrl, String textureImageUrl,
                                      String normalMapUrl, String metallicMapUrl, String roughnessMapUrl) {
        try {
            Long taskId = task.getId();
            log.info("开始同步转存任务文件到OSS: taskId={}, tripoTaskId={}", taskId, task.getTaskId());
            log.info("转存文件列表: modelUrl={}, pbrModelUrl={}, previewUrl={}",
                modelUrl, pbrModelUrl, previewUrl);

            // 转存主模型文件 (GLB格式) - 最重要的文件
            if (modelUrl != null && !modelUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(modelUrl, null, "model");
                    task.setModelUrl(ossUrl);
                    log.info("主模型文件转存成功: taskId={}, ossUrl={}", taskId, ossUrl);
                } catch (Exception e) {
                    log.error("主模型文件转存失败: taskId={}, sourceUrl={}", taskId, modelUrl, e);
                    task.setModelUrl(modelUrl); // 转存失败时保留原URL
                }
            }

            // 转存预览图 - 第二重要
            if (previewUrl != null && !previewUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(previewUrl, null, "preview");
                    task.setPreviewUrl(ossUrl);
                    log.info("预览图转存成功: taskId={}", taskId);
                } catch (Exception e) {
                    log.error("预览图转存失败: taskId={}, sourceUrl={}", taskId, previewUrl, e);
                    task.setPreviewUrl(previewUrl);
                }
            }

            // 转存基础模型文件
            if (baseModelUrl != null && !baseModelUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(baseModelUrl, null, "base_model");
                    task.setBaseModelUrl(ossUrl);
                    log.info("基础模型文件转存成功: taskId={}", taskId);
                } catch (Exception e) {
                    log.error("基础模型文件转存失败: taskId={}, sourceUrl={}", taskId, baseModelUrl, e);
                    task.setBaseModelUrl(baseModelUrl);
                }
            }

            // 转存PBR模型文件
            if (pbrModelUrl != null && !pbrModelUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(pbrModelUrl, null, "pbr_model");
                    task.setPbrModelUrl(ossUrl);
                    log.info("PBR模型文件转存成功: taskId={}", taskId);
                } catch (Exception e) {
                    log.error("PBR模型文件转存失败: taskId={}, sourceUrl={}", taskId, pbrModelUrl, e);
                    task.setPbrModelUrl(pbrModelUrl);
                }
            }

            // 转存纹理图像
            if (textureImageUrl != null && !textureImageUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(textureImageUrl, null, "texture");
                    task.setTextureImageUrl(ossUrl);
                    log.info("纹理图像转存成功: taskId={}", taskId);
                } catch (Exception e) {
                    log.error("纹理图像转存失败: taskId={}, sourceUrl={}", taskId, textureImageUrl, e);
                    task.setTextureImageUrl(textureImageUrl);
                }
            }

            // 转存法线贴图
            if (normalMapUrl != null && !normalMapUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(normalMapUrl, null, "normal_map");
                    task.setNormalMapUrl(ossUrl);
                    log.info("法线贴图转存成功: taskId={}", taskId);
                } catch (Exception e) {
                    log.error("法线贴图转存失败: taskId={}, sourceUrl={}", taskId, normalMapUrl, e);
                    task.setNormalMapUrl(normalMapUrl);
                }
            }

            // 转存金属度贴图
            if (metallicMapUrl != null && !metallicMapUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(metallicMapUrl, null, "metallic_map");
                    task.setMetallicMapUrl(ossUrl);
                    log.info("金属度贴图转存成功: taskId={}", taskId);
                } catch (Exception e) {
                    log.error("金属度贴图转存失败: taskId={}, sourceUrl={}", taskId, metallicMapUrl, e);
                    task.setMetallicMapUrl(metallicMapUrl);
                }
            }

            // 转存粗糙度贴图
            if (roughnessMapUrl != null && !roughnessMapUrl.trim().isEmpty()) {
                try {
                    String ossUrl = ossService.uploadFromUrl(roughnessMapUrl, null, "roughness_map");
                    task.setRoughnessMapUrl(ossUrl);
                    log.info("粗糙度贴图转存成功: taskId={}", taskId);
                } catch (Exception e) {
                    log.error("粗糙度贴图转存失败: taskId={}, sourceUrl={}", taskId, roughnessMapUrl, e);
                    task.setRoughnessMapUrl(roughnessMapUrl);
                }
            }

            // 所有文件转存完成后，更新任务状态为SUCCESS
            task.setStatus("SUCCESS");
            if (safeUpdateTask(task)) {
                log.info("任务文件转存完成，状态已更新为SUCCESS: taskId={}", taskId);
            } else {
                log.error("任务状态更新失败: taskId={}", taskId);
            }

        } catch (Exception e) {
            log.error("同步转存任务文件到OSS失败: taskId={}", task.getId(), e);

            // 转存失败时，将任务状态设为失败
            task.setStatus("FAILED");
            task.setErrorMessage("文件转存到OSS失败: " + e.getMessage());
            safeUpdateTask(task);
        }
    }

    /**
     * 图片生成3D模型 - 异步实现
     */
    public TaskResponse imageTo3D(GenerateRequest request) {
        try {
            // 1. 立即创建任务记录（状态：PENDING）
            ModelTask task = new ModelTask();
            task.setUserId(request.getUserId());
            task.setPrompt(request.getPrompt());
            task.setInputType("image");
            task.setInputData(request.getInputData());
            task.setTaskType("image");
            task.setGenerationStage("single"); // 图片生成是单阶段
            task.setStatus("PENDING"); // 初始状态：等待处理
            task.setCreatedAt(LocalDateTime.now());

            modelTaskMapper.insert(task);

            // 2. 异步调用Tripo API（不阻塞当前线程）
            processImageTo3DAsync(task.getId(), request);

            // 3. 立即返回任务信息（不等待Tripo API响应）
            TaskResponse response = new TaskResponse();
            response.setId(task.getId());
            response.setTaskId(null); // Tripo任务ID稍后异步获取
            response.setPrompt(request.getPrompt());
            response.setInputType("image");
            response.setStatus("PENDING");
            response.setCreatedAt(task.getCreatedAt());

            log.info("图片生成3D任务创建成功（异步）: taskId={}", task.getId());

            return response;

        } catch (Exception e) {
            log.error("图片生成3D任务创建失败", e);
            throw new RuntimeException("图片生成3D任务创建失败: " + e.getMessage());
        }
    }

    /**
     * 异步处理图片生成3D - 在后台线程执行
     */
    @Async("model3dTaskExecutor")
    public CompletableFuture<Void> processImageTo3DAsync(Long taskId, GenerateRequest request) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("开始异步处理图片生成3D: taskId={}", taskId);

                // 更新状态为处理中
                ModelTask task = modelTaskMapper.selectById(taskId);
                task.setStatus("IN_PROGRESS");
                modelTaskMapper.updateById(task);

                // 构建Tripo API请求
                TripoImageToModelRequest tripoRequest = buildImageToModelRequest(request);
                TripoTaskResponse tripoResponse = tripoApiClient.imageToModel(tripoRequest);

                // 检查响应
                if (tripoResponse.getCode() != 0) {
                    throw new RuntimeException("Tripo API返回错误: " + tripoResponse.getMessage());
                }

                // 更新任务信息
                task.setTaskId(tripoResponse.getData().getTaskId());
                task.setStatus("RUNNING");
                modelTaskMapper.updateById(task);

                log.info("图片生成3D任务提交成功: taskId={}, tripoTaskId={}",
                        taskId, tripoResponse.getData().getTaskId());

                // 等待任务完成
                waitForTaskCompletion(tripoResponse.getData().getTaskId(), taskId);

            } catch (Exception e) {
                log.error("图片生成3D异步处理失败: taskId={}", taskId, e);

                // 更新任务状态为失败
                ModelTask task = modelTaskMapper.selectById(taskId);
                task.setStatus("FAILED");
                task.setErrorMessage("Tripo API调用失败: " + e.getMessage());
                task.setCompletedAt(LocalDateTime.now());
                modelTaskMapper.updateById(task);
            }
        });
    }

    /**
     * 构建图片生成3D请求参数
     */
    private TripoImageToModelRequest buildImageToModelRequest(GenerateRequest request) {
        TripoImageToModelRequest tripoRequest = new TripoImageToModelRequest();

        // 设置文件信息 (必需)
        TripoImageToModelRequest.TripoFileInfo fileInfo = new TripoImageToModelRequest.TripoFileInfo();

        // 根据输入数据类型设置文件信息
        String inputData = request.getInputData();
        if (inputData.startsWith("http://") || inputData.startsWith("https://")) {
            // URL方式
            fileInfo.setUrl(inputData);
            // 根据URL推断文件类型
            if (inputData.toLowerCase().contains(".png")) {
                fileInfo.setType("png");
            } else if (inputData.toLowerCase().contains(".webp")) {
                fileInfo.setType("webp");
            } else {
                fileInfo.setType("jpeg"); // 默认
            }
        } else {
            // 假设是file_token (从上传接口获得)
            fileInfo.setFileToken(inputData);
            fileInfo.setType("jpeg"); // 默认类型
        }

        tripoRequest.setFile(fileInfo);

        // 模型版本 (优先使用用户指定的版本)
        String modelVersion = request.getModelVersion();
        if (modelVersion == null || modelVersion.trim().isEmpty()) {
            modelVersion = "v2.5-20250123"; // 默认版本
        }
        tripoRequest.setModelVersion(modelVersion);

        // 种子参数
        if (request.getModelSeed() != null) {
            tripoRequest.setModelSeed(request.getModelSeed());
        } else if (request.getSeed() != null) {
            tripoRequest.setModelSeed(request.getSeed());
        }

        if (request.getTextureSeed() != null) {
            tripoRequest.setTextureSeed(request.getTextureSeed());
        }

        // 风格参数
        if (request.getStyle() != null && !request.getStyle().trim().isEmpty()) {
            tripoRequest.setStyle(request.getStyle());
        }

        // 面数限制 (优先使用faceLimit，其次使用targetPolycount)
        if (request.getFaceLimit() != null) {
            tripoRequest.setFaceLimit(request.getFaceLimit());
        } else if (request.getTargetPolycount() != null) {
            tripoRequest.setFaceLimit(request.getTargetPolycount());
        }

        // 纹理和材质设置
        tripoRequest.setTexture(request.getTexture() != null ? request.getTexture() : true);
        tripoRequest.setPbr(request.getPbr() != null ? request.getPbr() : true);

        // 纹理质量
        String textureQuality = request.getTextureQuality();
        if (textureQuality == null || textureQuality.trim().isEmpty()) {
            textureQuality = "standard"; // 默认质量
        }
        tripoRequest.setTextureQuality(textureQuality);

        // 纹理对齐 (优先使用用户设置，其次根据enableOriginalUv判断)
        String textureAlignment = request.getTextureAlignment();
        if (textureAlignment == null || textureAlignment.trim().isEmpty()) {
            if (request.getEnableOriginalUv() != null && request.getEnableOriginalUv()) {
                textureAlignment = "original_image";
            } else {
                textureAlignment = "original_image"; // 默认优先保持原图像的视觉保真度
            }
        }
        tripoRequest.setTextureAlignment(textureAlignment);

        // 方向设置
        String orientation = request.getOrientation();
        if (orientation == null || orientation.trim().isEmpty()) {
            orientation = "default"; // 默认方向
        }
        tripoRequest.setOrientation(orientation);

        // 其他高级选项
        tripoRequest.setAutoSize(request.getAutoSize() != null ? request.getAutoSize() : false);

        // 四边形网格 (根据topology参数判断)
        Boolean quad = request.getQuad();
        if (quad == null && request.getTopology() != null) {
            quad = "quad".equalsIgnoreCase(request.getTopology());
        }
        tripoRequest.setQuad(quad != null ? quad : false);

        tripoRequest.setSmartLowPoly(request.getSmartLowPoly() != null ? request.getSmartLowPoly() : false);
        tripoRequest.setGenerateParts(request.getGenerateParts() != null ? request.getGenerateParts() : false);

        // 压缩选项
        if (request.getCompress() != null && !request.getCompress().trim().isEmpty()) {
            tripoRequest.setCompress(request.getCompress());
        }

        // 几何质量
        if (request.getGeometryQuality() != null && !request.getGeometryQuality().trim().isEmpty()) {
            tripoRequest.setGeometryQuality(request.getGeometryQuality());
        }

        return tripoRequest;
    }

    /**
     * 查询任务状态 - 使用本地任务ID
     */
    public TaskResponse getTaskStatus(Long localTaskId) {
        try {
            // 从数据库查询任务
            ModelTask task = modelTaskMapper.selectById(localTaskId);
            if (task == null) {
                throw new RuntimeException("任务不存在: " + localTaskId);
            }

            // 如果任务还没有Tripo任务ID，直接返回当前状态
            if (task.getTaskId() == null || task.getTaskId().trim().isEmpty()) {
                log.debug("任务尚未获得Tripo任务ID，返回当前状态: localTaskId={}, status={}", localTaskId, task.getStatus());
                return buildTaskResponse(task, null);
            }

            // 如果任务已经完成或转存中，直接返回数据库中的结果
            if ("SUCCESS".equals(task.getStatus()) || "FAILED".equals(task.getStatus()) || "TRANSFERRING".equals(task.getStatus())) {
                log.debug("任务已完成或转存中，直接返回结果: localTaskId={}, status={}", localTaskId, task.getStatus());
                return buildTaskResponse(task, null);
            }

            // 查询Tripo API任务状态
            TripoTaskResponse tripoResponse = tripoApiClient.getTaskStatus(task.getTaskId());

            if (tripoResponse.getCode() == 0) {
                String status = tripoResponse.getData().getStatus();

                // 更新数据库任务状态
                updateTaskFromTripoResponse(task, tripoResponse);
                modelTaskMapper.updateById(task);

                log.debug("任务状态已更新: localTaskId={}, tripoTaskId={}, status={}",
                         localTaskId, task.getTaskId(), task.getStatus());

                return buildTaskResponse(task, tripoResponse);
            } else {
                log.warn("Tripo API返回错误: localTaskId={}, tripoTaskId={}, error={}",
                        localTaskId, task.getTaskId(), tripoResponse.getMessage());
                return buildTaskResponse(task, null);
            }

        } catch (Exception e) {
            log.error("查询任务状态失败: localTaskId={}", localTaskId, e);
            throw new RuntimeException("查询任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 根据Tripo任务ID查询状态（备用方法）
     */
    public TaskResponse getTaskStatusByTripoId(String tripoTaskId) {
        try {
            // 从数据库查询任务
            QueryWrapper<ModelTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", tripoTaskId);
            ModelTask task = modelTaskMapper.selectOne(queryWrapper);

            if (task == null) {
                throw new RuntimeException("找不到对应的任务: tripoTaskId=" + tripoTaskId);
            }

            // 查询Tripo API任务状态
            TripoTaskResponse tripoResponse = tripoApiClient.getTaskStatus(tripoTaskId);

            if (tripoResponse.getCode() == 0) {
                // 更新数据库任务状态
                updateTaskFromTripoResponse(task, tripoResponse);
                modelTaskMapper.updateById(task);

                return buildTaskResponse(task, tripoResponse);
            } else {
                log.warn("Tripo API返回错误: tripoTaskId={}, error={}", tripoTaskId, tripoResponse.getMessage());
                return buildTaskResponse(task, null);
            }

        } catch (Exception e) {
            log.error("根据Tripo任务ID查询状态失败: tripoTaskId={}", tripoTaskId, e);
            throw new RuntimeException("查询任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务状态（从Tripo响应）
     */
    private void updateTaskFromTripoResponse(ModelTask task, TripoTaskResponse tripoResponse) {
        String status = tripoResponse.getData().getStatus();

        if ("success".equals(status)) {
            // 任务完成的处理已经在waitForTaskCompletion中完成，这里只更新状态
            // 避免重复处理导致数据覆盖
            if (!"SUCCESS".equals(task.getStatus()) && !"TRANSFERRING".equals(task.getStatus())) {
                task.setStatus("TRANSFERRING");
                task.setCompletedAt(LocalDateTime.now());
                // 注意：不在这里触发OSS转存，避免重复处理
            }
        } else if ("failed".equals(status) || "banned".equals(status) || "expired".equals(status)) {
            task.setStatus("FAILED");
            task.setErrorMessage("Tripo任务失败，状态: " + status);
            task.setCompletedAt(LocalDateTime.now());
        } else if ("running".equals(status)) {
            task.setStatus("IN_PROGRESS");
        } else if ("queued".equals(status)) {
            task.setStatus("PENDING");
        }
    }

    /**
     * 构建任务响应对象
     */
    private TaskResponse buildTaskResponse(ModelTask task, TripoTaskResponse tripoResponse) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTaskId(task.getTaskId());
        response.setTaskType(task.getTaskType());
        response.setPrompt(task.getPrompt());
        response.setInputType(task.getInputType());
        response.setInputData(task.getInputData());
        response.setStatus(task.getStatus());
        response.setModelUrl(task.getModelUrl());
        response.setBaseModelUrl(task.getBaseModelUrl());
        response.setPbrModelUrl(task.getPbrModelUrl());
        response.setTextureImageUrl(task.getTextureImageUrl());
        response.setNormalMapUrl(task.getNormalMapUrl());
        response.setMetallicMapUrl(task.getMetallicMapUrl());
        response.setRoughnessMapUrl(task.getRoughnessMapUrl());
        response.setPreviewUrl(task.getPreviewUrl());
        response.setErrorMessage(task.getErrorMessage());
        response.setCreatedAt(task.getCreatedAt());
        response.setCompletedAt(task.getCompletedAt());

        // 设置进度信息
        if (tripoResponse != null && tripoResponse.getData() != null && tripoResponse.getData().getProgress() != null) {
            TaskResponse.ProgressInfo progressInfo = new TaskResponse.ProgressInfo();
            progressInfo.setPercentage(tripoResponse.getData().getProgress());
            progressInfo.setStage(tripoResponse.getData().getStatus());
            response.setProgress(progressInfo);
        } else {
            // 根据状态设置默认进度
            TaskResponse.ProgressInfo progressInfo = new TaskResponse.ProgressInfo();
            switch (task.getStatus()) {
                case "PENDING":
                    progressInfo.setPercentage(0);
                    progressInfo.setStage("等待处理");
                    break;
                case "IN_PROGRESS":
                    progressInfo.setPercentage(30);
                    progressInfo.setStage("正在生成");
                    break;
                case "RUNNING":
                    progressInfo.setPercentage(60);
                    progressInfo.setStage("生成中");
                    break;
                case "TRANSFERRING":
                    progressInfo.setPercentage(90);
                    progressInfo.setStage("正在保存");
                    break;
                case "SUCCESS":
                    progressInfo.setPercentage(100);
                    progressInfo.setStage("生成完成");
                    break;
                case "FAILED":
                    progressInfo.setPercentage(0);
                    progressInfo.setStage("生成失败");
                    break;
                default:
                    progressInfo.setPercentage(0);
                    progressInfo.setStage("未知状态");
            }
            response.setProgress(progressInfo);
        }

        return response;
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
                response.setTaskType(task.getTaskType());
                response.setPrompt(task.getPrompt());
                response.setInputType(task.getInputType());
                response.setInputData(task.getInputData());
                response.setStatus(task.getStatus());
                response.setModelUrl(task.getModelUrl());
                response.setBaseModelUrl(task.getBaseModelUrl());
                response.setPbrModelUrl(task.getPbrModelUrl());
                response.setTextureImageUrl(task.getTextureImageUrl());
                response.setNormalMapUrl(task.getNormalMapUrl());
                response.setMetallicMapUrl(task.getMetallicMapUrl());
                response.setRoughnessMapUrl(task.getRoughnessMapUrl());
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

    /**
     * 安全获取任务信息（处理数据库连接问题）
     */
    private ModelTask safeGetTaskById(Long taskId) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return modelTaskMapper.selectById(taskId);
            } catch (Exception e) {
                log.warn("第{}次获取任务失败: taskId={}, error={}", attempt, taskId, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("获取任务失败，已重试{}次: taskId={}", maxRetries, taskId, e);
                }
            }
        }
        return null;
    }

    /**
     * 安全更新任务信息（处理数据库连接问题）
     */
    private boolean safeUpdateTask(ModelTask task) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                modelTaskMapper.updateById(task);
                return true;
            } catch (Exception e) {
                log.warn("第{}次更新任务失败: taskId={}, error={}", attempt, task.getId(), e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("更新任务失败，已重试{}次: taskId={}", maxRetries, task.getId(), e);
                }
            }
        }
        return false;
    }

    /**
     * 判断URL是否为OSS URL
     */
    private boolean isOssUrl(String url) {
        if (url == null) return false;
        return url.contains("aliyuncs.com") || url.contains("oss-");
    }
}
