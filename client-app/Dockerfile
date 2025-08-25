# 1段階目: ビルド
FROM gradle:8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# 2段階目: 実行
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/client-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
