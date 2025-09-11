FROM eclipse-temurin:21-jdk AS builder
LABEL authors="brian.kim"
WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre AS runner
WORKDIR /app

ENV JAR_NAME=xrpl.jar
EXPOSE 8080

RUN groupadd --system --gid 1001 javagroup
RUN useradd --system --uid 1001 javauser
USER javauser

COPY --from=builder --chown=javauser:javagroup /app/build/libs/$JAR_NAME $JAR_NAME

ENTRYPOINT exec java -jar $JAR_NAME
