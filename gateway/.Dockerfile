FROM openjdk:11-jre-slim
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} gateway.jar
ENTRYPOINT ["java","-jar","/gateway.jar"]
EXPOSE 8080