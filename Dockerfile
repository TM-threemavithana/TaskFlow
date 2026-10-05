# ====================================================================================
# DOCKERFILE - Multi-stage build
#
# STAGE 1 ("builder"): Uses a Maven image to compile and package the app.
# STAGE 2 ("runtime"): Uses a slim JRE image to run the app.
#
# Multi-stage builds keep the final image SMALL: the Maven/JDK tools are discarded,
# only the compiled JAR and the JRE are shipped.
#
# Build:  docker build -t taskflow .
# Run:    docker run -p 8080:8080 taskflow
# ====================================================================================

# STAGE 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the POM first. Docker caches this layer, so dependencies are only re-downloaded
# when pom.xml changes (not when your code changes). This speeds up rebuilds.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the source code and build
COPY src ./src
RUN mvn package -DskipTests -B

# STAGE 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy ONLY the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create a non-root user (security best practice: don't run as root in containers)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080

# ENTRYPOINT vs CMD:
# ENTRYPOINT = the command that always runs
# The JVM flags: -XX:+UseContainerSupport tells the JVM to respect Docker memory limits
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
