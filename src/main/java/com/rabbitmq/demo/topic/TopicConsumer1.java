package com.rabbitmq.demo.topic;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Topic模式消费者1
 * 
 * Topic模式是RabbitMQ中最灵活的消息路由模式，它允许通过通配符匹配路由键
 * 通配符规则：
 * - * ：匹配一个单词
 * - # ：匹配零个或多个单词
 * 
 * 本消费者使用绑定模式 *.orange.*，表示接收：
 * - 中间单词为 "orange" 的所有路由键
 * - 例如：quick.orange.rabbit, lazy.orange.fox 等
 */
public class TopicConsumer1 {
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

        // 定义绑定模式（路由键匹配规则）
        String bindingPattern = "*.orange.*";
        // 将队列与交换机绑定，并指定匹配模式
        channel.queueBind(queueName, EXCHANGE_NAME, bindingPattern);

        System.out.println("[消费者1] 等待接收消息 (模式: '" + bindingPattern + "')...");

        // 消息接收回调函数
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 从消息体中获取消息内容
            String message = new String(delivery.getBody(), "UTF-8");
            // 获取消息的路由键
            String routingKey = delivery.getEnvelope().getRoutingKey();
            // 打印接收到的消息
            System.out.println("[消费者1] 接收到消息: '" + message + "' (路由键: '" + routingKey + "')");
        };

        // 消费消息
        // 参数说明：队列名称、自动确认、消息接收回调、消费者取消回调
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }
}