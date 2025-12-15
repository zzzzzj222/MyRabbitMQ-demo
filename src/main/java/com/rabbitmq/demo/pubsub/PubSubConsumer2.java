package com.rabbitmq.demo.pubsub;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 发布订阅模式消费者2
 * 
 * 发布订阅模式（Publish/Subscribe）是RabbitMQ的一种消息传递模式：
 * 1. 生产者将消息发送到交换机（Exchange）
 * 2. 交换机将消息广播到所有与之绑定的队列
 * 3. 每个消费者都有自己的队列，从自己的队列中接收消息
 * 
 * 本示例使用Fanout（扇出）交换机，它会将消息发送到所有绑定的队列
 * 适用于日志记录、事件通知等需要将同一消息发送给多个接收者的场景
 * 
 * 注意：这是第二个独立的消费者实例，与Consumer1一样会接收所有消息
 */
public class PubSubConsumer2 {
    // 交换机名称
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明交换机，类型为fanout（扇出）
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        // 创建临时队列（随机名称，非持久化，独占，自动删除）
        // 发布订阅模式中每个消费者都需要有自己的独立队列
        String queueName = channel.queueDeclare().getQueue();
        // 将队列与交换机绑定
        // Fanout交换机不需要路由键，所以第三个参数为空字符串
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println("[消费者2] 等待接收日志消息...");

        // 消息接收回调函数
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 从消息体中获取消息内容
            String message = new String(delivery.getBody(), "UTF-8");
            // 打印接收到的日志消息
            System.out.println("[消费者2] 接收到日志: '" + message + "'");
        };

        // 消费消息
        // 参数说明：队列名称、自动确认、消息接收回调、消费者取消回调
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}