FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["sh","-c","java -jar /app/app.jar"]
