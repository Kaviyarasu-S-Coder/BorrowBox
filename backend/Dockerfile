# Build Stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user and group
RUN addgroup -S borrowbox && adduser -S borrowbox -G borrowbox

# Create uploads directory with correct permissions
RUN mkdir -p /app/uploads/items && chown -R borrowbox:borrowbox /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/borrowbox-backend-1.0.0.jar app.jar
RUN chown borrowbox:borrowbox app.jar

# Switch to non-root user
USER borrowbox

# Expose standard Spring Boot port
EXPOSE 8080

# Configure production JVM flags
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+OptimizeStringConcat -XX:+UseStringDeduplication -Xms256m -Xmx512m"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
