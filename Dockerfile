
FROM eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache bash curl unzip dos2unix

WORKDIR /workspace


COPY settings.gradle . 
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
RUN dos2unix gradlew && chmod +x gradlew


COPY . .


RUN ./gradlew bootJar --no-daemon -x test


FROM eclipse-temurin:21-jre-alpine

RUN adduser -D -h /app appuser \
 && mkdir -p /app/cache /tmp \
 && chown -R appuser:appuser /app /tmp

WORKDIR /app
VOLUME ["/tmp"]


COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown appuser:appuser /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
