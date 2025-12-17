package com.rabbitmq.demo.mirror;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 镜像队列消费者
 *
 * 镜像队列消费者特点：
 * 1. 可以连接到集群中的任何节点消费消息
 * 2. 无论连接到哪个节点，都会从主队列接收消息
 * 3. 当主节点故障时，消费者会自动连接到新的主节点
 * 4. 不需要在代码中特殊配置，只需要确保队列名称与策略匹配
 *
 * 高可用性测试建议：
 * 1. 启动消费者和生产者
 * 2. 生产者发送消息
 * 3. 手动关闭主节点
 * 4. 观察消费者是否继续正常接收消息
 *
 * 注意事项：
 * - 镜像队列会增加网络流量和存储开销
 * - 建议只对关键队列使用镜像队列
 * - 镜像队列的配置通过RabbitMQ集群策略管理，而不是代码
 */
public class MirrorQueueConsumer {
    // 镜像队列名称（需要与生产者一致，以ha开头）
    private static final String MIRROR_QUEUE_NAME = "ha_mirror_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("=== RabbitMQ 镜像队列模式 - 消费者 ===");

        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明镜像队列（与生产者一致）
        // 注意：镜像队列的配置是通过集群策略实现的，代码中不需要特殊配置
        channel.queueDeclare(MIRROR_QUEUE_NAME, true, false, false, null);

        // 设置手动确认模式和公平分发
        channel.basicQos(1);

        System.out.println("镜像队列消费者已启动");
        System.out.println("注意：已连接到RabbitMQ集群，支持高可用性");
        System.out.println("当主节点故障时，会自动切换到备用节点");
        System.out.println("========================================\n");

        // 定义镜像队列消费者
        Consumer consumer = new DefaultConsumer(channel) {
            /**
             * 处理RabbitMQ消息投递的回调方法
             * 
             * @param consumerTag 消费者标签，用于标识消费者
             * @param envelope    消息信封，包含消息的元数据信息
             * @param properties  消息属性，包含消息的附加属性
             * @param body        消息体内容，以字节数组形式存储
             * @throws IOException 当IO操作出现异常时抛出
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                long deliveryTag = envelope.getDeliveryTag();

                try {
                    // 打印接收到的消息信息
                    System.out.println("[镜像队列] 接收消息: '" + message + "'");
                    System.out.println("[镜像队列] 队列名称: " + MIRROR_QUEUE_NAME);
                    System.out.println("[镜像队列] 路由键: " + envelope.getRoutingKey());

                    // 模拟消息处理过程，暂停2秒
                    TimeUnit.SECONDS.sleep(2);

                    // 打印消息处理完成信息
                    System.out.println("[镜像队列] 消息处理完成: '" + message + "'");
                    System.out.println("----------------------------------------");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 确认消息已被成功处理，从队列中移除
                channel.basicAck(deliveryTag, false);
            }
        };

        // 启动消费者，关闭自动确认
        channel.basicConsume(MIRROR_QUEUE_NAME, false, consumer);

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