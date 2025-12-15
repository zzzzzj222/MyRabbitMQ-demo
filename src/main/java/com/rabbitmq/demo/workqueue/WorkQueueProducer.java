package com.rabbitmq.demo.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 工作队列模式的生产者
 * 发送多个任务到工作队列，由多个消费者共同处理
 * 实现了负载均衡和消息持久化
 */
public class WorkQueueProducer {
    // 工作队列名称
    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 获取 RabbitMQ 连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 队列持久化设置
        boolean durable = true;

        /**
         * 声明持久化队列
         * 参数说明：
         * 1. queue: 队列名称
         * 2. durable: true表示持久化，队列会在服务器重启后保留
         * 3. exclusive: 是否排外
         * 4. autoDelete: 是否自动删除
         * 5. arguments: 其他参数
         */
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

        // 发送10个任务到队列
        for (int i = 1; i <= 10; i++) {
            String message = "Task " + i;

            /**
             * 发送消息
             * 注意：要使消息持久化，还需要在basicPublish方法中设置MessageProperties.PERSISTENT_TEXT_PLAIN
             */
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println("[生产者] 发送消息: '" + message + "'");

            // 模拟发送间隔
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 关闭通道和连接
        RabbitMQUtils.closeChannel(channel);
        RabbitMQUtils.closeConnection(connection);
    }
}