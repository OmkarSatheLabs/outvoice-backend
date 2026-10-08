# Multi-stage production container for outvoice-backend (Spring Boot 3.5 / Java 21)
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and project descriptor
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN dos2unix mvnw 2>/dev/null || sed -i -e 's/\r$//' mvnw
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B || true

# Copy source and build executable JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create unprivileged user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70.0 -XX:+UseSerialGC -Xss512k -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
