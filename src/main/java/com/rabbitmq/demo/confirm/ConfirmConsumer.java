package com.rabbitmq.demo.confirm;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 确认模式消费者
 *
 * 用于验证确认模式生产者发送的消息是否正确接收
 * 这是一个简单的消费者，主要用于演示目的
 */
public class ConfirmConsumer {
    // 队列名称（与生产者使用同一个队列）
    private static final String QUEUE_NAME = "confirm_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明队列（与生产者保持一致）
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        System.out.println("[确认模式消费者] 等待接收消息...");

        // 消息接收回调函数
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 从消息体中获取消息内容
            String message = new String(delivery.getBody(), "UTF-8");
            // 打印接收到的消息
            System.out.println("[确认模式消费者] 接收到消息: '" + message + "'");
        };

        // 消费消息（自动确认模式）
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }
}