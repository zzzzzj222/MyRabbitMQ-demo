package com.rabbitmq.demo.quorum;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 仲裁队列消费者
 *
 * 仲裁队列消费者特点：
 * 1. 与普通队列消费者API基本相同
 * 2. 支持手动确认机制，确保消息被正确处理
 * 3. 即使在节点故障切换时，也能继续消费消息
 * 4. 支持消息的公平分发机制
 */
public class QuorumQueueConsumer {
    // 队列名称（必须与生产者使用的队列名称一致）
    private static final String QUEUE_NAME = "quorum_queue_example";

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("=== RabbitMQ 仲裁队列消费者演示 ===");

        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 定义队列参数（与生产者完全一致）
        Map<String, Object> queueArgs = new HashMap<>();
        // 声明仲裁队列类型
        queueArgs.put("x-queue-type", "quorum");

        // 声明仲裁队列
        // 注意：消费者也需要声明队列，确保队列存在
        // 仲裁队列在消费端的特点：
        // 1. 消费者始终连接到队列的主节点
        // 2. 主节点负责将消息分发给消费者
        // 3. 当主节点故障时，Raft算法会选举新的主节点
        // 4. 消费者会自动重连到新的主节点，继续消费
        // 5. 消息的投递顺序得到保证
        channel.queueDeclare(QUEUE_NAME, true, false, false, queueArgs);

        System.out.println("仲裁队列消费者已启动");
        System.out.println("等待接收消息...");
        System.out.println("按Ctrl+C退出");
        System.out.println("========================================\n");

        // 配置消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                // 接收到的消息
                String message = new String(body, "UTF-8");

                // 获取消息的投递标签
                long deliveryTag = envelope.getDeliveryTag();

                // 模拟消息处理延迟
                try {
                    System.out.println("接收到消息: '" + message + "'");
                    System.out.println("投递标签: " + deliveryTag);
                    System.out.println("消息路由键: " + envelope.getRoutingKey());
                    System.out.println("正在处理消息...");

                    // 模拟处理时间
                    TimeUnit.SECONDS.sleep(1);

                    System.out.println("消息处理完成: '" + message + "'");
                    System.out.println("----------------------------------------");

                    // 手动确认消息已处理完成
                    // 第二个参数multiple设置为false，表示只确认当前消息
                    channel.basicAck(deliveryTag, false);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    // 处理中断异常，拒绝消息并将其放回队列
                    channel.basicNack(deliveryTag, false, true);
                    System.out.println("消息处理失败，已放回队列: '" + message + "'");
                }
            }
        };

        // 设置消费者的QoS（服务质量）参数
        // 1: 每次只接收一条消息，处理完成后再接收下一条
        // 这样可以实现公平分发，避免消息堆积在单个消费者
        channel.basicQos(1);

        // 注册消费者到队列
        // 第三个参数autoAck设置为false，表示手动确认消息
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // 保持消费者运行，等待消息
        System.out.println("消费者正在运行，等待消息...");
        System.out.println("按Ctrl+C停止消费者");

        // 注意：实际应用中不需要下面的代码，这里只是为了演示让程序持续运行
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 关闭通道和连接
        RabbitMQUtils.closeChannel(channel);
        RabbitMQUtils.closeConnection(connection);
    }
}