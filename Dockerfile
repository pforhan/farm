# farm/Dockerfile
# Use an official OpenJDK image as the base for the Ktor backend
FROM openjdk:17-jdk-slim

# Set environment variables
ENV APP_HOME=/app

# Create app directory
WORKDIR $APP_HOME

# Copy the Gradle wrapper and build files
COPY gradlew $APP_HOME/
COPY gradle $APP_HOME/gradle
COPY settings.gradle.kts $APP_HOME/
COPY build.gradle.kts $APP_HOME/
COPY backend $APP_HOME/backend
COPY common $APP_HOME/common
COPY database $APP_HOME/database
COPY frontend $APP_HOME/frontend
COPY public $APP_HOME/public # Copy public/uploads and public/previews directories
COPY var $APP_HOME/var # Copy var/logs and var/cache directories
COPY docs $APP_HOME/docs # Copy docs for database.sql, etc.

# Build the Ktor application and the Compose Multiplatform frontend for web
# Frontend build command: ./gradlew frontend:jsBrowserProductionWebpack
# Backend build command: ./gradlew backend:installDist
# This task chain ensures frontend assets are copied to backend resources before backend JAR is built.
RUN chmod +x ./gradlew && ./gradlew backend:installDist

# Use the distribution created by installDist
WORKDIR $APP_HOME/backend/build/install/backend/bin

# Expose Ktor's default port
EXPOSE 8080

# Command to run the application
CMD ["./backend"]

# Adjust permissions for directories that need to be writable by the application
# These are mounted from host volumes, so this is mainly for safety within the container.
RUN mkdir -p $APP_HOME/public/uploads && \
    mkdir -p $APP_HOME/public/previews && \
    mkdir -p $APP_HOME/var/logs && \
    mkdir -p $APP_HOME/var/cache && \
    chown -R nobody:nogroup $APP_HOME/public/uploads && \
    chown -R nobody:nogroup $APP_HOME/public/previews && \
    chown -R nobody:nogroup $APP_HOME/var/logs && \
    chown -R nobody:nogroup $APP_HOME/var/cache && \
    chmod -R 777 $APP_HOME/public/uploads && \
    chmod -R 777 $APP_HOME/public/previews && \
    chmod -R 777 $APP_HOME/var/logs && \
    chmod -R 777 $APP_HOME/var/cache