# 仅运行环境，不在镜像内编译。须先在服务器上用本机 Maven 打出 jar 后再 docker build。
# 与 Jenkinsfile 配合：服务器拉代码 → 服务器 mvn package → 本 Dockerfile 打镜像 → 起容器。
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata fontconfig ttf-dejavu && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

COPY ruoyi-admin/target/ruoyi-admin.jar app.jar

RUN mkdir -p /app/uploadPath

EXPOSE 80

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=80"]
