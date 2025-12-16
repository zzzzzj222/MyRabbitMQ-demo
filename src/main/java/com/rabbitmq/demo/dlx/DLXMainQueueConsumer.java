package com.rabbitmq.demo.dlx;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 死信队列 - 主队列消费者
 *
 * 主队列消费者负责：
 * 1. 处理主队列中的正常消息
 * 2. 演示如何将消息标记为死信（拒绝消息且requeue=false）
 * 3. 注意：需要手动确认消息，否则消息会一直存在于队列中
 */
public class DLXMainQueueConsumer {
    // 主队列名称
    private static final String MAIN_QUEUE_NAME = "main_queue";
    // 死信交换机名称
    private static final String DLX_EXCHANGE_NAME = "dlx_exchange";
    // 死信队列名称
    private static final String DLX_QUEUE_NAME = "dlx_queue";
    // 死信路由键
    private static final String DLX_ROUTING_KEY = "dlx_routing_key";

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("=== RabbitMQ 死信队列模式 - 主队列消费者 ===");

        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明死信交换机（与生产者一致）
        channel.exchangeDeclare(DLX_EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

        // 声明死信队列（与生产者一致）
        channel.queueDeclare(DLX_QUEUE_NAME, true, false, false, null);

        // 将死信队列绑定到死信交换机（与生产者一致）
        channel.queueBind(DLX_QUEUE_NAME, DLX_EXCHANGE_NAME, DLX_ROUTING_KEY);

        // 声明主队列参数（与生产者一致）
        Map<String, Object> mainQueueArgs = new HashMap<>();
        mainQueueArgs.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        mainQueueArgs.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);
        mainQueueArgs.put("x-message-ttl", 5000);
        mainQueueArgs.put("x-max-length", 3);

        // 声明主队列
        channel.queueDeclare(MAIN_QUEUE_NAME, true, false, false, mainQueueArgs);

        // 设置手动确认模式
        channel.basicQos(1); // 一次只处理一条消息

        System.out.println("主队列消费者已启动，等待接收消息...");
        System.out.println("注意：部分消息会被拒绝并转发到死信队列");
        System.out.println("========================================\n");

        // 定义主队列消费者
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                long deliveryTag = envelope.getDeliveryTag();

                try {
                    System.out.println("[主队列] 接收消息: '" + message + "'");

                    // 模拟消息处理
                    TimeUnit.SECONDS.sleep(2);

                    // 演示：将包含"拒绝"关键词的消息标记为死信
                    if (message.contains("拒绝")) {
                        System.out.println("[主队列] 拒绝消息并转发到死信队列: '" + message + "'");
                        // 拒绝消息且不重新入队（requeue=false）
                        channel.basicReject(deliveryTag, false);
                    } else {
                        System.out.println("[主队列] 处理完成: '" + message + "'");
                        // 正常确认消息
                        channel.basicAck(deliveryTag, false);
                    }
                    System.out.println("----------------------------------------");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // 处理异常时拒绝消息
                    channel.basicReject(deliveryTag, false);
                }
            }
        };

        // 启动消费者，关闭自动确认
        channel.basicConsume(MAIN_QUEUE_NAME, false, consumer);

        // 保持程序运行
        System.out.println("按 Ctrl+C 结束程序...");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭通道和连接
            RabbitMQUtils.closeChannel(channel);
            RabbitMQUtils.closeConnection(connection);
        }
    }
}