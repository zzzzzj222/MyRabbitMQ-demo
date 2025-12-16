package com.rabbitmq.demo.delay;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 延迟队列消费者
 *
 * 延迟队列消费者特点：
 * 1. 从延迟队列（死信队列）接收消息
 * 2. 这些消息是经过指定延迟时间后才被转发到延迟队列的
 * 3. 消费者接收到的消息已经是延迟处理的消息
 *
 * 注意事项：
 * - 消费者需要声明与生产者相同的延迟交换机和延迟队列
 * - 不需要声明临时队列，因为临时队列是由生产者管理的
 */
public class DelayQueueConsumer {
  // 延迟交换机（死信交换机）名称
  private static final String DELAY_EXCHANGE_NAME = "delay_exchange";
  // 延迟队列（死信队列）名称
  private static final String DELAY_QUEUE_NAME = "delay_queue";
  // 延迟路由键
  private static final String DELAY_ROUTING_KEY = "delay_routing_key";

  public static void main(String[] args) throws IOException, TimeoutException {
    System.out.println("=== RabbitMQ 延迟队列模式 - 消费者 ===");

    // 获取RabbitMQ连接
    Connection connection = RabbitMQUtils.getConnection();
    // 创建通道
    Channel channel = connection.createChannel();

    // 1. 声明延迟交换机（与生产者一致）
    channel.exchangeDeclare(DELAY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

    // 2. 声明延迟队列（与生产者一致）
    channel.queueDeclare(DELAY_QUEUE_NAME, true, false, false, null);

    // 3. 将延迟队列绑定到延迟交换机（与生产者一致）
    channel.queueBind(DELAY_QUEUE_NAME, DELAY_EXCHANGE_NAME, DELAY_ROUTING_KEY);

    System.out.println("延迟队列消费者已启动，等待接收延迟消息...");
    System.out.println("注意：这些消息是经过指定延迟时间后才到达的");
    System.out.println("========================================\n");

    // 定义延迟队列消费者
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
        long deliveryTag = envelope.getDeliveryTag();

        try {
          System.out.println("[延迟队列] 接收延迟消息: '" + message + "'");
          System.out.println("[延迟队列] 消息属性: " + properties.toString());

          // 模拟消息处理
          TimeUnit.SECONDS.sleep(2);

          System.out.println("[延迟队列] 延迟消息处理完成: '" + message + "'");
          System.out.println("----------------------------------------");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        // 确认消息
        channel.basicAck(deliveryTag, false);
      }
    };

    // 启动消费者，关闭自动确认
    channel.basicConsume(DELAY_QUEUE_NAME, false, consumer);

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