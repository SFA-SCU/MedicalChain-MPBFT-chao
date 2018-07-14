# Blockchain
用于开发 validator node，Maven项目.
1. mongodb 3.2
2. RabbitMQ 3.7.4
3. 多及节点测试启动顺序：Transaction Transmitter - ValidatorsStarter - BlockersStarter

# Web 端说明
1. 使用SpringMVC + Spring 框架作为 Web 后台提供与 Validator 交互， Spring 版本为 4.1.7.
2. JDK版本为1.8, Tomcat 版本为 8.

