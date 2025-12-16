package com.rabbitmq.demo.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 确认模式生产者
 *
 * 发布者确认（Publisher Confirms）是RabbitMQ的一种可靠消息传递机制：
 * 1. 生产者将消息发送给RabbitMQ
 * 2. RabbitMQ接收并处理消息（如路由到队列）
 * 3. RabbitMQ向生产者发送确认（Ack）或否定确认（Nack）
 * 4. 生产者根据确认结果判断消息是否成功投递
 *
 * 本示例演示了三种确认模式：
 * 1. 单条确认（Simple Confirm）
 * 2. 批量确认（Batch Confirm）
 * 3. 异步确认（Asynchronous Confirm）
 */
public class ConfirmPublisher {
  // 队列名称
  private static final String QUEUE_NAME = "confirm_queue";
  // 发送消息数量
  private static final int MESSAGE_COUNT = 1000;

  public static void main(String[] args) throws IOException, TimeoutException {
    System.out.println("=== RabbitMQ 发布者确认模式演示 ===");

    // 演示单条确认模式
    System.out.println("\n1. 演示单条确认模式...");
    singleConfirm();

    // 演示批量确认模式
    System.out.println("\n2. 演示批量确认模式...");
    batchConfirm();

    // 演示异步确认模式
    System.out.println("\n3. 演示异步确认模式...");
    asyncConfirm();
  }

  /**
   * 单条确认模式
   * - 每条消息发送后立即调用waitForConfirms()等待确认
   * - 简单可靠，但性能较差
   */
  public static void singleConfirm() throws IOException, TimeoutException {
    Connection connection = RabbitMQUtils.getConnection();
    Channel channel = connection.createChannel();

    // 声明队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 开启发布者确认模式
    channel.confirmSelect();

    long startTime = System.nanoTime();

    for (int i = 0; i < MESSAGE_COUNT; i++) {
      String message = "单条确认消息 - " + i;
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

      // 等待单条消息的确认
      try {
        boolean confirmed = channel.waitForConfirms();
        if (confirmed) {
          // System.out.println("消息 " + i + " 确认成功");
        } else {
          System.out.println("消息 " + i + " 确认失败");
          // 处理确认失败的情况
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
        // 处理中断异常
      }
    }

    long endTime = System.nanoTime();
    System.out.printf("发送 %d 条消息耗时: %.2f 毫秒\n",
        MESSAGE_COUNT, (endTime - startTime) / 1000000.0);

    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }

  /**
   * 批量确认模式
   * - 累积一定数量的消息后，调用waitForConfirms()等待批量确认
   * - 性能比单条确认好，但如果其中一条失败，无法确定具体是哪条
   */
  public static void batchConfirm() throws IOException, TimeoutException {
    Connection connection = RabbitMQUtils.getConnection();
    Channel channel = connection.createChannel();

    // 声明队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 开启发布者确认模式
    channel.confirmSelect();

    long startTime = System.nanoTime();

    // 批量大小
    int batchSize = 100;

    for (int i = 0; i < MESSAGE_COUNT; i++) {
      String message = "批量确认消息 - " + i;
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

      // 达到批量大小，等待确认
      if ((i + 1) % batchSize == 0) {
        try {
          channel.waitForConfirms();
          // System.out.println("批量消息 " + (i - batchSize + 1) + "-" + i + " 确认成功");
        } catch (InterruptedException e) {
          e.printStackTrace();
          // 处理中断异常
        }
      }
    }

    // 处理最后一批不足batchSize的消息
    try {
      channel.waitForConfirms();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = System.nanoTime();
    System.out.printf("发送 %d 条消息耗时: %.2f 毫秒\n",
        MESSAGE_COUNT, (endTime - startTime) / 1000000.0);

    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }

  /**
   * 异步确认模式
   * - 发送消息后不阻塞，通过回调函数处理确认结果
   * - 性能最好，且可以精确定位到失败的消息
   */
  public static void asyncConfirm() throws IOException, TimeoutException {
    Connection connection = RabbitMQUtils.getConnection();
    Channel channel = connection.createChannel();

    // 声明队列
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // 开启发布者确认模式
    channel.confirmSelect();

    // 用于跟踪未确认的消息
    final SortedSet<Long> unconfirmedSet = Collections.synchronizedSortedSet(new TreeSet<>());

    // 确认回调函数
    ConfirmCallback ackCallback = (deliveryTag, multiple) -> {
      if (multiple) {
        // 批量确认，移除所有小于等于deliveryTag的消息
        unconfirmedSet.headSet(deliveryTag + 1).clear();
      } else {
        // 单条确认，移除指定的消息
        unconfirmedSet.remove(deliveryTag);
      }
      // System.out.println("确认消息，deliveryTag: " + deliveryTag + ", multiple: " +
      // multiple);
    };

    // 否定确认回调函数
    ConfirmCallback nackCallback = (deliveryTag, multiple) -> {
      String message = multiple
          ? "批量消息确认失败，deliveryTag: " + deliveryTag
          : "单条消息确认失败，deliveryTag: " + deliveryTag;
      System.err.println(message);

      // 处理确认失败的消息
      if (multiple) {
        unconfirmedSet.headSet(deliveryTag + 1).clear();
      } else {
        unconfirmedSet.remove(deliveryTag);
      }
      // 可以在这里添加消息重试逻辑
    };

    // 注册确认回调
    channel.addConfirmListener(ackCallback, nackCallback);

    long startTime = System.nanoTime();

    for (int i = 0; i < MESSAGE_COUNT; i++) {
      long deliveryTag = channel.getNextPublishSeqNo();
      String message = "异步确认消息 - " + i;

      // 发送消息
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

      // 将消息添加到未确认集合
      unconfirmedSet.add(deliveryTag);
    }

    // 等待所有消息确认
    while (!unconfirmedSet.isEmpty()) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    long endTime = System.nanoTime();
    System.out.printf("发送 %d 条消息耗时: %.2f 毫秒\n",
        MESSAGE_COUNT, (endTime - startTime) / 1000000.0);

    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }
}