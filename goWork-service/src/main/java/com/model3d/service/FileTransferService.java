package com.model3d.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 文件转存服务
 * 负责将Tripo临时文件转存到阿里云OSS
 */
@Slf4j
@Service
public class FileTransferService {

    @Autowired
    private OssService ossService;

    /**
     * 异步转存任务文件到OSS
     * 
     * @param taskId 任务ID
     * @param modelUrl 主模型URL
     * @param baseModelUrl 基础模型URL
     * @param pbrModelUrl PBR模型URL
     * @param previewUrl 预览图URL
     * @param textureImageUrl 纹理图像URL
     * @param normalMapUrl 法线贴图URL
     * @param metallicMapUrl 金属度贴图URL
     * @param roughnessMapUrl 粗糙度贴图URL
     */
    @Async("model3dTaskExecutor")
    public CompletableFuture<Void> transferFilesToOss(Long taskId, String modelUrl, String baseModelUrl, 
                                                     String pbrModelUrl, String previewUrl, String textureImageUrl,
                                                     String normalMapUrl, String metallicMapUrl, String roughnessMapUrl) {
        
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("开始转存任务文件到OSS: taskId={}", taskId);
                
                int successCount = 0;
                int totalCount = 0;
                
                // 转存主模型文件 (GLB格式)
                if (modelUrl != null && !modelUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(modelUrl, null, "model");
                        ossService.updateTaskUrl(taskId, "model_url", ossUrl);
                        successCount++;
                        log.info("主模型文件转存成功: taskId={}, ossUrl={}", taskId, ossUrl);
                    } catch (Exception e) {
                        log.error("主模型文件转存失败: taskId={}, sourceUrl={}", taskId, modelUrl, e);
                    }
                }
                
                // 转存基础模型文件
                if (baseModelUrl != null && !baseModelUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(baseModelUrl, null, "base_model");
                        ossService.updateTaskUrl(taskId, "base_model_url", ossUrl);
                        successCount++;
                        log.info("基础模型文件转存成功: taskId={}", taskId);
                    } catch (Exception e) {
                        log.error("基础模型文件转存失败: taskId={}, sourceUrl={}", taskId, baseModelUrl, e);
                    }
                }
                
                // 转存PBR模型文件
                if (pbrModelUrl != null && !pbrModelUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(pbrModelUrl, null, "pbr_model");
                        ossService.updateTaskUrl(taskId, "pbr_model_url", ossUrl);
                        successCount++;
                        log.info("PBR模型文件转存成功: taskId={}", taskId);
                    } catch (Exception e) {
                        log.error("PBR模型文件转存失败: taskId={}, sourceUrl={}", taskId, pbrModelUrl, e);
                    }
                }
                
                // 转存预览图
                if (previewUrl != null && !previewUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(previewUrl, null, "preview");
                        ossService.updateTaskUrl(taskId, "preview_url", ossUrl);
                        successCount++;
                        log.info("预览图转存成功: taskId={}", taskId);
                    } catch (Exception e) {
                        log.error("预览图转存失败: taskId={}, sourceUrl={}", taskId, previewUrl, e);
                    }
                }
                
                // 转存纹理图像
                if (textureImageUrl != null && !textureImageUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(textureImageUrl, null, "texture");
                        ossService.updateTaskUrl(taskId, "texture_image_url", ossUrl);
                        successCount++;
                        log.info("纹理图像转存成功: taskId={}", taskId);
                    } catch (Exception e) {
                        log.error("纹理图像转存失败: taskId={}, sourceUrl={}", taskId, textureImageUrl, e);
                    }
                }
                
                // 转存法线贴图
                if (normalMapUrl != null && !normalMapUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(normalMapUrl, null, "normal_map");
                        ossService.updateTaskUrl(taskId, "normal_map_url", ossUrl);
                        successCount++;
                        log.info("法线贴图转存成功: taskId={}", taskId);
                    } catch (Exception e) {
                        log.error("法线贴图转存失败: taskId={}, sourceUrl={}", taskId, normalMapUrl, e);
                    }
                }
                
                // 转存金属度贴图
                if (metallicMapUrl != null && !metallicMapUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(metallicMapUrl, null, "metallic_map");
                        ossService.updateTaskUrl(taskId, "metallic_map_url", ossUrl);
                        successCount++;
                        log.info("金属度贴图转存成功: taskId={}", taskId);
                    } catch (Exception e) {
                        log.error("金属度贴图转存失败: taskId={}, sourceUrl={}", taskId, metallicMapUrl, e);
                    }
                }
                
                // 转存粗糙度贴图
                if (roughnessMapUrl != null && !roughnessMapUrl.trim().isEmpty()) {
                    totalCount++;
                    try {
                        String ossUrl = ossService.uploadFromUrl(roughnessMapUrl, null, "roughness_map");
                        ossService.updateTaskUrl(taskId, "roughness_map_url", ossUrl);
                        successCount++;
                        log.info("粗糙度贴图转存成功: taskId={}", taskId);
                    } catch (Exception e) {
                        log.error("粗糙度贴图转存失败: taskId={}, sourceUrl={}", taskId, roughnessMapUrl, e);
                    }
                }
                
                log.info("任务文件转存完成: taskId={}, 成功: {}/{}", taskId, successCount, totalCount);
                
            } catch (Exception e) {
                log.error("转存任务文件到OSS失败: taskId={}", taskId, e);
            }
        });
    }

    /**
     * 单个文件转存
     */
    public String transferSingleFile(String sourceUrl, String fileType) {
        try {
            return ossService.uploadFromUrl(sourceUrl, null, fileType);
        } catch (Exception e) {
            log.error("单个文件转存失败: sourceUrl={}, fileType={}", sourceUrl, fileType, e);
            return sourceUrl; // 转存失败时返回原URL
        }
    }
}