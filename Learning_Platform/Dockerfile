# Use the official Maven image to build the project
FROM maven:3.8.6-openjdk-11 AS build

# Set the working directory to /myproject
WORKDIR /Learning_Platform

# Copy the project files to the container
COPY . .

# Build the Spring Boot application
RUN mvn clean package -DskipTests

# Use the official OpenJDK runtime image
FROM openjdk:11-jre-slim

# Set the working directory to /myproject in the runtime stage
WORKDIR /Learning_Platform

# Copy the built jar file from the build stage
COPY --from=build /Learning_Platform/target/*.jar app.jar

# Expose the port that the Spring Boot app will run on
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
