# RabbitMQ 镜像队列集群策略配置详解

## 一、什么是RabbitMQ集群策略

RabbitMQ集群策略（Policy）是一种动态配置机制，用于：
- 为队列、交换器等资源应用特定的配置
- 实现镜像队列的高可用性配置
- 动态更新资源配置，无需重启服务

策略通过**正则表达式**匹配资源名称，并为匹配的资源应用指定的配置参数。

## 二、镜像队列策略配置方法

### 方法1：使用命令行工具 (rabbitmqctl)

#### 1.1 基本策略配置命令

```bash
# 在集群任意节点上执行
rabbitmqctl set_policy [-p <vhost>] <name> <pattern> <definition>
```

**参数说明：**
- `-p <vhost>`：可选，指定虚拟主机（默认为/）
- `<name>`：策略名称，如"ha-all"
- `<pattern>`：队列名称匹配的正则表达式，如"^ha_"匹配所有以ha_开头的队列
- `<definition>`：JSON格式的策略参数，如'{"ha-mode":"all"}'

#### 1.2 常用镜像队列策略示例

**示例1：所有队列都镜像到所有节点**
```bash
rabbitmqctl set_policy ha-all "" '{"ha-mode":"all"}'
```

**示例2：以ha_开头的队列镜像到所有节点**
```bash
rabbitmqctl set_policy ha-all "^ha_" '{"ha-mode":"all"}'
```

**示例3：镜像到指定数量的节点**
```bash
# 镜像到3个节点
rabbitmqctl set_policy ha-three "^ha_" '{"ha-mode":"exactly","ha-params":3}'
```

**示例4：镜像到指定名称的节点**
```bash
# 镜像到名为rabbit@node1和rabbit@node2的节点
rabbitmqctl set_policy ha-nodes "^ha_" '{"ha-mode":"nodes","ha-params":["rabbit@node1","rabbit@node2"]}'
```

### 方法2：使用Web管理界面

#### 2.1 访问管理界面
1. 打开浏览器，访问 http://your-rabbitmq-server:15672
2. 使用管理员账号登录（默认：guest/guest）

#### 2.2 配置策略步骤

1. **进入策略配置页面**：
   - 点击顶部导航栏的"Admin"选项
   - 在右侧菜单中点击"Policies"
   - 点击"Add / update a policy"

2. **填写策略配置**：
   - **Name**：策略名称（如ha-all）
   - **Pattern**：队列名称匹配正则（如^ha_）
   - **Apply to**：选择"Queues"
   - **Priority**：策略优先级（数字越大优先级越高）
   - **Definition**：点击"Add field"添加参数

3. **添加镜像队列参数**：
   - 选择"ha-mode"，设置值为：
     - `all`：镜像到所有节点
     - `exactly`：镜像到指定数量的节点，需要配合`ha-params`设置数量
     - `nodes`：镜像到指定名称的节点，需要配合`ha-params`设置节点名称列表

4. **保存策略**：点击"Add policy"按钮

## 三、镜像队列策略参数详解

### 核心参数

| 参数名 | 类型 | 可选值 | 说明 |
|--------|------|--------|------|
| `ha-mode` | 字符串 | `all`/`exactly`/`nodes` | 镜像模式 |
| `ha-params` | 数值/数组 | - | 配合ha-mode使用的参数 |
| `ha-sync-mode` | 字符串 | `automatic`/`manual` | 队列同步模式 |
| `ha-promote-on-shutdown` | 字符串 | `always`/`when-synced` | 节点关闭时的主节点提升策略 |

### 参数详细说明

1. **ha-mode**：
   - `all`：将队列镜像到集群中的所有节点
   - `exactly`：将队列镜像到指定数量的节点，需配合`ha-params`指定数量
   - `nodes`：将队列镜像到指定名称的节点，需配合`ha-params`指定节点列表

2. **ha-sync-mode**：
   - `automatic`：队列创建后自动同步所有镜像节点
   - `manual`：需要手动触发队列同步

3. **ha-promote-on-shutdown**：
   - `always`：节点关闭时，即使镜像节点未同步完成也会提升为主节点
   - `when-synced`：只有当镜像节点同步完成后才会提升为主节点

## 四、策略的匹配规则与优先级

1. **匹配规则**：
   - 策略通过正则表达式匹配队列名称
   - 可以为不同的队列设置不同的策略
   - 一个队列可以匹配多个策略

2. **优先级处理**：
   - 当多个策略匹配同一个队列时，**优先级高**（priority数值大）的策略生效
   - 如果优先级相同，**后创建**的策略生效

## 五、查看和管理策略

### 查看策略
```bash
# 查看所有策略
rabbitmqctl list_policies

# 查看指定虚拟主机的策略
rabbitmqctl list_policies -p /
```

### 删除策略
```bash
# 删除指定名称的策略
rabbitmqctl clear_policy <name>

# 删除指定虚拟主机的策略
rabbitmqctl clear_policy -p <vhost> <name>
```

### 更新策略
```bash
# 重新执行set_policy命令即可更新现有策略
rabbitmqctl set_policy <name> <pattern> <definition>
```

## 六、与代码的关系（为什么代码中不需要配置）

正如`MirrorQueueConsumer.java`第43行注释所说：
```java
// 注意：镜像队列的配置是通过集群策略实现的，代码中不需要特殊配置
```

这是因为：
1. **解耦设计**：策略配置与应用代码分离，便于动态管理
2. **集中管理**：通过管理界面或命令行统一配置，无需修改多个应用
3. **动态更新**：可以在不重启应用的情况下修改镜像队列配置
4. **灵活匹配**：通过正则表达式灵活匹配需要镜像的队列

## 七、最佳实践

1. **命名规范**：为需要镜像的队列使用统一前缀（如`ha_`）
2. **合理选择镜像模式**：
   - 小型集群（2-3节点）：使用`all`模式
   - 大型集群（>3节点）：使用`exactly`模式限制镜像数量
3. **设置合理的同步模式**：
   - 新队列：使用`automatic`自动同步
   - 现有大型队列：使用`manual`避免性能问题
4. **优先级设置**：为不同级别的队列设置不同优先级的策略
5. **定期检查**：使用`rabbitmqctl list_policies`定期检查策略配置

## 八、验证镜像队列配置

### 方法1：使用命令行查看队列状态
```bash
rabbitmqctl list_queues name durable auto_delete arguments policy slave_pids
```

### 方法2：使用管理界面查看
1. 进入"Queues"页面
2. 查看队列的"Features"列，如果显示"Mirrored"则表示镜像队列已生效
3. 点击队列名称，在"Mirror Details"部分查看镜像节点信息

---

通过以上配置，您就可以为RabbitMQ队列启用镜像功能，实现高可用性。镜像队列策略是RabbitMQ集群中实现高可用性的核心配置机制。