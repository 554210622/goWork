import request from "../utils/request"

// 上传图片
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: "/upload/image",
    method: "post",
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 文字生成3D模型
export function generateModelFromText(data) {
  return request({
    url: "/model3d/text-to-3d",
    method: "post",
    data,
  })
}

// 图片生成3D模型
export function generateModelFromImage(data) {
  return request({
    url: "/model/generate/image",
    method: "post",
    data,
  })
}

// 获取模型历史记录
export function getModelHistoryApi() {
  return request({
    url: "/model3d/tasks?userId=1",
    method: "get",
  })
}

// 根据ID查询模型详情
export function getModelByIdApi(id) {
  return request({
    url: `/model3d/task/${id}`,
    method: "get",
  })
}

