FROM maven:3.9-eclipse-temurin-21-alpine as build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN mvn package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

# Install wget for healthcheck
RUN apk add --no-cache wget

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.inventory.authservice.AuthServiceApplication"]

HEALTHCHECK --interval=10s --timeout=3s --retries=5 CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1
