# 3D Model Generation Service

基于Spring Boot + MyBatis Plus + MySQL + Redis的3D模型生成服务

## 技术栈

- **Java 17**
- **Spring Boot 3.2.0**
- **MyBatis Plus 3.5.4**
- **MySQL 8.0**
- **Redis**
- **Meshy AI API**

## 快速开始

### 1. 环境准备

确保本机已安装：
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis

### 2. 数据库初始化

```sql
-- 执行初始化脚本
source src/main/resources/sql/init.sql
```

### 3. 配置文件

复制环境变量模板：
```bash
cp .env.example .env
```

编辑 `.env` 文件，填入正确的配置信息。

### 4. 启动服务

```bash
# 方式1: Maven启动
mvn spring-boot:run

# 方式2: IDE启动
# 直接运行 Application.java 主类
```

### 5. 验证服务

访问以下地址验证服务是否正常：

- 健康检查: http://localhost:8080/api/test/health
- 服务状态: http://localhost:8080/actuator/health

## API接口

### 测试接口

- `GET /api/test/health` - 服务健康检查
- `GET /api/test/db` - 数据库连接测试
- `GET /api/test/redis` - Redis连接测试

## 项目结构

```
src/main/java/com/model3d/
├── Application.java              # 启动类
├── config/                       # 配置类
│   ├── WebConfig.java           # Web配置
│   └── MeshyConfig.java         # Meshy AI配置
├── controller/                   # 控制器
│   └── TestController.java      # 测试控制器
├── entity/                       # 实体类
│   ├── User.java
│   ├── ModelTask.java
│   └── ModelFile.java
├── dto/                          # 数据传输对象
│   ├── GenerateRequest.java
│   └── TaskResponse.java
├── mapper/                       # MyBatis Mapper
│   ├── UserMapper.java
│   ├── ModelTaskMapper.java
│   └── ModelFileMapper.java
├── service/                      # 服务层 (待实现)
└── common/                       # 公共类
    └── Result.java              # 统一响应结果
```

## 开发计划

- [x] 项目基础架构搭建
- [x] 数据库表设计
- [x] 基础实体类和配置
- [ ] Meshy AI集成服务
- [ ] 3D模型生成接口
- [ ] 任务状态管理
- [ ] 文件存储服务
- [ ] 前端接口开发

## 注意事项

1. 确保MySQL和Redis服务正常运行
2. 配置正确的Meshy AI API密钥
3. 开发阶段可使用cpolar进行内网穿透测试