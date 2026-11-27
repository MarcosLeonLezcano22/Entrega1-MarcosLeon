FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package

FROM tomcat:10.1-jdk17

COPY --from=build /app/target/Entrega1-MarcosLeon-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war