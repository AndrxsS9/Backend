# Etapa de construcción (Build)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compilar el proyecto saltando los tests para agilizar el despliegue
RUN mvn clean package -DskipTests

# Etapa de ejecución (Run)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiamos el archivo .jar compilado desde la etapa anterior
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto por el que correrá Spring Boot
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
