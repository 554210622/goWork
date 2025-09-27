// src/utils/request.js
import axios from "axios"
import config from "@/config"

// 创建 axios 实例
const service = axios.create({
  baseURL: config.baseURL,
  timeout: config.timeout,
})

// 请求拦截器
service.interceptors.request.use(
  (req) => {
    // 如果有 token，可以在这里带上
    const token = localStorage.getItem("token")
    if (token) {
      req.headers.Authorization = `Bearer ${token}`
    }
    return req
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (res) => {
    // 常见接口返回格式：{ code: 200, data: {...}, message: "success" }
    const data = res.data
    if (data.code && data.code !== 200) {
      return Promise.reject(data.message || "请求出错")
    }
    return data.data || data
  },
  (error) => {
    console.error("请求错误:", error)
    return Promise.reject(error)
  }
)

export default service
