FROM openjdk:21-jdk-slim
# Set the working directory in the container
WORKDIR app

# Copy the application JAR file into the working directory
COPY build/libs/*.jar app.jar

# Expose the port on which the application will run
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]