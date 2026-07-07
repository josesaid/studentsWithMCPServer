# =============================================
# Build stage
# =============================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Install Maven (this layer is cached)
RUN apk add --no-cache maven

# Copy source and build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

# =============================================
# Runtime stage
# =============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/students-1.0.1.jar app.jar
EXPOSE 8101
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8101"]
