FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY sample-app/pom.xml sample-app/pom.xml
COPY sample-app/src sample-app/src
RUN mvn -B -ntp -f sample-app/pom.xml package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/sample-app/target/vulnerable-saas-demo.jar /app/vulnerable-saas-demo.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/vulnerable-saas-demo.jar"]