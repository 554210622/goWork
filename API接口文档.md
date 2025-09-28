# API接口文档

## 接口概览

### 基础信息
- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **字符编码**: UTF-8

## 3D模型生成接口

### 1. 文本生成3D模型
**接口地址**: `POST /model3d/text-to-3d`

**请求参数**:
```json
{
    "userId": 1,
    "prompt": "一只可爱的小猫",
    "style": "cartoon",
    "quality": "standard"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": 123,
        "status": "PENDING"
    }
}
```

### 2. 图片生成3D模型
**接口地址**: `POST /model3d/image-to-3d`

**请求参数**:
```json
{
    "userId": 1,
    "imageUrl": "https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/upload/images/2025/09/27/example.jpg",
    "style": "realistic"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": 124,
        "status": "PENDING"
    }
}
```

### 3. 查询任务状态
**接口地址**: `GET /model3d/task/{taskId}`

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": 123,
        "status": "SUCCESS",
        "progress": 100,
        "modelUrl": "https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/model/2025/09/27/model.glb",
        "pbrModelUrl": "https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/pbr_model/2025/09/27/pbr_model.glb",
        "previewUrl": "https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/27/preview.webp",
        "createdAt": "2025-09-27T20:30:00",
        "completedAt": "2025-09-27T20:32:15"
    }
}
```

### 4. 获取用户任务列表
**接口地址**: `GET /model3d/tasks?userId={userId}&page={page}&size={size}`

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 50,
        "page": 1,
        "size": 10,
        "tasks": [
            {
                "taskId": 123,
                "inputType": "text",
                "prompt": "一只可爱的小猫",
                "status": "SUCCESS",
                "previewUrl": "https://...",
                "createdAt": "2025-09-27T20:30:00"
            }
        ]
    }
}
```

## 文件上传接口

### 1. 图片上传
**接口地址**: `POST /upload/image`

**请求参数**: `multipart/form-data`
- **file**: 图片文件 (支持jpg, png, webp格式，最大10MB)

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "url": "https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/upload/images/2025/09/27/image.jpg",
        "fileName": "image.jpg",
        "fileSize": 1024000
    }
}
```

## 状态码说明

### 任务状态
- **PENDING**: 任务已创建，等待处理
- **RUNNING**: 任务正在执行中
- **TRANSFERRING**: 任务完成，正在转存文件
- **SUCCESS**: 任务成功完成
- **FAILED**: 任务执行失败

