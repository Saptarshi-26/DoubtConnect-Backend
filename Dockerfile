# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/doubtconnect-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", \
     "-XX:TieredStopAtLevel=1", \
     "-Xshare:auto", \
     "-Xmx400m", \
     "-jar", "app.jar"]