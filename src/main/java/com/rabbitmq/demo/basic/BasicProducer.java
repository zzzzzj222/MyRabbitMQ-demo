package com.rabbitmq.demo.basic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 基础的消息生产者
 * 实现了最简单的消息传递模式：一个生产者发送消息到队列
 */
public class BasicProducer {
  // 队列名称
  private static final String QUEUE_NAME = "hello_queue";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 获取 RabbitMQ 连接
    Connection connection = RabbitMQUtils.getConnection();
    // 创建通道
    Channel channel = connection.createChannel();

    /**
     * 声明队列
     * 参数说明：
     * 1. queue: 队列名称
     * 2. durable: 是否持久化
     * 3. exclusive: 是否排外，即只允许当前连接访问
     * 4. autoDelete: 是否自动删除，当最后一个消费者断开连接后自动删除
     * 5. arguments: 其他参数
     */
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 要发送的消息
    String message = "Hello RabbitMQ!";

    /**
     * 发送消息
     * 参数说明：
     * 1. exchange: 交换机名称，使用空字符串表示默认交换机
     * 2. routingKey: 路由键，这里使用队列名称
     * 3. props: 消息属性
     * 4. body: 消息体
     */
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
    System.out.println("[生产者] 发送消息: '" + message + "'");

    // 关闭通道和连接
    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }
}