# Imagen del backend: no necesita Java ni Maven instalados en el PC.
# Etapa 1: compila el proyecto con Maven y Java 17.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Los tests se corren aparte con mvnw (necesitan la base de datos).
RUN mvn -q -B package -DskipTests

# Etapa 2: solo Java 17 + el .jar (imagen más liviana).
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/pasteleria-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
