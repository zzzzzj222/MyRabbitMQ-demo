package com.rabbitmq.demo.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Topic模式生产者
 * 
 * Topic模式是RabbitMQ中最灵活的消息路由模式，它允许通过通配符匹配路由键
 * 通配符规则：
 * - * ：匹配一个单词
 * - # ：匹配零个或多个单词
 * 
 * 本生产者向topic交换机发送带有不同路由键的消息，演示不同路由键如何被消费者匹配
 */
public class TopicProducer {
    // 交换机名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明交换机类型为topic
        String exchangeType = "topic";
        channel.exchangeDeclare(EXCHANGE_NAME, exchangeType);

        // 准备要发送的消息
        String[] messages = {
            "快速的橙色兔子",
            "懒惰的橙色大象",
            "快速的橙色狐狸",
            "懒惰的棕色狐狸",
            "快速的棕色狐狸",
            "快速的橙色雄性兔子",
            "懒惰的橙色雄性兔子"
        };

        // 为每条消息定义对应的路由键
        // 路由键格式：速度.颜色.动物类型 或 速度.颜色.性别.动物类型
        String[] routingKeys = {
            "quick.orange.rabbit",      // 快速的橙色兔子
            "lazy.orange.elephant",     // 懒惰的橙色大象
            "quick.orange.fox",         // 快速的橙色狐狸
            "lazy.brown.fox",           // 懒惰的棕色狐狸
            "quick.brown.fox",          // 快速的棕色狐狸
            "quick.orange.male.rabbit", // 快速的橙色雄性兔子
            "lazy.orange.male.rabbit"   // 懒惰的橙色雄性兔子
        };

        // 循环发送每条消息
        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            String routingKey = routingKeys[i];
            
            // 发送消息到交换机
            // 参数说明：交换机名称、路由键、消息属性、消息体
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
            
            // 打印发送信息
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