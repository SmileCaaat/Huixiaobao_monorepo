# ==================== 阶段1: 构建 ====================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 先复制 pom.xml 文件，利用 Docker 缓存加速依赖下载
COPY pom.xml .
COPY ruoyi-admin/pom.xml ruoyi-admin/
COPY ruoyi-common/pom.xml ruoyi-common/
COPY ruoyi-framework/pom.xml ruoyi-framework/
COPY ruoyi-generator/pom.xml ruoyi-generator/
COPY ruoyi-quartz/pom.xml ruoyi-quartz/
COPY ruoyi-system/pom.xml ruoyi-system/

# 下载依赖（使用阿里云镜像加速）
RUN mvn dependency:go-offline -B \
    -Dmaven.repo.local=/build/.m2 \
    -DdownloadSources=false \
    -DdownloadJavadocs=false

# 复制源代码
COPY . .

# 编译打包（跳过测试）
RUN mvn clean package -DskipTests -B \
    -Dmaven.repo.local=/build/.m2

# ==================== 阶段2: 运行 ====================
FROM eclipse-temurin:21-jre-alpine

# 安装时区和字体（验证码需要）
RUN apk add --no-cache tzdata fontconfig ttf-dejavu && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/ruoyi-admin/target/ruoyi-admin.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploadPath

# 暴露 80 端口
EXPOSE 80

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=80"]