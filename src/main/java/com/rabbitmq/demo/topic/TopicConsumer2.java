package com.rabbitmq.demo.topic;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Topic模式消费者2
 * 
 * Topic模式允许通过通配符匹配路由键
 * 通配符规则：
 * - * ：匹配一个单词
 * - # ：匹配零个或多个单词
 * 
 * 本消费者使用两个绑定模式：
 * 1. *.*.rabbit ：
 *    - 第三个单词为 "rabbit" 的所有路由键
 *    - 例如：quick.orange.rabbit, lazy.brown.rabbit 等
 * 2. lazy.# ：
 *    - 以 "lazy" 开头的所有路由键
 *    - 例如：lazy.orange, lazy.brown.fox, lazy 等
 */
public class TopicConsumer2 {
    // 交换机名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明交换机，类型为topic
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        // 创建临时队列（随机名称，非持久化，独占，自动删除）
        String queueName = channel.queueDeclare().getQueue();

        // 定义多个绑定模式（路由键匹配规则）
        String[] bindingPatterns = {"*.*.rabbit", "lazy.#"};
        // 将队列与交换机绑定，应用所有绑定模式
        for (String pattern : bindingPatterns) {
            channel.queueBind(queueName, EXCHANGE_NAME, pattern);
        }

        System.out.println("[消费者2] 等待接收消息 (模式: '" + String.join(", ", bindingPatterns) + "')...");

        // 消息接收回调函数
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 从消息体中获取消息内容
            String message = new String(delivery.getBody(), "UTF-8");
            // 获取消息的路由键
            String routingKey = delivery.getEnvelope().getRoutingKey();
            // 打印接收到的消息
            System.out.println("[消费者2] 接收到消息: '" + message + "' (路由键: '" + routingKey + "')");
        };

        // 消费消息
        // 参数说明：队列名称、自动确认、消息接收回调、消费者取消回调
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }
}