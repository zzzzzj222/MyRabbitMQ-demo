package com.rabbitmq.demo.headers;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Headers交换机消费者1（使用"all"匹配模式）
 *
 * 本消费者使用"x-match=all"匹配模式：
 * - 只有当消息的所有指定头信息都匹配时，才会接收消息
 * - 演示了如何精确匹配多个头信息条件
 */
public class HeadersConsumer1 {
    // 交换机名称
    private static final String EXCHANGE_NAME = "headers_exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明交换机，类型为headers
        channel.exchangeDeclare(EXCHANGE_NAME, "headers");
        // 创建临时队列（随机名称，非持久化，独占，自动删除）
        String queueName = channel.queueDeclare().getQueue();

        // 定义头信息匹配规则
        Map<String, Object> headers = new HashMap<>();
        // 设置匹配模式为"all"：所有头信息都必须匹配
        headers.put("x-match", "all");
        // 指定要匹配的头信息
        headers.put("type", "order");
        headers.put("priority", "high");

        // 将队列与交换机绑定，并设置头信息匹配规则
        // 注意：headers交换机的路由键参数被忽略，所以第三个参数为空字符串
        channel.queueBind(queueName, EXCHANGE_NAME, "", headers);

        System.out.println("[消费者1] 等待接收消息 (使用'x-match=all'匹配所有头信息)...");
        System.out.println("匹配条件: type='order' AND priority='high'");

        // 消息接收回调函数
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 从消息体中获取消息内容
            String message = new String(delivery.getBody(), "UTF-8");
            // 获取消息头信息
            Map<String, Object> messageHeaders = delivery.getProperties().getHeaders();
            
            // 打印接收到的消息和头信息
            System.out.println("[消费者1] 接收到消息: '" + message + "'");
            System.out.println("[消费者1] 消息头信息: " + messageHeaders);
        };

        // 消费消息
        // 参数说明：队列名称、自动确认、消息接收回调、消费者取消回调
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }
}