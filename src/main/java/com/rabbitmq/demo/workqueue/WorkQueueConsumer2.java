package com.rabbitmq.demo.workqueue;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 工作队列模式的消费者2
 * 工作队列模式允许多个消费者共同处理队列中的消息
 * 此消费者模拟较快的处理速度
 */
public class WorkQueueConsumer2 {
    // 工作队列名称
    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取 RabbitMQ 连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明持久化队列（与生产者保持一致）
        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

        /**
         * 设置通道的预取计数
         * 参数为1表示RabbitMQ一次只发送一条消息给消费者
         * 结合手动确认，可以实现基于消费者处理能力的公平分发
         */
        channel.basicQos(1);

        System.out.println("[消费者2] 等待接收消息...");

        /**
         * 消息接收回调函数
         * 模拟较快的处理速度（200毫秒）
         */
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 将消息体转换为字符串
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("[消费者2] 接收到消息: '" + message + "'");

            try {
                // 模拟处理消息的耗时操作（200毫秒）
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("[消费者2] 完成消息处理: '" + message + "'");

                /**
                 * 手动确认消息
                 * 确保消息被正确处理后才从队列中删除
                 * 这样可以避免消费者处理失败时消息丢失
                 */
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        /**
         * 启动消费
         * 设置autoAck为false，启用手动确认机制
         */
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
    }
}