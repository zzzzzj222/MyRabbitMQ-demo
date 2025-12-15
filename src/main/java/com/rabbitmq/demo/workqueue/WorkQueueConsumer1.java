package com.rabbitmq.demo.workqueue;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 工作队列模式的消费者1
 * 工作队列模式允许多个消费者共同处理队列中的消息
 * 此消费者模拟较慢的处理速度
 */
public class WorkQueueConsumer1 {
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
         * 消费者处理完一条消息并确认后，才会发送下一条消息
         * 这样可以实现公平分发，避免消息堆积
         */
        channel.basicQos(1);

        System.out.println("[消费者1] 等待接收消息...");

        /**
         * 消息接收回调函数
         * 模拟较慢的处理速度（1秒）
         */
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 将消息体转换为字符串
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("[消费者1] 接收到消息: '" + message + "'");

            try {
                // 模拟处理消息的耗时操作（1秒）
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("[消费者1] 完成消息处理: '" + message + "'");

                /**
                 * 手动确认消息
                 * 参数说明：
                 * 1. deliveryTag: 消息的标签
                 * 2. multiple: 是否批量确认
                 * 手动确认可以确保消息被正确处理后才从队列中删除
                 */
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        /**
         * 启动消费
         * 参数说明：
         * 1. queue: 队列名称
         * 2. autoAck: false表示手动确认消息
         * 3. deliverCallback: 消息接收回调函数
         * 4. cancelCallback: 取消消费回调函数
         */
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
    }
}