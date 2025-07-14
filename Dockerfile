# Usa l'immagine ufficiale OpenJDK 21 come base
FROM eclipse-temurin:21-jdk

# Cartella di lavoro dentro il container
WORKDIR /app

# Copia il jar dell'app nella cartella di lavoro
COPY target/serverB-0.0.1-SNAPSHOT.jar /app/app.jar

# Espone la porta 8080
EXPOSE 8081

# Comando per avviare l'app Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]