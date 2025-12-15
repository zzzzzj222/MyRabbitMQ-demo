package com.rabbitmq.demo.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ 工具类
 * 提供连接和通道的创建、关闭等功能
 */
public class RabbitMQUtils {
    // RabbitMQ 连接工厂
    private static ConnectionFactory factory;

    /**
     * 静态初始化连接工厂
     * 配置 RabbitMQ 服务器的连接信息
     */
    static {
        factory = new ConnectionFactory();
        // 设置 RabbitMQ 服务器地址
        factory.setHost("localhost");
        // 设置 RabbitMQ 服务器端口
        factory.setPort(5672);
        // 设置用户名
        factory.setUsername("guest");
        // 设置密码
        factory.setPassword("guest");
        // 设置虚拟主机
        factory.setVirtualHost("/");
    }

    /**
     * 获取 RabbitMQ 连接
     *
     * @return RabbitMQ 连接
     * @throws IOException      IO 异常
     * @throws TimeoutException 超时异常
     */
    public static Connection getConnection() throws IOException, TimeoutException {
        return factory.newConnection();
    }

    /**
     * 获取 RabbitMQ 通道
     *
     * @return RabbitMQ 通道
     * @throws IOException      IO 异常
     * @throws TimeoutException 超时异常
     */
    public static Channel getChannel() throws IOException, TimeoutException {
        return getConnection().createChannel();
    }

    /**
     * 关闭 RabbitMQ 连接
     *
     * @param connection RabbitMQ 连接
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭 RabbitMQ 通道
     *
     * @param channel RabbitMQ 通道
     */
    public static void closeChannel(Channel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}