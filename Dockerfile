FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/ing-0.0.1-SNAPSHOT.jar ingAssetCase.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "ingAssetCase.jar"]
