package com.rabbitmq.demo.quorum;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 仲裁队列生产者
 *
 * 仲裁队列（Quorum Queue）是RabbitMQ 3.8版本引入的一种新的高可用队列类型：
 * 1. 基于Raft一致性算法实现，提供强一致性保证
 * 2. 自动在集群节点间复制消息，确保数据不会丢失
 * 3. 支持消息持久化和发布者确认机制
 * 4. 推荐用于需要高可用性和强一致性的场景
 *
 * 核心特点：
 * - 使用Raft协议在多个节点间复制消息
 * - 至少需要3个节点的集群环境才能发挥最佳效果
 * - 提供更强的一致性保证，避免了镜像队列的脑裂问题
 * - 支持类似镜像队列的功能，但性能更优
 */
public class QuorumQueueProducer {
    // 队列名称
    private static final String QUEUE_NAME = "quorum_queue_example";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        System.out.println("=== RabbitMQ 仲裁队列模式演示 ===");

        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 定义队列参数
        Map<String, Object> queueArgs = new HashMap<>();
        // 声明仲裁队列类型
        queueArgs.put("x-queue-type", "quorum");

        // 声明仲裁队列
        // 参数说明：
        // 1. 队列名称
        // 2. durable: true - 队列持久化，消息会写入磁盘
        // 3. exclusive: false - 非排他队列，所有连接都可以访问
        // 4. autoDelete: false - 非自动删除，不会因为消费者断开而删除
        // 5. arguments: 设置x-queue-type为quorum，声明为仲裁队列
        //
        // 仲裁队列实现原理：
        // 1. 基于Raft一致性算法，确保消息在多个节点间复制
        // 2. 每个仲裁队列有一个主节点(leader)和多个副本节点(follower)
        // 3. 消息发送到主节点后，主节点会通过Raft协议同步到副本节点
        // 4. 当大多数节点(超过半数)确认收到消息后，才会向生产者发送确认
        // 5. 这种机制确保了即使某个节点故障，消息也不会丢失
        channel.queueDeclare(QUEUE_NAME, true, false, false, queueArgs);

        System.out.println("仲裁队列生产者已启动");
        System.out.println("队列类型：仲裁队列 (x-queue-type=quorum)");
        System.out.println("注意：建议在3个或更多节点的RabbitMQ集群中测试");
        System.out.println("========================================\n");

        // 消息发送
        System.out.println("开始发送消息到仲裁队列...");

        for (int i = 1; i <= 10; i++) {
            String message = "高可用消息 - " + i;

            // 发送消息到队列
            // 使用默认交换机("")，路由键为队列名称
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

            System.out.println("发送消息: '" + message + "'");

            // 模拟发送延迟
            TimeUnit.MILLISECONDS.sleep(500);
        }

        System.out.println("\n所有消息发送完成！");
        System.out.println("仲裁队列会自动在集群节点间复制消息");
        System.out.println("即使某个节点故障，消息也不会丢失");

        // 关闭通道和连接
        RabbitMQUtils.closeChannel(channel);
        RabbitMQUtils.closeConnection(connection);
    }
}