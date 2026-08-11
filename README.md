# VarioPool

基于 **Nacos + Redis** 的 Java 动态线程池框架

## 架构设计

```text
VarioPool/
├── variopool-core              # 核心层：线程池、注册表、刷新引擎、配置解析
├── variopool-spring            # Spring 集成：注解、自动配置、事件驱动刷新
├── variopool-config-nacos      # Nacos 配置源适配器（可选依赖）
├── variopool-config-redis      # Redis 配置源适配器（可选依赖）
├── variopool-example           # 示例应用
├── variopool-dashboard         # 控制台后端 API（9009）
└── variopool-dashboard-ui      # Vue 3 控制台前端
```

### 分层职责

| 模块 | 职责 |
|------|------|
| **core** | `VarioPoolExecutor`、注册表、`ThreadPoolRefreshService`、YAML 解析 |
| **spring** | `@EnableVarioPool`、`@VarioPoolBean`、Bean 后置处理器、Spring 事件 |
| **config-nacos** | 监听 Nacos 配置变更，发布 `ConfigRefreshedEvent` |
| **config-redis** | Redis 存储配置 + Redisson Topic 推送刷新信号 |

### 刷新链路

```text
Nacos 变更 / Redis Topic
        ↓
ConfigSource（配置源适配器）
        ↓
ConfigRefreshedEvent（Spring 事件）
        ↓
ThreadPoolRefreshService（对比 + 热更新）
        ↓
VarioPoolRegistry 中的 ThreadPoolExecutor
```

Nacos 与 Redis **可同时启用**，任一源变更都会触发刷新（幂等）。

## 快速开始

### 1. 构建

```bash
cd VarioPool
mvn clean install -DskipTests
```

### 2. 启动依赖

- Nacos：`127.0.0.1:8848`
- Redis：`127.0.0.1:6379`

### 3. 配置 Nacos

在 Nacos 创建 `variopool-example.yaml`（参考 `variopool-example/src/main/resources/nacos-variopool-example.yaml`）。

### 4. 配置 Redis（可选）

将同样 YAML 内容写入 Redis：

```bash
redis-cli SET variopool:config:variopool-example "$(cat nacos-variopool-example.yaml)"
```

变更后发布刷新信号：

```bash
curl -X POST http://localhost:9090/variopool/redis/refresh-signal
```

### 5. 运行示例

```bash
cd variopool-example
mvn spring-boot:run
```

查看线程池状态：

```bash
curl http://localhost:9090/variopool/pools
```

## 使用方式

```java
@SpringBootApplication
@EnableVarioPool
public class Application { ... }

@Configuration
public class PoolConfig {

    @Bean
    @VarioPoolBean
    public VarioPoolExecutor orderExecutor() {
        return new VarioPoolExecutor("order-pool", ...);
    }
}
```

```yaml
variopool:
  enable: true
  nacos:
    data-id: variopool-example.yaml
    group: DEFAULT_GROUP
  redis:
    config-key: variopool:config:variopool-example
    topic: variopool:config:refresh:variopool-example
  executors:
    - pool-id: order-pool
      core-pool-size: 4
      maximum-pool-size: 8
      queue-capacity: 200
      work-queue: ResizableCapacityLinkedBlockingQueue
      rejected-handler: CallerRunsPolicy
      keep-alive-time: 60
```

## 动态修改支持

| 参数 | 运行时更新 |
|------|-----------|
| corePoolSize | ✅ |
| maximumPoolSize | ✅ |
| keepAliveTime | ✅ |
| rejectedHandler | ✅ |
| allowCoreThreadTimeout | ✅ |
| queueCapacity | ✅（需 `ResizableCapacityLinkedBlockingQueue`） |

## JVM 参数

替换队列时需要开放反射：

```text
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
```

## 控制台（Vue 完整版）

### 启动顺序

```bash
# 1. Dashboard API（9009）
cd variopool-dashboard && mvn spring-boot:run

# 2. 示例应用（9090）
cd variopool-example && mvn spring-boot:run

# 3. Vue 前端（5173）
cd variopool-dashboard-ui && npm install && npm run dev
```

访问 http://localhost:5173 ，默认账号 `admin / admin`

### 功能

- 线程池列表：Nacos 配置 + 实例运行时指标
- 参数编辑：保存后发布到 Nacos，可选同步 Redis
- 监控页：ECharts 仪表盘，5 秒自动刷新
