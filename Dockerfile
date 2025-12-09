FROM eclipse-temurin:21-jre-alpine-3.22
ENV PORT=8081
EXPOSE 8081

WORKDIR /app
COPY /target/realtime-chatapp-message-service-0.0.1-SNAPSHOT.jar app.jar

RUN adduser -D appuser
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]