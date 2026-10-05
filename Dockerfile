FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace


COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline


COPY src/ src/
ARG SKIP_TESTS=false
RUN ./mvnw -B -q verify -DskipTests=${SKIP_TESTS} -Djacoco.skip=${SKIP_TESTS} \
    && cp target/desafio-tecnico-*.jar app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build --chown=app:app /workspace/app.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
