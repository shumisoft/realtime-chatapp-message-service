# -------- BUILD STAGE --------
FROM eclipse-temurin:21-jdk-alpine-3.22 AS build

WORKDIR /app
COPY . .
RUN ./mvnw -q -DskipTests package

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jre-alpine-3.22

ENV PORT=8081
EXPOSE 8081

WORKDIR /app
COPY --from=build /app/target/realtime-chatapp-message-service-0.0.1-SNAPSHOT.jar app.jar

# Create non-root user
RUN adduser -D appuser
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
