package com.rabbitmq.demo.headers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Headers交换机生产者
 * <p>
 * Headers交换机是RabbitMQ的一种高级路由机制：
 * 1. 它忽略路由键（Routing Key）
 * 2. 根据消息头（Message Headers）进行消息路由
 * 3. 支持两种匹配模式：
 * - "x-match=all": 所有指定的头信息都必须匹配
 * - "x-match=any": 只要有一个指定的头信息匹配即可
 * <p>
 * 适用场景：需要基于多个属性进行复杂路由决策的情况
 */
public class HeadersProducer {
    // 交换机名称
    private static final String EXCHANGE_NAME = "headers_exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明交换机，类型为headers
        String exchangeType = "headers";
        channel.exchangeDeclare(EXCHANGE_NAME, exchangeType);

        // 准备不同类型的消息头
        // 1. 订单相关的消息头
        Map<String, Object> orderHeaders = new HashMap<>();
        orderHeaders.put("type", "order");
        orderHeaders.put("priority", "high");
        orderHeaders.put("region", "east");

        // 2. 用户相关的消息头
        Map<String, Object> userHeaders = new HashMap<>();
        userHeaders.put("type", "user");
        userHeaders.put("priority", "medium");
        userHeaders.put("region", "west");

        // 3. 日志相关的消息头
        Map<String, Object> logHeaders = new HashMap<>();
        logHeaders.put("type", "log");
        logHeaders.put("priority", "low");
        logHeaders.put("level", "info");

        // 创建消息属性
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();

        // 发送订单消息
        propsBuilder.headers(orderHeaders);
        String orderMessage = "新订单：#1001 - 购买笔记本电脑";
        // 发送消息到headers交换机，注意路由键为空字符串（headers交换机忽略路由键）
        /*
        propsBuilder.build(): 构建消息属性对象，包含消息的元数据
        orderMessage.getBytes(): 将订单消息字符串转换为字节数组进行传输
         */
        channel.basicPublish(EXCHANGE_NAME, "", propsBuilder.build(), orderMessage.getBytes());
        System.out.println("[生产者] 发送订单消息: '" + orderMessage + "'");

        // 发送用户消息
        propsBuilder.headers(userHeaders);
        String userMessage = "用户操作：zhangsan - 更新个人资料";
        channel.basicPublish(EXCHANGE_NAME, "", propsBuilder.build(), userMessage.getBytes());
        System.out.println("[生产者] 发送用户消息: '" + userMessage + "'");

        // 发送日志消息
        propsBuilder.headers(logHeaders);
        String logMessage = "系统日志：2024-01-15 14:30:00 - 数据库连接成功";
        channel.basicPublish(EXCHANGE_NAME, "", propsBuilder.build(), logMessage.getBytes());
        System.out.println("[生产者] 发送日志消息: '" + logMessage + "'");

        // 关闭通道和连接
        RabbitMQUtils.closeChannel(channel);
        RabbitMQUtils.closeConnection(connection);
    }
}