
FROM ubuntu:24.04

RUN apt-get update && \
    apt-get install -y \
        openjdk-21-jdk \
        fonts-noto-cjk \
        fontconfig \
        libgtk-3-0 \
        libx11-6 \
        libx11-xcb1 \
        libxext6 \
        libxrender1 \
        libxtst6 \
        libxi6 \
        libxxf86vm1 \
        libgl1 \
        libglx0 \
        libegl1 \
        libasound2t64 \
    && fc-cache -f -v \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/classes /app
COPY target/dependency /app/dependency

CMD ["java", "-Dprism.order=sw", "-Dprism.verbose=true", "--module-path", "/app/dependency", "--add-modules", "javafx.controls,javafx.fxml", "--add-exports", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED", "-cp", "/app:/app/dependency/*", "Main"]
