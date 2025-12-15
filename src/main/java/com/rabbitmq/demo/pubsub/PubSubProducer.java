package com.rabbitmq.demo.pubsub;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 发布订阅模式生产者
 * 
 * 发布订阅模式（Publish/Subscribe）是RabbitMQ的一种消息传递模式：
 * 1. 生产者将消息发送到交换机（Exchange）
 * 2. 交换机将消息广播到所有与之绑定的队列
 * 3. 每个消费者都有自己的队列，从自己的队列中接收消息
 * 
 * 本示例使用Fanout（扇出）交换机，它会将消息发送到所有绑定的队列
 * 适用于日志记录、事件通知等需要将同一消息发送给多个接收者的场景
 */
public class PubSubProducer {
    // 交换机名称
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明交换机类型为fanout（扇出）
        String exchangeType = "fanout";
        channel.exchangeDeclare(EXCHANGE_NAME, exchangeType);

        // 循环发送5条日志消息
        for (int i = 1; i <= 5; i++) {
            // 构建日志消息
            String message = "Log message " + i;
            // 发送消息到交换机
            // 参数说明：交换机名称、路由键（Fanout交换机不需要路由键）、消息属性、消息体
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
            // 打印发送信息
            System.out.println("[生产者] 发送日志: '" + message + "'");
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