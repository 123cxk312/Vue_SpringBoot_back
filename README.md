# Vue_SpringBoot

这是一个只包含依赖和基础配置的 Spring Boot 后端骨架，不包含实体、Mapper、Service、Controller 和成绩业务代码。

## 环境

- Java 21
- Spring Boot 3.5.6
- MyBatis-Plus 3.5.16
- MySQL 8

## 配置

修改 `src/main/resources/application.yml` 中的数据库连接，或者设置以下环境变量：

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_TTL
```

## 启动

```powershell
$env:JAVA_HOME='D:\jdk-21.0.6'
.\mvnw.cmd spring-boot:run
```

如果 Maven Wrapper 需要联网下载，可以使用 IntelliJ IDEA 内置 Maven：

```powershell
$env:JAVA_HOME='D:\jdk-21.0.6'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.1.5.1\plugins\maven\lib\maven3\bin\mvn.cmd' spring-boot:run
```

服务端口：

```text
http://localhost:8088
```

Swagger UI：

```text
http://localhost:8088/swagger-ui/index.html
```

当前没有配置数据库建表脚本，因此启动前只需要保证 MySQL 服务可用；业务表可以在后续开发时逐步创建。
