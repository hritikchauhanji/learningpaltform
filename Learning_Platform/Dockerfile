FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /Learning_Platform/
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
WORKDIR /Learning_Platform/
COPY --from=build /Learning_Platform/target/*.jar Learning_Platform.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","Learning_Platform.jar"]
