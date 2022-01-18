FROM openjdk:11-jre-slim
LABEL maintainer="shdlehdwls@gmail.com"
VOLUME /tmp
ARG JAR_FILE=./build/libs/*.jar
ADD ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:dev/./uuradom", "-jar", "/app.jar"]