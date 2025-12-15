package com.rabbitmq.demo.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 路由模式生产者
 * 
 * 路由模式（Routing Pattern）是RabbitMQ的一种消息传递模式：
 * 1. 生产者将消息发送到交换机（Exchange），并指定路由键（Routing Key）
 * 2. 交换机根据路由键将消息路由到与之精确匹配的队列
 * 3. 消费者从自己的队列中接收消息
 * 
 * 本示例使用Direct（直接）交换机，它会根据精确的路由键匹配规则来转发消息
 * 适用于需要根据消息类型或优先级进行分类处理的场景，如日志级别过滤
 */
public class RoutingProducer {
  private static final String EXCHANGE_NAME = "direct_logs";

  public static void main(String[] args) throws IOException, TimeoutException {
    // 获取RabbitMQ连接
    Connection connection = RabbitMQUtils.getConnection();
    // 创建通道
    Channel channel = connection.createChannel();

    // 声明交换机类型为direct（直接）
    String exchangeType = "direct";
    channel.exchangeDeclare(EXCHANGE_NAME, exchangeType);

    // 定义不同级别的日志消息
    String[] logMessages = {
        "系统启动成功", // info级别
        "用户登录", // info级别
        "磁盘空间不足", // warning级别
        "网络连接失败", // error级别
        "数据备份完成" // info级别
    };

    // 定义对应的路由键（与日志级别一一对应）
    String[] routingKeys = {
        "info", // 信息日志
        "info", // 信息日志
        "warning", // 警告日志
        "error", // 错误日志
        "info" // 信息日志
    };

    // 循环发送不同级别的日志消息
    for (int i = 0; i < logMessages.length; i++) {
      String message = logMessages[i];
      String routingKey = routingKeys[i];

      // 发送消息到交换机，并指定路由键
      // 参数说明：交换机名称、路由键、消息属性、消息体
      channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
      System.out.println("[生产者] 发送消息: '" + message + "' (路由键: '" + routingKey + "')");

      // 等待1秒，方便观察发送过程
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // 关闭通道和连接
    RabbitMQUtils.closeChannel(channel);
    RabbitMQUtils.closeConnection(connection);
  }
}