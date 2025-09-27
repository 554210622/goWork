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
    url: "/model/generate/text",
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
export function getModelHistory() {
  return request({
    url: "/model/history",
    method: "get",
  })
}

