package com.rabbitmq.demo.basic;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 基础的消息消费者
 * 实现了最简单的消息传递模式：一个消费者从队列接收消息
 */
public class BasicConsumer {
    // 队列名称，必须与生产者使用的队列名称一致
    private static final String QUEUE_NAME = "hello_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取 RabbitMQ 连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        /**
         * 声明队列
         * 注意：这里再次声明队列是为了确保队列存在，避免消费者先启动时队列不存在的情况
         */
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        /**
         * 消息接收回调函数
         * 当接收到消息时会调用此函数
         */
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 将消息体转换为字符串
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("[消费者] 接收消息: '" + message + "'");
        };

        /**
         * 启动消费
         * 参数说明：
         * 1. queue: 队列名称
         * 2. autoAck: 是否自动确认消息
         * 3. deliverCallback: 消息接收回调函数
         * 4. cancelCallback: 取消消费回调函数
         */
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            // 消费被取消时的处理逻辑
            System.out.println("[消费者] 消费被取消: " + consumerTag);
        });

        System.out.println("[消费者] 等待接收消息...");
        System.out.println("按 Ctrl+C 退出");
    }
}