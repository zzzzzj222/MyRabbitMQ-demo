package com.rabbitmq.demo.dlx;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 死信队列 - 死信消费者
 *
 * 死信队列消费者负责：
 * 1. 接收从主队列转发过来的死信消息
 * 2. 处理这些死信消息（可以进行记录、分析、重试等操作）
 * 3. 通常用于处理异常情况，如消息过期、队列满、消息被拒绝等
 */
public class DLXConsumer {
  // 死信交换机名称
  private static final String DLX_EXCHANGE_NAME = "dlx_exchange";
  // 死信队列名称
  private static final String DLX_QUEUE_NAME = "dlx_queue";
  // 死信路由键
  private static final String DLX_ROUTING_KEY = "dlx_routing_key";

  public static void main(String[] args) throws IOException, TimeoutException {
    System.out.println("=== RabbitMQ 死信队列模式 - 死信消费者 ===");

    // 获取RabbitMQ连接
    Connection connection = RabbitMQUtils.getConnection();
    // 创建通道
    Channel channel = connection.createChannel();

    // 声明死信交换机（与生产者一致）
    channel.exchangeDeclare(DLX_EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

    // 声明死信队列（与生产者一致）
    channel.queueDeclare(DLX_QUEUE_NAME, true, false, false, null);

    // 将死信队列绑定到死信交换机（与生产者一致）
    channel.queueBind(DLX_QUEUE_NAME, DLX_EXCHANGE_NAME, DLX_ROUTING_KEY);

    System.out.println("死信队列消费者已启动，等待接收死信消息...");
    System.out.println("注意：这些消息是从主队列转发过来的死信");
    System.out.println("========================================\n");

    // 定义死信消费者
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
        long deliveryTag = envelope.getDeliveryTag();

        try {
          System.out.println("[死信队列] 接收死信消息: '" + message + "'");
          System.out.println("[死信队列] 消息属性: " + properties.toString());
          System.out.println("[死信队列] 原路由键: " + envelope.getRoutingKey());

          // 模拟死信消息处理（可以记录日志、发送告警、进行重试等）
          TimeUnit.SECONDS.sleep(3);

          System.out.println("[死信队列] 死信处理完成: '" + message + "'");
          System.out.println("----------------------------------------");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        // 确认死信消息
        channel.basicAck(deliveryTag, false);
      }
    };

    // 启动死信队列消费者，关闭自动确认
    channel.basicConsume(DLX_QUEUE_NAME, false, consumer);

    // 保持程序运行
    System.out.println("按 Ctrl+C 结束程序...");
    try {
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // 关闭通道和连接
      RabbitMQUtils.closeChannel(channel);
      RabbitMQUtils.closeConnection(connection);
    }
  }
}