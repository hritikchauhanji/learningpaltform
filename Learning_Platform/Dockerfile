FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/learningplatformbyhritikchauhan-0.0.1-SNAPSHOT.jar learningplatformbyhritikchauhan.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","learningplatformbyhritikchauhan.jar"]