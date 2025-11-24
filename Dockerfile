# Use an official OpenJDK 8 image as a parent image
FROM maven:3.8.6-jdk-8

# Set the working directory in the container
WORKDIR /app

# Copy the POM file
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package assembly:single

EXPOSE 3002

# Run the application
CMD ["java", "-cp", "target/sprbaysign-1.0-SNAPSHOT-jar-with-dependencies.jar", "org.example.Main"]