package com.rabbitmq.demo.mirror;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 镜像队列生产者
 *
 * 镜像队列（Mirror Queue）是RabbitMQ的高可用性（HA）解决方案：
 * 1. 将队列复制到集群中的多个节点，确保数据冗余
 * 2. 当主节点故障时，备用节点可以自动接管服务
 * 3. 镜像队列的配置通过RabbitMQ集群的策略（Policy）实现，而不是在代码中配置
 *
 * 镜像队列配置：
 * 1. 需要RabbitMQ集群环境（至少2个节点）
 * 2. 通过管理界面或命令行设置策略：
 * - 命令行示例：
 * rabbitmqctl set_policy ha-all "^ha." '{"ha-mode":"all"}'
 * - 策略说明：
 * - ha-all：策略名称
 * - ^ha.：匹配所有以ha开头的队列
 * - ha-mode: all：将队列镜像到所有节点
 *
 * 镜像队列特点：
 * 1. 消息会被复制到所有镜像节点
 * 2. 只有主节点处理消息的发布和消费
 * 3. 主节点故障时，集群会自动选举新的主节点
 * 4. 消费者连接到集群中的任何节点都可以访问镜像队列
 */
public class MirrorQueueProducer {
  // 镜像队列名称（需要以ha开头，与策略匹配）
  private static final String MIRROR_QUEUE_NAME = "ha_mirror_queue";

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    System.out.println("=== RabbitMQ 镜像队列模式 - 生产者 ===");

    // 获取RabbitMQ连接
    Connection connection = RabbitMQUtils.getConnection();
    // 创建通道
    Channel channel = connection.createChannel();

    // 声明镜像队列
    // 注意：镜像队列的配置是通过集群策略实现的，代码中不需要特殊配置
    // 只需要声明队列名称与策略匹配即可（这里以ha开头）
    channel.queueDeclare(MIRROR_QUEUE_NAME, true, false, false, null);

    System.out.println("镜像队列生产者已启动");
    System.out.println("注意：需要RabbitMQ集群环境和镜像队列策略配置");
    System.out.println("策略示例：rabbitmqctl set_policy ha-all '^ha.' '{\"ha-mode\":\"all\"}'");
    System.out.println("========================================\n");

    // 消息属性构建器
    AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
    // 设置消息持久化
    propsBuilder.deliveryMode(2);

    // 发送消息到镜像队列
    System.out.println("开始发送消息到镜像队列...");

    for (int i = 1; i <= 5; i++) {
      String message = "高可用性消息 - " + i;
      channel.basicPublish("", MIRROR_QUEUE_NAME, propsBuilder.build(), message.getBytes());
      System.out.println("发送消息: '" + message + "'");
      TimeUnit.SECONDS.sleep(1);
    }

    System.out.println("\n所有消息发送完成！");
    System.out.println("消息已被复制到所有镜像节点");

    // 关闭通道和连接
    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }
}