FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/Learning_Platform-0.0.1-SNAPSHOT.jar Learning_Platform.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","Learning_Platform.jar"]
