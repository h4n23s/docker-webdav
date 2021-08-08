# Compile sources and create 'fat jar'
FROM openjdk:11-jdk-buster AS builder

WORKDIR /workspace
COPY . ./
# Compile to fat-jar
RUN ./gradlew clean check shadowJar

# Create native image to reduce overhead
FROM ghcr.io/graalvm/graalvm-ce:latest AS native-builder

COPY --from=builder /workspace/build/libs/webdav-*.jar /workspace/setup-webdav.jar
COPY /reflection-config.json /workspace/reflection-config.json

WORKDIR /workspace
RUN gu install native-image && \
    native-image --gc=epsilon -esa -O2 --no-fallback -jar setup-webdav.jar setup-webdav \
    -H:ReflectionConfigurationFiles=reflection-config.json \
    -H:IncludeResources='eu\/hgweb\/webdav\/.+'

FROM httpd:2.4-buster

COPY --from=native-builder /workspace/setup-webdav /var/local/webdav/
COPY /docker/entrypoint.sh /var/local/webdav/

# Install curl as it's needed to do our health checks
RUN apt update && \
    apt install -y curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /var/local/webdav/

HEALTHCHECK --interval=25s --timeout=40s \
        # Wait for healthcheck script to be created
        CMD if [ -e healthcheck.sh ]; then chmod +x healthcheck.sh && ./healthcheck.sh; else exit 0; fi

ENTRYPOINT ["/bin/sh", "entrypoint.sh"]