FROM openjdk:17-jdk-slim
WORKDIR /app

# Copia qualquer .jar gerado no target para app.jar
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
