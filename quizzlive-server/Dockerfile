# Step 1: Build Java Application using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY quizzlive-shared ./quizzlive-shared
COPY quizzlive-server ./quizzlive-server
COPY quizzlive-client ./quizzlive-client

RUN mvn clean package -DskipTests

# Step 2: Create lightweight runtime container image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/quizzlive-server/target/quizzlive-server-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
