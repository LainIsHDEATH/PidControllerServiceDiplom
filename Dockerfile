FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/PidControllerService-1.0.0.jar PidControllerService.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "PidControllerService.jar"]