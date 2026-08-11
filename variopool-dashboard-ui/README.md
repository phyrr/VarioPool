# VarioPool Dashboard UI

Vue 3 + Element Plus 动态线程池控制台。

## 功能

- 登录鉴权
- 线程池列表（Nacos 配置 + 运行时指标）
- 参数编辑（发布到 Nacos，可选同步 Redis）
- 实时监控页（ECharts 仪表盘，5 秒自动刷新）

## 启动

```bash
# 1. 启动后端 API（9009）
cd ../variopool-dashboard
mvn spring-boot:run

# 2. 启动示例应用（9090）
cd ../variopool-example
mvn spring-boot:run

# 3. 启动前端
npm install
npm run dev
```

访问：http://localhost:5173

默认账号：`admin / admin`

## 技术栈

- Vue 3 + Vite
- Element Plus
- Pinia + Vue Router
- Axios
- ECharts
