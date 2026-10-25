
FROM ubuntu:24.04

RUN apt-get update && \
    apt-get install -y openjdk-21-jdk openjfx fonts-noto-cjk fontconfig && \
    fc-cache -f -v && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/classes /app
COPY target/dependency /app/dependency

CMD ["java", "-Dprism.order=sw", "--module-path", "/usr/share/openjfx/lib", "--add-modules", "javafx.controls", "-cp", "/app:/app/dependency/*", "Main"]
