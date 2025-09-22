# ---------- Build ----------
# Imagen base con Maven + JDK 17 para compilar el proyecto
FROM maven:3.9.9-eclipse-temurin-17 AS build

# Directorio de trabajo dentro del contenedor (fase build)
WORKDIR /app

# Copia el archivo pom.xml para resolver dependencias
COPY pom.xml ./

# Descarga dependencias Maven primero para aprovechar la cache
RUN mvn -q -e -DskipTests dependency:go-offline

# Copia el código fuente al contenedor
COPY src ./src

# Compila y empaqueta el proyecto en un JAR (sin correr tests)
RUN mvn -q -e -DskipTests package


# ---------- Runtime ----------
# Imagen ligera con solo JRE 17 para ejecutar el JAR
FROM eclipse-temurin:17-jre

# Directorio de trabajo en la fase de runtime
WORKDIR /opt/app

# Copia el JAR construido desde la fase build al runtime
COPY --from=build /app/target/card-ops-producer-*.jar app.jar

# Variable de entorno para pasar flags extra a la JVM (ej: GC, heap, logs)
ENV JAVA_OPTS=""

# Expone el puerto 8085 (se puede mapear desde docker-compose con SERVER_PORT)
EXPOSE 8085

# Comando de inicio: ejecuta la aplicación con posibles opciones de JVM
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
