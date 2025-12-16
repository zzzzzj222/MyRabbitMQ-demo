package com.rabbitmq.demo.priority;

import com.rabbitmq.client.*;
import com.rabbitmq.demo.utils.RabbitMQUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 优先级队列消费者
 *
 * 优先级队列消费者特点：
 * 1. 需要声明与生产者相同的队列参数（特别是x-max-priority）
 * 2. 高优先级的消息会被优先分发给消费者
 * 3. 注意：消费者需要有处理延迟才能明显看到优先级效果
 */
public class PriorityConsumer {
    // 队列名称（必须与生产者一致）
    private static final String QUEUE_NAME = "priority_queue";
    // 最大优先级（必须与生产者一致）
    private static final int MAX_PRIORITY = 10;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        System.out.println("=== RabbitMQ 优先级队列模式 - 消费者 ===");

        // 获取RabbitMQ连接
        Connection connection = RabbitMQUtils.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 定义队列参数（必须与生产者一致）
        Map<String, Object> queueArgs = new HashMap<>();
        // 设置队列的最大优先级
        queueArgs.put("x-max-priority", MAX_PRIORITY);

        // 声明优先级队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, queueArgs);

        System.out.println("消费者已启动，等待接收消息...");
        System.out.println("注意：消息将按优先级高低顺序被消费");
        System.out.println("========================================\n");

        // 定义消费者
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                Integer priority = properties.getPriority();
                
                try {
                    // 模拟消息处理延迟（1-3秒）
                    int delay = (int) (Math.random() * 2) + 1;
                    System.out.println("[接收消息] 内容: '" + message + "' | 优先级: " + priority + " | 处理延迟: " + delay + "秒");
                    
                    // 模拟处理时间
                    TimeUnit.SECONDS.sleep(delay);
                    
                    System.out.println("[处理完成] 内容: '" + message + "' | 优先级: " + priority);
                    System.out.println("----------------------------------------");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 手动确认消息
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // 启动消费者（关闭自动确认）
        channel.basicConsume(QUEUE_NAME, false, consumer);

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