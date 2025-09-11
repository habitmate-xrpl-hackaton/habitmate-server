FROM eclipse-temurin:21-jdk AS builder
LABEL authors="brian.kim"
WORKDIR /app

ARG ARG_APP_NAME
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew :$ARG_APP_NAME:clean :$ARG_APP_NAME:bootJar --no-daemon
RUN cd $ARG_APP_NAME

FROM eclipse-temurin:21-jre AS runner
WORKDIR /app

ARG ARG_APP_NAME
ENV JAR_NAME=$ARG_APP_NAME.jar
EXPOSE 8080

RUN groupadd --system --gid 1001 javagroup
RUN useradd --system --uid 1001 javauser
USER javauser

COPY --from=builder --chown=javauser:javagroup /app/$ARG_APP_NAME/build/libs/$JAR_NAME $JAR_NAME

ENTRYPOINT exec java -jar $JAR_NAME
