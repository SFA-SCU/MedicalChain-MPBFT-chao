# 说明
1. Maven项目
2. 使用SpringMVC + Spring + MyBatis 框架, Spring 版本为 4.1.7, MyBatis 版本为 3.3.
3. 数据库配置文件位于`src\main\resources\jdbc.properties`.
4. 数据库表的 sql 文件位于`src\main\sql\user_demo.sql`.
5. 数据库连接池使用 Druid 1.1.0, 日志记录使用 logback 1.1.3.
6. JDK版本为1.8, Tomcat 版本为 8.
# Note
1. Maven project.
2. Using SpringMVC + Spring + MyBatis, The version of Spring is 4.1.7, MyBatis is 3.3.
3. Database configuration file is in `src\main\resources\jdbc.properties`.
4. Sql file is in `src\main\sql\user_demo.sql`.
5. Using Druid 1.1.0 as data store, and logback 1.1.3 as logger.
6. Usring JDK 1.8 and Tomcat8.

# Blockchain
用于开发 validator node.
1. mongodb 3.2
2. RabbitMQ 3.7.4
3. 多及节点测试启动顺序：Transaction Transmitter - ValidatorsStarter - BlockersStarter
