# Jeopardy backend (Spring Boot). Build: docker build -t jeopardy-backend .
#
# Runs with the "prod" profile. Configure it with environment variables (see application-prod.yaml):
#   SPRING_DATASOURCE_URL / SPRING_DATASOURCE_USERNAME / SPRING_DATASOURCE_PASSWORD   Postgres
#   CLERK_ISSUER                    your Clerk PRODUCTION instance (Frontend API URL), e.g. https://clerk.yourdomain.com
#                                   tokens must also come from JEOPARDY_PUBLIC_URL (Clerk "azp" check)
#   LOBBY_BASE_URL                  how this server reaches the lobby API (e.g. http://lobby-backend:8080)
#   JEOPARDY_PUBLIC_URL             public URL of the Jeopardy frontend; players are sent to <it>/play/{id}
#   LOBBY_PUBLIC_KEY_LOCATION       e.g. file:/secrets/lobby-public.pem (mount the lobby's public key read-only)

FROM eclipse-temurin:26-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Windows line endings / lost exec bit break the wrapper on Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B -q dependency:go-offline
COPY src src
# Tests run in the GitHub workflow before this image is built (they need a Postgres, which a docker build doesn't have).
RUN ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:26-jre
WORKDIR /app
RUN useradd -r -u 1001 app
COPY --from=build /app/target/*.jar app.jar
USER app
# Heap follows the container's memory limit (mem_limit in the VPS compose file), leaving room for the JVM itself.
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_JPA_SHOW_SQL=false \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
