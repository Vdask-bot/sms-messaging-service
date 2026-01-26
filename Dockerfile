# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

RUN ./mvnw -DskipTests package

# ---------- run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Quarkus fast-jar output
COPY --from=build /workspace/target/quarkus-app/ /app/quarkus-app/

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/quarkus-app/quarkus-run.jar"]
