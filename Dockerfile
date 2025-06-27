# farm/Dockerfile
# This Dockerfile will build both the Kotlin Ktor backend and the React/TypeScript frontend,
# and then package them into a single image where the Ktor app serves the static React files.

# --- Stage 1: Build the React Frontend ---
FROM node:18-alpine as frontend-builder

WORKDIR /app/frontend-react

# Copy package.json and package-lock.json (or yarn.lock) for dependency caching
COPY frontend-react/package.json ./

# Clean npm cache and node_modules before installing to ensure a fresh environment
RUN rm -rf node_modules && npm cache clean --force

# Copy vite.config.ts
COPY frontend-react/vite.config.ts ./

# Install frontend dependencies, using --legacy-peer-deps for robustness with peer dependency issues
RUN npm install --legacy-peer-deps

# Copy the rest of the frontend source code (src/ and public/)
COPY frontend-react/ ./

# --- DEBUGGING: List contents to verify file structure before build ---
RUN echo "--- Contents of /app/frontend-react/ ---"
RUN ls -R /app/frontend-react/
RUN echo "--- Checking for public/index.html ---"
RUN test -f public/index.html && echo "public/index.html exists." || echo "public/index.html NOT found!"
RUN echo "--- End of debug output ---"


# Build the React application
# This command typically creates a 'dist' or 'build' directory with static files
RUN npm run build


# --- Stage 2: Build the Kotlin Ktor Backend ---
FROM openjdk:21-jdk-slim as backend-builder

ENV APP_HOME=/app
WORKDIR $APP_HOME

# Copy Gradle wrapper and build files
COPY gradlew $APP_HOME/
COPY gradle $APP_HOME/gradle/
COPY settings.gradle.kts $APP_HOME/
COPY build.gradle.kts $APP_HOME/

COPY backend $APP_HOME/backend/
COPY common $APP_HOME/common/
COPY database $APP_HOME/database/
# Copy public/uploads and public/previews directories
COPY public $APP_HOME/public/
# Copy var/logs and var/cache directories
COPY var $APP_HOME/var/

# Build the Ktor application JAR
# This will also run the 'createDirs' task to ensure public/var dirs exist
RUN chmod +x ./gradlew && ./gradlew backend:installDist

# --- Stage 3: Create the Final Production Image ---
FROM openjdk:21-jdk-slim

ENV APP_HOME=/app
WORKDIR $APP_HOME

# Copy the backend distribution from the backend-builder stage
COPY --from=backend-builder $APP_HOME/backend/build/install/backend /app/backend-dist

# Copy the built React frontend static files from the frontend-builder stage
# These files will be served by Ktor's static content feature from src/main/resources/static
COPY --from=frontend-builder /app/frontend-react/build /app/backend-dist/lib/src/main/resources/static/

# Adjust permissions for directories that need to be writable by the application
# These are mounted from host volumes, so this is mainly for safety within the container.
# They are relative to the Ktor application's working directory within the container, which is /app
RUN mkdir -p $APP_HOME/public/uploads && \
    mkdir -p $APP_HOME/public/previews && \
    mkdir -p $APP_HOME/var/logs && \
    mkdir -p $APP_HOME/var/cache && \
    chown -R nobody:nogroup $APP_HOME/public/uploads && \
    chown -R nobody:nogroup $APP_HOME/public/previews && \
    chown -R nobody:nogroup $APP_HOME/var/logs && \
    chown -R 777 $APP_HOME/public/uploads && \
    chmod -R 777 /app/public/previews \
    && chmod -R 777 $APP_HOME/var/logs \
    && chmod -R 777 $APP_HOME/var/cache

# Set the entry point to run the Ktor application
WORKDIR /app/backend-dist/bin
CMD ["./backend"]

EXPOSE 8080