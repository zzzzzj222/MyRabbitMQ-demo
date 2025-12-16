package com.rabbitmq.demo.dlx;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 死信队列生产者
 *
 * 死信队列（Dead Letter Queue, DLX）是RabbitMQ的一种特殊队列：
 * 1. 当消息在主队列中变为死信（Dead Letter）时，会被转发到死信队列
 * 2. 消息变为死信的情况：
 * - 消息被拒绝（basic.reject/basic.nack）且requeue=false
 * - 消息过期（TTL过期）
 * - 队列达到最大长度，新消息被丢弃
 *
 * 死信队列配置：
 * 1. 声明死信交换机（DLX）
 * 2. 声明死信队列
 * 3. 将死信队列绑定到死信交换机
 * 4. 声明主队列时设置x-dead-letter-exchange和x-dead-letter-routing-key参数
 */
public class DLXProducer {
    // 主队列名称
    private static final String MAIN_QUEUE_NAME = "main_queue";
    // 死信交换机名称
    private static final String DLX_EXCHANGE_NAME = "dlx_exchange";
    // 死信队列名称
    private static final String DLX_QUEUE_NAME = "dlx_queue";
    // 死信路由键
    private static final String DLX_ROUTING_KEY = "dlx_routing_key";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        System.out.println("=== RabbitMQ 死信队列模式 - 生产者 ===");

        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 1. 声明死信交换机（类型：direct）
        channel.exchangeDeclare(DLX_EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

        // 2. 声明死信队列
        channel.queueDeclare(DLX_QUEUE_NAME, true, false, false, null);

        // 3. 将死信队列绑定到死信交换机
        channel.queueBind(DLX_QUEUE_NAME, DLX_EXCHANGE_NAME, DLX_ROUTING_KEY);

        // 4. 声明主队列参数
        Map<String, Object> mainQueueArgs = new HashMap<>();
        // 设置死信交换机
        mainQueueArgs.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        // 设置死信路由键
        mainQueueArgs.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);
        // 设置队列消息TTL（可选，单位：毫秒）
        mainQueueArgs.put("x-message-ttl", 5000);
        // 设置队列最大长度（可选）
        mainQueueArgs.put("x-max-length", 3);

        // 5. 声明主队列
        channel.queueDeclare(MAIN_QUEUE_NAME, true, false, false, mainQueueArgs);

        System.out.println("死信队列环境已准备完成:");
        System.out.println("- 主队列: " + MAIN_QUEUE_NAME);
        System.out.println("- 死信交换机: " + DLX_EXCHANGE_NAME);
        System.out.println("- 死信队列: " + DLX_QUEUE_NAME);
        System.out.println("- 消息TTL: 5秒");
        System.out.println("- 队列最大长度: 3");
        System.out.println("========================================\n");

        // 消息属性构建器
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        // 设置消息持久化
        propsBuilder.deliveryMode(2);

        // 发送测试消息
        System.out.println("开始发送消息到主队列...");

        // 1. 发送正常消息（会被TTL过期处理）
        propsBuilder.priority(0);
        channel.basicPublish("", MAIN_QUEUE_NAME, propsBuilder.build(), "正常消息 - 会在5秒后过期".getBytes());
        System.out.println("发送消息: '正常消息 - 会在5秒后过期'");
        TimeUnit.SECONDS.sleep(1);

        // 2. 发送将被拒绝的消息（假设消费者会拒绝它）
        propsBuilder.priority(1);
        channel.basicPublish("", MAIN_QUEUE_NAME, propsBuilder.build(), "将被拒绝的消息 - requeue=false".getBytes());
        System.out.println("发送消息: '将被拒绝的消息 - requeue=false'");
        TimeUnit.SECONDS.sleep(1);

        // 3. 发送队列满时的消息（超出队列最大长度）
        for (int i = 1; i <= 3; i++) {
            propsBuilder.priority(2);
            channel.basicPublish("", MAIN_QUEUE_NAME, propsBuilder.build(), ("队列满时的消息 - " + i).getBytes());
            System.out.println("发送消息: '队列满时的消息 - " + i + "'");
            TimeUnit.SECONDS.sleep(1);
        }

        // 4. 发送第4条消息，这将导致队列满，最早的消息会被挤入死信队列
        propsBuilder.priority(3);
        channel.basicPublish("", MAIN_QUEUE_NAME, propsBuilder.build(), "超出队列长度的消息 - 第4条".getBytes());
        System.out.println("发送消息: '超出队列长度的消息 - 第4条'");

        System.out.println("\n所有消息发送完成！");
        System.out.println("- 部分消息会因TTL过期进入死信队列");
        System.out.println("- 部分消息会因队列满进入死信队列");
        System.out.println("- 部分消息会因被拒绝进入死信队列");

        // 关闭通道和连接
        RabbitMQUtils.closeChannel(channel);
        RabbitMQUtils.closeConnection(connection);
    }
}