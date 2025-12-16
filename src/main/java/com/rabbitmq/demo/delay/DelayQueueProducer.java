package com.rabbitmq.demo.delay;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 延迟队列生产者
 *
 * 延迟队列（Delayed Queue）是一种特殊队列：
 * 1. 消息在队列中不会立即被消费，而是在指定的延迟时间后才会被处理
 * 2. 实现方式：TTL（Time To Live） + DLX（Dead Letter Exchange）
 * 3. 原理：
 * - 创建一个临时队列，设置消息TTL
 * - 该队列配置了死信交换机（DLX）
 * - 消息过期后成为死信，被转发到死信队列
 * - 消费者从死信队列接收延迟后的消息
 *
 * 注意事项：
 * - 临时队列是排他的，只对当前连接可见，连接关闭后自动删除
 * - 消息TTL可以设置在队列级别或消息级别
 * - 队列级别TTL：所有消息都有相同的过期时间
 * - 消息级别TTL：每个消息可以设置不同的过期时间
 */
public class DelayQueueProducer {
  // 延迟交换机（死信交换机）名称
  private static final String DELAY_EXCHANGE_NAME = "delay_exchange";
  // 延迟队列（死信队列）名称
  private static final String DELAY_QUEUE_NAME = "delay_queue";
  // 延迟路由键
  private static final String DELAY_ROUTING_KEY = "delay_routing_key";

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    System.out.println("=== RabbitMQ 延迟队列模式 - 生产者 ===");

    // 获取RabbitMQ连接
    Connection connection = RabbitMQUtils.getConnection();
    // 创建通道
    Channel channel = connection.createChannel();

    // 1. 声明延迟交换机（类型：direct）
    channel.exchangeDeclare(DELAY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

    // 2. 声明延迟队列（死信队列）
    channel.queueDeclare(DELAY_QUEUE_NAME, true, false, false, null);

    // 3. 将延迟队列绑定到延迟交换机
    channel.queueBind(DELAY_QUEUE_NAME, DELAY_EXCHANGE_NAME, DELAY_ROUTING_KEY);

    // 4. 声明临时队列参数
    Map<String, Object> tempQueueArgs = new HashMap<>();
    // 设置死信交换机
    tempQueueArgs.put("x-dead-letter-exchange", DELAY_EXCHANGE_NAME);
    // 设置死信路由键
    tempQueueArgs.put("x-dead-letter-routing-key", DELAY_ROUTING_KEY);
    // 注意：这里不设置队列级别的TTL，而是在消息级别设置TTL

    // 5. 声明临时队列（排他的，自动删除的）
    String tempQueueName = channel.queueDeclare().getQueue();
    channel.queueBind(tempQueueName, "", tempQueueName); // 绑定到默认交换机

    System.out.println("延迟队列环境已准备完成:");
    System.out.println("- 延迟交换机: " + DELAY_EXCHANGE_NAME);
    System.out.println("- 延迟队列: " + DELAY_QUEUE_NAME);
    System.out.println("- 临时队列: " + tempQueueName);
    System.out.println("========================================\n");

    // 消息属性构建器
    AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
    // 设置消息持久化
    propsBuilder.deliveryMode(2);

    // 发送不同延迟时间的消息
    System.out.println("开始发送不同延迟时间的消息...");

    // 1. 发送5秒延迟消息
    long delayTime1 = 5000; // 5秒
    propsBuilder.expiration(String.valueOf(delayTime1));
    channel.basicPublish("", tempQueueName, propsBuilder.build(), "5秒延迟消息 - 定时任务1".getBytes());
    System.out.println("发送消息: '5秒延迟消息 - 定时任务1' (延迟: " + delayTime1 + "ms)");
    TimeUnit.SECONDS.sleep(1);

    // 2. 发送10秒延迟消息
    long delayTime2 = 10000; // 10秒
    propsBuilder.expiration(String.valueOf(delayTime2));
    channel.basicPublish("", tempQueueName, propsBuilder.build(), "10秒延迟消息 - 定时任务2".getBytes());
    System.out.println("发送消息: '10秒延迟消息 - 定时任务2' (延迟: " + delayTime2 + "ms)");
    TimeUnit.SECONDS.sleep(1);

    // 3. 发送2秒延迟消息
    long delayTime3 = 2000; // 2秒
    propsBuilder.expiration(String.valueOf(delayTime3));
    channel.basicPublish("", tempQueueName, propsBuilder.build(), "2秒延迟消息 - 定时任务3".getBytes());
    System.out.println("发送消息: '2秒延迟消息 - 定时任务3' (延迟: " + delayTime3 + "ms)");

    System.out.println("\n所有延迟消息发送完成！");
    System.out.println("注意：消息将在指定的延迟时间后被消费者接收");
    System.out.println("延迟时间短的消息会先被消费");

    // 关闭通道和连接
    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }
}