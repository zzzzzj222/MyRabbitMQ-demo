@echo off

echo =========================
echo RabbitMQ 示例运行脚本
echo =========================
echo 1. 基础生产者-消费者模式
echo 2. 工作队列模式
echo 3. 发布/订阅模式
echo 4. 路由模式
echo 5. 主题模式
echo 0. 退出
echo =========================

:menu
set /p choice=请选择要运行的示例 (0-5): 

if "%choice%"=="0" goto exit
if "%choice%"=="1" goto basic
if "%choice%"=="2" goto workqueue
if "%choice%"=="3" goto pubsub
if "%choice%"=="4" goto routing
if "%choice%"=="5" goto topic

echo 无效的选择，请重新输入。
goto menu

:basic
echo 启动基础消费者...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.basic.BasicConsumer"
echo 3秒后启动生产者...
timeout /t 3 >nul
echo 启动基础生产者...
mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.basic.BasicProducer
goto menu

:workqueue
echo 启动工作队列消费者1...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.workqueue.WorkQueueConsumer1"
echo 1秒后启动消费者2...
timeout /t 1 >nul
echo 启动工作队列消费者2...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.workqueue.WorkQueueConsumer2"
echo 3秒后启动生产者...
timeout /t 3 >nul
echo 启动工作队列生产者...
mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.workqueue.WorkQueueProducer
goto menu

:pubsub
echo 启动发布/订阅消费者1...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.pubsub.PubSubConsumer1"
echo 1秒后启动消费者2...
timeout /t 1 >nul
echo 启动发布/订阅消费者2...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.pubsub.PubSubConsumer2"
echo 3秒后启动生产者...
timeout /t 3 >nul
echo 启动发布/订阅生产者...
mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.pubsub.PubSubProducer
goto menu

:routing
echo 启动路由消费者1 (仅error)...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.routing.RoutingConsumer1"
echo 1秒后启动消费者2 (info, warning, error)...
timeout /t 1 >nul
echo 启动路由消费者2...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.routing.RoutingConsumer2"
echo 3秒后启动生产者...
timeout /t 3 >nul
echo 启动路由生产者...
mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.routing.RoutingProducer
goto menu

:topic
echo 启动主题消费者1 (*.orange.*)...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.topic.TopicConsumer1"
echo 1秒后启动消费者2 (*.*.rabbit, lazy.#)...
timeout /t 1 >nul
echo 启动主题消费者2...
start cmd /k "mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.topic.TopicConsumer2"
echo 3秒后启动生产者...
timeout /t 3 >nul
echo 启动主题生产者...
mvn exec:java -Dexec.mainClass=com.rabbitmq.demo.topic.TopicProducer
goto menu

:exit
echo 感谢使用 RabbitMQ 示例！
exit /b
