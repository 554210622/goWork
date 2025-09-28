// src/config/index.js
const config = {
  baseURL: import.meta.env.VITE_APP_BASE_API || "http://localhost:8080/api/", // API 根路径
  timeout: 100000, // 请求超时时间（毫秒）
}

export default config
