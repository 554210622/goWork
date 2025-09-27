// src/api/index.js
import request from "@/utils/request"

// 调用生成模型接口
export function generateModel(params) {
  return request.post("/generate-model", params)
}

// 获取历史记录
export function getHistory() {
  return request.get("/history")
}
