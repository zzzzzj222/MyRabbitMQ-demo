package com.rabbitmq.demo.routing;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 路由模式消费者1
 * 
 * 路由模式（Routing）是RabbitMQ的一种消息传递模式：
 * 1. 生产者将消息发送到Direct交换机，并指定一个路由键（routing key）
 * 2. 交换机根据消息的路由键，将消息转发到绑定了相同路由键的队列
 * 3. 消费者从队列中接收消息
 * 
 * 本示例使用Direct交换机，消费者1只接收路由键为"error"的消息
 * 适用于需要根据消息类型或重要性进行选择性接收的场景，如日志级别过滤
 */
public class RoutingConsumer1 {
    // 交换机名称
    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明交换机，类型为direct（直接）
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        // 创建临时队列（随机名称，非持久化，独占，自动删除）
        String queueName = channel.queueDeclare().getQueue();

        // 定义要接收的消息严重级别（路由键）
        String severity = "error";
        // 将队列与交换机绑定，并指定路由键
        // Direct交换机根据路由键精确匹配消息
        channel.queueBind(queueName, EXCHANGE_NAME, severity);

        System.out.println("[消费者1] 等待接收消息 (仅error级别)...");

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