package com.rabbitmq.demo.priority;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 优先级队列生产者
 *
 * 优先级队列（Priority Queue）是RabbitMQ的一种特殊队列：
 * 1. 可以为消息设置优先级（0-255，默认0-9）
 * 2. 高优先级的消息会被优先消费
 * 3. 队列声明时需要设置x-max-priority参数
 *
 * 注意事项：
 * - 消费者必须有一定的处理延迟，否则优先级效果不明显
 * - 队列必须有足够的背压（有未消费的消息）才能体现优先级
 */
public class PriorityProducer {
  // 队列名称
  private static final String QUEUE_NAME = "priority_queue";
  // 最大优先级（0-255，建议使用0-9以获得最佳性能）
  private static final int MAX_PRIORITY = 10;

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    System.out.println("=== RabbitMQ 优先级队列模式演示 ===");

    // 获取RabbitMQ连接
    Connection connection = RabbitMQUtils.getConnection();
    // 创建通道
    Channel channel = connection.createChannel();

    // 定义队列参数
    Map<String, Object> queueArgs = new HashMap<>();
    // 设置队列的最大优先级
    queueArgs.put("x-max-priority", MAX_PRIORITY);

    // 声明优先级队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, queueArgs);

    // 消息属性构建器
    AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();

    // 发送不同优先级的消息（注意：消息发送顺序与消费顺序无关）
    System.out.println("开始发送不同优先级的消息...");

    // 1. 发送低优先级消息（优先级1）
    propsBuilder.priority(1);
    channel.basicPublish("", QUEUE_NAME, propsBuilder.build(), "低优先级消息 - 普通任务".getBytes());
    System.out.println("发送消息: '低优先级消息 - 普通任务' (优先级: 1)");
    TimeUnit.SECONDS.sleep(1);

    // 2. 发送中优先级消息（优先级5）
    propsBuilder.priority(5);
    channel.basicPublish("", QUEUE_NAME, propsBuilder.build(), "中优先级消息 - 重要任务".getBytes());
    System.out.println("发送消息: '中优先级消息 - 重要任务' (优先级: 5)");
    TimeUnit.SECONDS.sleep(1);

    // 3. 发送高优先级消息（优先级10）
    propsBuilder.priority(10);
    channel.basicPublish("", QUEUE_NAME, propsBuilder.build(), "高优先级消息 - 紧急任务".getBytes());
    System.out.println("发送消息: '高优先级消息 - 紧急任务' (优先级: 10)");
    TimeUnit.SECONDS.sleep(1);

    // 4. 再发送一个低优先级消息
    propsBuilder.priority(1);
    channel.basicPublish("", QUEUE_NAME, propsBuilder.build(), "低优先级消息 - 普通任务2".getBytes());
    System.out.println("发送消息: '低优先级消息 - 普通任务2' (优先级: 1)");
    TimeUnit.SECONDS.sleep(1);

    // 5. 再发送一个高优先级消息
    propsBuilder.priority(9);
    channel.basicPublish("", QUEUE_NAME, propsBuilder.build(), "高优先级消息 - 次紧急任务".getBytes());
    System.out.println("发送消息: '高优先级消息 - 次紧急任务' (优先级: 9)");

    System.out.println("\n所有消息发送完成！");
    System.out.println("消费者将按优先级顺序处理消息（高优先级先处理）");

    // 关闭通道和连接
    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }
}