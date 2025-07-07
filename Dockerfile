# farm/Dockerfile
# This Dockerfile will build both the Kotlin Ktor backend and the React/TypeScript frontend,
# and then package them into a single image where the Ktor app serves the static React files.

# --- Stage 1: Build the React Frontend ---
FROM node:20-alpine as frontend-builder # Changed from node:18-alpine to node:20-alpine for better compatibility with Vite 5.x

WORKDIR /app/frontend-react

# Add a check for Node.js version to ensure compatibility
# This ensures that if the base image ever changes or is overridden,
# we explicitly fail if the Node.js version is too low for Vite/TypeScript.
RUN NODE_MAJOR_VERSION=$(node -v | sed 's/v//' | cut -d'.' -f1) && \
    if [ "$NODE_MAJOR_VERSION" -lt 20 ]; then \
        echo "ERROR: Node.js version $NODE_MAJOR_VERSION is too old. Minimum required is 20." && \
        exit 1; \
    else \
        echo "Node.js version $NODE_MAJOR_VERSION is compatible."; \
    fi


# Copy package.json and package-lock.json (or yarn.lock) for dependency caching
COPY frontend-react/package.json ./

# Clean npm cache and node_modules before installing to ensure a fresh environment
# RUN rm -rf node_modules && npm cache clean --force # This line is usually not needed and can cause issues

# Copy vite.config.ts
COPY frontend-react/vite.config.ts ./

# Install frontend dependencies, using --legacy-peer-deps for robustness with peer dependency issues
RUN npm install --legacy-peer-deps

# Copy the rest of the frontend source code (src/ and public/)
COPY frontend-react/ ./

# --- DEBUGGING: List contents to verify file structure before frontend build ---
RUN echo "--- Contents of /app/frontend-react/ (before frontend build) ---"
RUN ls -R /app/frontend-react/
RUN echo "--- Checking for public/index.html (before frontend build) ---"
RUN test -f public/index.html && echo "public/index.html exists." || echo "public/index.html NOT found!"
RUN echo "--- End of debug output (before frontend build) ---"


# Build the React application
# This command typically creates a 'dist' or 'build' directory with static files
RUN npm run build

# --- DEBUGGING: Verify frontend build output ---
RUN echo "--- Contents of /app/frontend-react/ (after frontend build) ---"
RUN ls -R /app/frontend-react/
RUN echo "--- Contents of /app/frontend-react/build/ ---"
RUN ls -l /app/frontend-react/build/ || echo "frontend-react/build directory not found or empty."
RUN test -d /app/frontend-react/build && echo "frontend-react/build directory exists." || echo "frontend-react/build directory DOES NOT EXIST."
RUN echo "--- End of debug output (after frontend build) ---"


# --- Stage 2: Build the Kotlin Ktor Backend ---
FROM openjdk:21-jdk-slim as backend-builder

ENV APP_HOME=/app
WORKDIR $APP_HOME

# Copy Gradle wrapper and build files first to leverage Docker cache
# These files define the project structure and dependencies.
COPY gradlew $APP_HOME/
COPY gradle $APP_HOME/gradle/
COPY settings.gradle.kts $APP_HOME/
COPY build.gradle.kts $APP_HOME/

# Force download of dependencies into a cached layer.
# This layer will only be invalidated if build.gradle.kts, settings.gradle.kts,
# or libs.versions.toml (copied with 'gradle/') changes.
# `--refresh-dependencies` ensures Gradle checks for new versions of dependencies,
# but it won't re-download if the versions haven't changed and are in the cache.
# The `dependencies` task resolves and downloads all project dependencies.
# `|| true` is added to prevent the build from failing if the dependencies task
# exits with a non-zero code for some reason (e.g., no dependencies to resolve, though unlikely).
RUN chmod +x ./gradlew && ./gradlew --refresh-dependencies backend:dependencies || true

# Copy the built React frontend static files from the frontend-builder stage
# These files will be served by Ktor's static content feature from src/main/resources/static
# This MUST happen BEFORE backend:installDist so the files are included in the JAR.
COPY --from=frontend-builder /app/frontend-react/build $APP_HOME/backend/src/main/resources/static/

# Copy the rest of the backend source code and other necessary directories
# Changes in these files will invalidate subsequent layers, but not the dependency download layer.
COPY backend $APP_HOME/backend/
COPY common $APP_HOME/common/
COPY database $APP_HOME/database/
COPY public $APP_HOME/public/ # Copy empty public/uploads and public/previews for structure
COPY var $APP_HOME/var/       # Copy empty var/logs and var/cache for structure

# Build the Ktor application JAR
# This will now use the dependencies downloaded and cached in the previous layer.
RUN ./gradlew backend:installDist

# --- Stage 3: Create the Final Production Image ---
FROM openjdk:21-jdk-slim

ENV APP_HOME=/app
WORKDIR $APP_HOME

# Copy the backend distribution from the backend-builder stage
COPY --from=backend-builder $APP_HOME/backend/build/install/backend /app/backend-dist

# Copy the built React frontend static files from the frontend-builder stage
# These files will be served by Ktor's static content feature from src/main/resources/static

# --- DEBUGGING: List contents of the Ktor static resources directory ---
RUN echo "--- Contents of /app/backend-dist/lib/src/main/resources/static/ (after frontend copy) ---"
RUN ls -R /app/backend-dist/lib/src/main/resources/static/
RUN echo "--- Detailed listing of /app/backend-dist/lib/src/main/resources/static/ ---"
RUN ls -l /app/backend-dist/lib/src/main/resources/static/
RUN echo "--- Checking for index.html in Ktor's static resources ---"
RUN test -f /app/backend-dist/lib/src/main/resources/static/index.html && echo "index.html found in Ktor static resources." || echo "index.html NOT found in Ktor static resources!"
RUN echo "--- End of debug output (after frontend copy) ---"


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
    chown -R nobody:nogroup $APP_HOME/var/cache && \
    chmod -R 777 $APP_HOME/public/uploads && \
    chmod -R 777 /app/public/previews \
    && chmod -R 777 $APP_HOME/var/logs \
    && chmod -R 777 $APP_HOME/var/cache

# Set the entry point to run the Ktor application
WORKDIR /app/backend-dist/bin
CMD ["./backend"]

EXPOSE 8080
