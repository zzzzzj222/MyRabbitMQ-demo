# RabbitMQ 学习项目

这是一个用于学习 RabbitMQ 的示例项目，包含了 RabbitMQ 中常用的消息模式实现。

## 项目结构

```
├── src/main/java/com/rabbitmq/demo/
│   ├── basic/          # 基础的生产者-消费者模式
│   ├── workqueue/      # 工作队列模式
│   ├── pubsub/         # 发布/订阅模式
│   ├── routing/        # 路由模式
│   ├── topic/          # 主题模式
│   └── utils/          # RabbitMQ 工具类
├── pom.xml             # Maven 配置文件
└── README.md           # 项目说明文档
```

## 环境准备

1. 安装 Java 8 或更高版本
2. 安装 Maven
3. 安装 RabbitMQ 服务器
   - 下载地址：https://www.rabbitmq.com/download.html
   - 启动 RabbitMQ 服务器

## 运行项目

### 编译项目

```bash
mvn compile
```

### 运行示例

#### 1. 基础的生产者-消费者模式

```bash
# 启动消费者
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.basic.BasicConsumer"

# 启动生产者
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.basic.BasicProducer"
```

#### 2. 工作队列模式

```bash
# 启动消费者1
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.workqueue.WorkQueueConsumer1"

# 启动消费者2
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.workqueue.WorkQueueConsumer2"

# 启动生产者
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.workqueue.WorkQueueProducer"
```

#### 3. 发布/订阅模式

```bash
# 启动消费者1
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.pubsub.PubSubConsumer1"

# 启动消费者2
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.pubsub.PubSubConsumer2"

# 启动生产者
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.pubsub.PubSubProducer"
```

#### 4. 路由模式

```bash
# 启动消费者1（仅接收error级别的消息）
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.routing.RoutingConsumer1"

# 启动消费者2（接收info、warning、error级别的消息）
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.routing.RoutingConsumer2"

# 启动生产者
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.routing.RoutingProducer"
```

#### 5. 主题模式

```bash
# 启动消费者1（匹配 *.orange.* 模式）
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.topic.TopicConsumer1"

# 启动消费者2（匹配 *.*.rabbit 和 lazy.# 模式）
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.topic.TopicConsumer2"

# 启动生产者
mvn exec:java -Dexec.mainClass="com.rabbitmq.demo.topic.TopicProducer"
```

## RabbitMQ 管理界面

启动 RabbitMQ 服务器后，可以通过以下地址访问管理界面：

- 地址：http://localhost:15672/
- 用户名：guest
- 密码：guest

## 各模式说明

### 1. 基础的生产者-消费者模式
- 最简单的消息传递模式
- 一个生产者发送消息到队列，一个消费者从队列接收消息

### 2. 工作队列模式
- 多个消费者从同一个队列接收消息
- 实现了负载均衡
- 支持消息确认机制，确保消息被正确处理
- 支持消息持久化，确保服务器重启后消息不丢失

### 3. 发布/订阅模式
- 使用 fanout 交换机
- 消息被发送到所有绑定到交换机的队列
- 每个消费者都能收到所有消息

### 4. 路由模式
- 使用 direct 交换机
- 消息通过路由键发送到特定的队列
- 消费者可以选择接收特定路由键的消息

### 5. 主题模式
- 使用 topic 交换机
- 支持通配符匹配路由键
- 提供更灵活的消息路由方式

## 学习资源

- [RabbitMQ 官方文档](https://www.rabbitmq.com/documentation.html)
- [RabbitMQ 教程](https://www.rabbitmq.com/getstarted.html)
