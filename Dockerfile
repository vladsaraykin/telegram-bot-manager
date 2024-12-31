FROM openjdk:21-jdk-slim

COPY build/libs/eva-tgbot-manager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT java $JAVA_OPTS -jar app.jar