FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew
COPY src/ src/
# Tests run without PostgreSQL; database integration tests are opt-in separately.
RUN ./gradlew --no-daemon test bootJar

FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app
RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

COPY --from=build /workspace/build/libs/app.jar /app/app.jar
USER 10001:10001
ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
