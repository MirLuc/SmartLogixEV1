FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -DskipTests -pl shipment-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN replace_sources() { \
    sed -i -e 's|http://archive.ubuntu.com/ubuntu|https://archive.ubuntu.com/ubuntu|g' \
    -e 's|http://security.ubuntu.com/ubuntu|https://security.ubuntu.com/ubuntu|g' "$@"; \
    }; \
    if [ -f /etc/apt/sources.list ]; then \
    replace_sources /etc/apt/sources.list; \
    fi; \
    if ls /etc/apt/sources.list.d/*.sources >/dev/null 2>&1; then \
    replace_sources /etc/apt/sources.list.d/*.sources; \
    fi \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/shipment-service/target/shipment-service-1.0.0.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
