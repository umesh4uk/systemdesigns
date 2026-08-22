# ================================================================
# Multi-stage build for the Spring Boot application
# Stage 1: Build with Maven
# Stage 2: Minimal JRE runtime image
# ================================================================

# --- Stage 1: Build ---
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# Copy Maven wrapper and POMs first for layer caching
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .

# Copy all module POMs
COPY ecommerce-shared-kernel/pom.xml   ecommerce-shared-kernel/
COPY ecommerce-identity/pom.xml        ecommerce-identity/
COPY ecommerce-catalog/pom.xml         ecommerce-catalog/
COPY ecommerce-inventory/pom.xml       ecommerce-inventory/
COPY ecommerce-pricing/pom.xml         ecommerce-pricing/
COPY ecommerce-cart/pom.xml            ecommerce-cart/
COPY ecommerce-order/pom.xml           ecommerce-order/
COPY ecommerce-payment/pom.xml         ecommerce-payment/
COPY ecommerce-promotion/pom.xml       ecommerce-promotion/
COPY ecommerce-notification/pom.xml    ecommerce-notification/
COPY ecommerce-app/pom.xml             ecommerce-app/

# Download dependencies (cached if POMs haven't changed)
RUN ./mvnw dependency:go-offline -B -q

# Copy source
COPY ecommerce-shared-kernel/src   ecommerce-shared-kernel/src
COPY ecommerce-identity/src        ecommerce-identity/src
COPY ecommerce-catalog/src         ecommerce-catalog/src
COPY ecommerce-inventory/src       ecommerce-inventory/src
COPY ecommerce-pricing/src         ecommerce-pricing/src
COPY ecommerce-cart/src            ecommerce-cart/src
COPY ecommerce-order/src           ecommerce-order/src
COPY ecommerce-payment/src         ecommerce-payment/src
COPY ecommerce-promotion/src       ecommerce-promotion/src
COPY ecommerce-notification/src    ecommerce-notification/src
COPY ecommerce-app/src             ecommerce-app/src

# Build, skip tests (tests run in CI)
RUN ./mvnw package -pl ecommerce-app -am -DskipTests -B -q

# Explode the JAR for optimal layer caching in the runtime stage
RUN mkdir -p /workspace/exploded && \
    java -Djarmode=layertools \
         -jar ecommerce-app/target/ecommerce-app-*.jar \
         extract --destination /workspace/exploded

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy AS runtime

# Security: run as non-root
RUN groupadd --system --gid 1001 appgroup && \
    useradd  --system --uid  1001 --gid appgroup --no-create-home appuser

WORKDIR /app

# Copy exploded JAR layers (ordered for maximum cache reuse)
COPY --from=builder --chown=appuser:appgroup /workspace/exploded/dependencies          ./
COPY --from=builder --chown=appuser:appgroup /workspace/exploded/spring-boot-loader    ./
COPY --from=builder --chown=appuser:appgroup /workspace/exploded/snapshot-dependencies ./
COPY --from=builder --chown=appuser:appgroup /workspace/exploded/application           ./

USER appuser

EXPOSE 8080

# Health check — waits up to 60s for the app to start
HEALTHCHECK --interval=15s --timeout=5s --retries=4 --start-period=60s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
