// src/config/index.js
const config = {
  baseURL: import.meta.env.VITE_APP_BASE_API || "https://276fb319.r16.cpolar.top/api/", // API 根路径
  timeout: 100000, // 请求超时时间（毫秒）
}

export default config
