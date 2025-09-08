# ---------- Build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
# descarga dependencias primero para cache más eficiente
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /opt/app
COPY --from=build /app/target/card-ops-producer-*.jar app.jar

# Puedes pasar flags aquí si quieres (GC logs, heap, etc.)
ENV JAVA_OPTS=""

# El puerto lo inyectamos por env (SERVER_PORT) desde docker-compose
EXPOSE 8085

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
