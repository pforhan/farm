# Farm Digital Asset Manager

This is a modern web application for managing digital assets, rewritten from a PHP/Nginx/MySQL stack to a Kotlin Ktor backend with Exposed for database interactions, and a Compose Multiplatform frontend for a rich, interactive user interface. The architecture is designed with an eye towards potential future expansion to Android, iOS, and Desktop applications using the same Compose Multiplatform codebase.

## Technologies Used

**Backend (Ktor JVM):**

* **Kotlin:** The primary language for backend development.

* **Ktor:** A flexible and asynchronous web framework for building servers and clients in Kotlin.

* **Exposed:** A Kotlin SQL framework (DSL and lightweight ORM) for database access, developed by JetBrains.

* **MySQL:** The relational database for storing asset metadata, tags, projects, and file information.

* **HikariCP:** A high-performance JDBC connection pool for efficient database connections.

* **Kotlinx.Serialization:** For efficient JSON serialization and deserialization of data between the frontend and backend.

**Frontend (Compose Multiplatform for Web):**

* **Kotlin:** The language for frontend development, compiled to JavaScript.

* **Compose Multiplatform:** A declarative UI framework by JetBrains, enabling shared UI code across Web, Desktop, Android, and iOS. For this web version, it compiles to Kotlin/JS.

* **Ktor Client:** Used by the frontend to make HTTP requests to the Ktor backend APIs.

**Containerization & Development Environment:**

* **Docker:** Used for containerizing the application services (Ktor app, MySQL database).

* **Docker Compose:** For defining and running multi-container Docker applications.

* **Gradle Kotlin DSL:** The build system for managing multi-module Kotlin projects.

## Project Structure

The project is organized into a multi-module Gradle setup:

* `farm/` (Root Project)

    * `settings.gradle.kts`: Defines the included submodules.

    * `build.gradle.kts`: Root Gradle configuration.

    * `docker-compose.yml`: Defines Docker services (`app` and `db`).

    * `Dockerfile`: Instructions for building the Ktor application Docker image.

    * `.env`: Environment variables for Docker Compose (e.g., database credentials, app port).

    * `public/`: Contains `uploads` and `previews` directories, mounted as Docker volumes.

    * `var/`: Contains `logs` and `cache` directories, mounted as Docker volumes.

    * `docs/database.sql`: Initial SQL schema for MySQL.

    * `backend/`:

        * `src/main/kotlin/com/farm/Application.kt`: Ktor server entry point and main module configuration.

        * `src/main/kotlin/com/farm/routes/AssetRoutes.kt`: Defines REST API endpoints for assets (upload, details, search, update).

        * `src/main/kotlin/com/farm/plugins/`: Ktor plugin configurations (serialization, error handling, static content).

        * `src/main/kotlin/com/farm/util/FileProcessing.kt`: Utility for thumbnail generation and ZIP file extraction.

        * `src/main/resources/static/`: Where the built Compose Multiplatform web assets (JS, HTML, CSS) are copied for Ktor to serve.

    * `common/`:

        * `src/commonMain/kotlin/com/farm/common/Models.kt`: Shared data classes (e.g., `Asset`, `FileDetail`, `UpdateAssetRequest`) used by both frontend and backend.

    * `database/`:

        * `src/main/kotlin/com/farm/database/DatabaseFactory.kt`: Database connection setup and Exposed transaction helpers.

        * `src/main/kotlin/com/farm/database/Tables.kt`: Exposed table definitions (Assets, Files, Tags, Projects, etc.).

        * `src/main/kotlin/com/farm/database/Dao.kt`: Data Access Object for interacting with the database using Exposed.

    * `frontend/`:

        * `src/jsMain/kotlin/Main.kt`: Compose Multiplatform web entry point.

        * `src/commonMain/kotlin/com/farm/frontend/App.kt`: Main UI application logic.

        * `src/commonMain/kotlin/com/farm/frontend/api/FarmApiClient.kt`: Ktor client for making API calls to the backend.

        * `src/commonMain/kotlin/com/farm/frontend/components/`: Individual reusable UI components (e.g., `UploadForm`, `BrowseAssets`).

        * `src/jsMain/resources/index.html`: The main HTML file for the web frontend.

        * `src/jsMain/resources/styles.css`: Basic CSS for the web frontend.

## Installation and Setup

### Prerequisites

* **Docker Desktop:** Ensure Docker is installed and running on your system.

* **Git:** For cloning the repository.

* (Optional, but recommended for development) **IntelliJ IDEA Ultimate:** Provides excellent support for Kotlin, Gradle, Ktor, Compose Multiplatform, and database tools.

### Steps to Run

1.  **Clone the Repository:**

    ```bash
    git clone <repository_url>
    cd farm
    ```

2.  **Create `.env` File:**
    Create a file named `.env` in the root `farm/` directory with the following content. **Replace placeholder values** with your desired credentials.

    ```dotenv
    # .env file for Farm Digital Asset Manager
    APP_PORT=6118

    # MySQL Database Configuration
    MYSQL_ROOT_PASSWORD=your_secure_root_password
    MYSQL_DATABASE=farm_db
    MYSQL_USER=farm_user
    MYSQL_PASSWORD=farm_password
    JDBC_DATABASE_URL=jdbc:mysql://db:3306/farm_db # 'db' refers to the service name in docker-compose
    ```

3.  **Ensure Writable Directories:**
    Create the necessary directories that Docker will mount as volumes. These should be at the root of your `farm` project:

    ```bash
    mkdir -p public/uploads public/previews var/logs var/cache
    ```

    On Linux/macOS, you might also need to ensure these directories are writable by the user running the Docker container (which is `nobody` in our Dockerfile, with `nogroup` group, and permissions are set to `777` for safety during testing).

4.  **Build and Run Docker Containers:**
    Navigate to the `farm/` root directory in your terminal and run:

    ```bash
    docker compose up --build -d
    ```

    * `--build`: Rebuilds the Docker images. This is important when you make changes to your Kotlin code or dependencies.

    * `-d`: Runs the containers in detached mode (in the background).

    This command will:

    * Build the `app` (Ktor) Docker image. During the build, it executes the Gradle task `backend:installDist`, which also triggers the `frontend:jsBrowserProductionWebpack` task to build the frontend and copy its assets to the backend's resources.

    * Start the MySQL database container (`db`).

    * Start the Ktor application container (`app`).

5.  **Access the Application:**
    Once the containers are up and running (it might take a few moments for the database to initialize and Ktor to start), open your web browser and navigate to:

    ```
    http://localhost:6118
    ```

    (Or the `APP_PORT` you specified in your `.env` file if different from `6118`).

## Development Notes

### Rebuilding After Code Changes

* **Backend Changes:** If you modify Kotlin code in `backend/`, `common/`, or `database/`, you'll need to rebuild the Docker image:

    ```bash
    docker compose up --build -d
    ```

* **Frontend Changes:** If you modify Kotlin Compose Multiplatform code in `frontend/`, the backend image build process should automatically pick up these changes. However, if you're iterating quickly on the frontend, you might explicitly run the frontend build task:

    ```bash
    ./gradlew :frontend:jsBrowserDevelopmentWebpack
    ```

    And then restart the backend container if its static content cache needs clearing, or perform a full `docker compose up --build -d`.

### Database Access

You can connect to the MySQL database from your host machine using a tool like DataGrip, MySQL Workbench, or DBeaver.

* **Host:** `localhost`

* **Port:** `3306` (as mapped in `docker-compose.yml`)

* **User:** `farm_user` (or whatever you set in `.env`)

* **Password:** `farm_password` (or whatever you set in `.env`)

* **Database:** `farm_db` (or whatever you set in `.env`)

### Future Enhancements (TODOs)

* **Two-Stage File Upload:** Implement a more robust upload process where files are first uploaded and processed, then metadata is reviewed and finalized by the user. (See `backend/src/main/kotlin/com/farm/routes/AssetRoutes.kt` for `TODO` comments).

* **User Management:** Integrate a complete user authentication and authorization system (e.g., user registration, login, roles, permissions). (See `backend/src/main/kotlin/com/farm/Application.kt` and `database/src/main/kotlin/com/farm/database/Tables.kt` for `TODO` comments).

* **Multiplatform Builds:** Expand the Compose Multiplatform frontend to target Android, iOS, and Desktop native applications.

* **Enhanced Search:** Implement more advanced search capabilities (e.g., filtering by tags, file types, date ranges).

* **Tag Management UI:** Create a UI for managing tags (create, edit, delete, merge).

* **Asset Deletion:** Add functionality to delete assets and their associated files/previews.

* **API Documentation:** Generate API documentation (e.g., using OpenAPI/Swagger).
