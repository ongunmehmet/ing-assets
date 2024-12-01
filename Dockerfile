FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/ing-0.0.1-SNAPSHOT.jar ingLoanCase.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "ingLoanCase.jar"]
