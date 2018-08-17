# Blockchain
用于开发 validator node，Maven项目.
1. mongodb 3.2
2. RabbitMQ 3.7.4
3. 多及节点测试启动顺序：Transaction Transmitter - ValidatorsStarter - BlockersStarter

# Web 端说明
1. 使用SpringMVC + Spring 框架作为 Web 后台提供与 Validator 交互， Spring 版本为 4.1.7.
2. JDK版本为1.8, Tomcat 版本为 8.

# 开发与改进方向
1. 目前节点间通信采用的是 BIO 的 **Socket**，节点间通信时，依次与其他节点建立 Socket 连接，传输数据，断开连接，这种方式是十分低效的，因此准备使用 **Kafka， Redis** 等分布式发布订阅消息系统改进。改进思路是主节点将分配好视图号 v 和序号 n 的预准备消息 m 发布到这些消息系统中，各备份节点从消息系统中获取预准备消息进行验证。接受后，生成准备消息，发送到消息系统中，其他节点进行消费。最后节点生成提交消息，发布到消息系统，供其他节点消费。
 
