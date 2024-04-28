FROM ghcr.io/graalvm/jdk-community:22.0.1

WORKDIR /app

COPY build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]