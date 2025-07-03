# Farm: Digital Asset Manager

## Files, Assets, Resources, Metadata

Farm is a simple, open-source web application for tracking and managing your digital assets. It features a **Kotlin Ktor backend** for robust API services and a **React/TypeScript frontend** for a dynamic user interface. This combination provides a modern, high-performance solution for organizing assets by store/source, author, license, tags, and projects, and includes capabilities to upload files (including zip archives), preview graphics, and play audio.

## Features

* **Modern Stack:** Built with Kotlin Ktor on the backend and React/TypeScript with Tailwind CSS on the frontend.
* **Asset Tracking:** Store details like asset name, source store, link, author, and license.
* **Flexible Organization:** Categorize assets with zero or more tags and associate them with zero or more projects.
* **File Management:** Upload individual files or zip archives.
* **Automatic Extraction:** Zip files are automatically extracted, and their contents are individually tracked.
* **Enhanced Tagging:**
    * Automatically tags assets based on the uploaded zip filename and source URL.
    * Extracts additional tags from directory names within uploaded zip archives (e.g., `20x20/static` will add "20x20" and "static" as tags).
* **Media Previews:** View previews of image files (JPG, PNG, GIF) and play audio files (WAV, MP3, OGG).
* **Text File Handling:** Supports uploading and managing various text-based files (TXT, MD, HTML, JSON, XML).
* **Search & Browse:** Search by name, tag, type, and graphics size. Browse all assets with quick access to details.

## Project Structure

```
farm/
├── backend/            # Kotlin Ktor backend source code and Gradle build files
├── common/             # Kotlin multiplatform module for shared data models (used by backend)
├── database/           # Kotlin module for Exposed ORM and database access logic
├── frontend-react/     # React/TypeScript frontend source code
│   ├── public/         # Static assets for React app (index.html)
│   ├── src/            # React components, TypeScript files, API client
│   ├── package.json    # Node.js dependencies for React
│   ├── tailwind.config.js # Tailwind CSS configuration
│   ├── tsconfig.json   # TypeScript configuration for React source
│   ├── tsconfig.node.json # TypeScript configuration for Node.js environment (e.g., Vite config)
│   ├── vite.config.ts  # Vite configuration for development server and build
│   └── ...             # Other React/TypeScript config files
├── public/             # Host-mounted directories for uploaded files and previews (Docker volumes)
│   ├── uploads/        # Actual uploaded files
│   └── previews/       # Generated thumbnails/previews
├── var/                # Host-mounted volatile data: logs, cache (Docker volumes)
│   ├── logs/
│   └── cache/
├── docs/               # Project documentation, including database schema
├── .env                # Environment variables for Docker Compose (DO NOT COMMIT)
├── docker-compose.yml  # Docker Compose setup
├── Dockerfile          # Multi-stage Dockerfile for building both frontend and backend
├── gradlew             # Gradle Wrapper for Kotlin backend
├── gradle/             # Gradle Wrapper files
│   └── libs.versions.toml # Centralized dependency versions (TOML format)
├── build.gradle.kts    # Root Gradle build script
├── settings.gradle.kts # Root Gradle settings script
├── .gitignore          # Git ignore file
├── LICENSE             # Project license
└── README.md           # This README file
```

## Installation with Docker Compose (Recommended)

This is the recommended way to run Farm, providing a consistent and isolated environment.

**Prerequisites:**

* **Docker Desktop** (for Windows/macOS) or **Docker Engine & Docker Compose** (for Linux) installed and running.
* **Git** (optional, but recommended for cloning this project).

**Steps:**

1.  **Clone the repository (or create the directory manually):**
    ```bash
    git clone [your-repo-url] farm
    cd farm
    ```
    If not using Git, manually create a directory named `farm` and populate it with the files from the project structure above.

2.  **Create the `.env` file:**
    Create a file named `.env` in the `farm/` directory (at the same level as `docker-compose.yml`) and add your database credentials. **Do not commit this file to version control.**

    ```dotenv
    # .env - Environment variables for Docker Compose
    MYSQL_ROOT_PASSWORD=your_root_password_here
    MYSQL_DATABASE=your_farm_db_name
    MYSQL_USER=your_farm_db_user
    MYSQL_PASSWORD=your_farm_db_password
    APP_PORT=6118 # Port the Ktor app will be accessible on your host
    ```
    **Remember to replace `your_root_password_here`, `your_farm_db_name`, `your_farm_db_user`, and `your_farm_db_password` with strong, unique values. Pay special attention to matching `MYSQL_USER` and `MYSQL_PASSWORD` if you're using names like `farm_user_dev`.**

3.  **Run the initial setup for host directories:**
    While Docker volumes handle persistence, it's good practice to create these on the host. From your `farm` directory, run:
    ```bash
    mkdir -p public/uploads public/previews var/logs var/cache
    chmod -R 777 public/uploads public/previews var/logs var/cache
    ```

4.  **Build and start the Docker services:**
    From your `farm` project root directory, run:
    ```bash
    docker compose up --build -d
    ```
    * `--build`: This ensures that your Docker images (Node.js for frontend, OpenJDK for backend) are built from their respective stages in the `Dockerfile`. You need this the first time or if you change the `Dockerfile`, `package.json`, or Gradle files.
    * `-d`: Runs the containers in detached mode (in the background).

    This command will:
    * Build the React frontend (Stage 1 in Dockerfile).
    * Build the Kotlin Ktor backend JAR (Stage 2 in Dockerfile).
    * Create a final Docker image containing both the Ktor application and the static React build, where Ktor is configured to serve the React files.
    * Start the `app` (Ktor application) container.
    * Start the `db` (MySQL) database container.
    * **Automatically initialize the MySQL database** by importing `docs/database.sql` on the *first run* of the `db` service.
    * Set up networking between the containers.

5.  **Access the application:**
    Open your web browser and navigate to `http://localhost:6118` (or the `APP_PORT` you defined in your `.env` file).

**To stop and remove containers (and networks/volumes by default):**

```bash
docker compose down
```

*(If you want to remove the database data volume as well, which means losing all your uploaded asset data, use `docker compose down -v`)*

## Usage

* **Upload:** Use the "Upload New Asset" form to add new digital game assets. You can provide the asset name, source URL, store, author, license, and initial tags/projects directly during upload. Zip files will be automatically extracted, and content will be processed and tagged.
* **Browse:** View a list of all your assets. Click "Details" to see more information about a specific asset, including its associated files, tags, and projects.
* **Search:** Use the search form to find assets by asset name, associated tags, or file type.

## Contributing

Contributions are welcome! Please feel to open a bug report, suggest a feature, or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.