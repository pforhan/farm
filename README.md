# Farm: Digital Asset Manager

NOTE: EVERYTHING BELOW AND EVERY FILE IN THIS PROJECT IS AI-GENERATED AND NOT CONFIRMED YET.  USE AT YOUR OWN RISK.

## Files, Assets, Resources, Metadata

Farm is a simple, open-source web application for tracking and managing your digital assets. It allows you to organize assets by store/source, author, license, tags, and projects, and provides a way to upload files (including zip archives), preview graphics, and play audio.

## Features

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
├── config/             # Configuration files (config.php)
├── src/                # Core application source code
│   ├── lib/            # Core PHP functions
│   ├── templates/      # HTML templates
│   └── public/         # Publicly accessible PHP entry point
├── public/             # Web server document root (publicly accessible files)
│   ├── css/            # Stylesheets
│   ├── js/             # JavaScript files
│   ├── uploads/        # Directory for uploaded asset files (managed by PHP)
│   ├── previews/       # Directory for generated image thumbnails (managed by PHP)
│   ├── index.php       # Main application entry point
│   └── .htaccess       # Apache configuration (optional)
├── var/                # Volatile data: logs, cache (excluded from version control)
├── docs/               # Project documentation, including database schema
├── nginx/              # Nginx configuration for Docker
├── .gitignore          # Git ignore file
├── composer.json       # Composer dependency definitions
├── LICENSE             # Project license
├── sample.env          # Environment variables template for Docker Compose (COPY TO .env)
├── .env                # Environment variables for Docker Compose (DO NOT COMMIT)
├── docker-compose.yml  # Docker Compose setup
└── install.sh          # Installation script
```

## Installation with Docker Compose (Recommended)

This is the recommended way to run Farm, providing a consistent and isolated environment.

**Prerequisites:**

* **Docker Desktop** (for Windows/macOS) or **Docker Engine & Docker Compose** (for Linux) installed and running.

**Steps:**

1.  **Create the project directory:**
    Create a new directory named `farm` on your local machine.

2.  **Create the file structure and populate files:**
    Inside the `farm` directory, manually create all the subdirectories (e.g., `config/`, `src/lib/`, `public/css/`, `nginx/`, etc.) as shown in the "Project Structure" above.

    Then, copy and paste the content for each file (including the **new** `farm/public/index.php`, `farm/docker-compose.yml`, `farm/sample.env`, and `farm/nginx/nginx.conf`) into its corresponding newly created file.

    **Crucially, update your `farm/.env` file with your desired strong passwords and database names.**

3.  **Run the initial setup script (optional, for host directories):**
    Open your terminal or command prompt, navigate to the `farm` directory, and run:
    ```bash
    chmod +x install.sh
    ./install.sh
    ```
    This script will:
    * Create the necessary `public/uploads`, `public/previews`, `var/logs`, and `var/cache` directories on your host machine.
    * Copy `sample.env` to `.env` if `.env` doesn't exist or if you choose to overwrite it.
    * **Move `src/index.php` to `public/index.php` if it's still in `src/`.**

4.  **Review and update `.env`:**
    Open the newly created (or updated) `.env` file in your `farm` project root. **Replace the placeholder values for `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, and `APP_PORT`** with your desired credentials and the port you'd like the application to run on (e.g., `APP_PORT=8080`).

5.  **Build and start the Docker services:**
    From your `farm` project root directory, run:
    ```bash
    sudo docker compose up --build -d
    ```
    * `--build`: This ensures that your PHP application image is built from the `Dockerfile`. You only need this the first time or if you change the `Dockerfile`.
    * `-d`: Runs the containers in detached mode (in the background).

    This command will:
    * Build the `app` (PHP-FPM) Docker image.
    * Start the `nginx` web server container, configured to serve your application on the port specified in `.env` (defaulting to 6118).
    * Start the `db` (MySQL) database container.
    * **Automatically initialize the MySQL database** by importing `docs/database.sql` on the *first run* of the `db` service.
    * Set up networking between the containers.

6.  **Access the application:**
    Open your web browser and navigate to `http://localhost:6118` (or `http://localhost:YOUR_APP_PORT` if you changed `APP_PORT` from 6118).

**To stop and remove containers (and networks/volumes by default):**
```bash
docker compose down
```
*(If you want to remove the database data volume as well, use `docker compose down -v`)*

## Usage

* **Upload:** Use the "Upload New Asset" form to add new digital game assets. You can now provide the asset name, source URL, store, author, license, and initial tags/projects directly during upload. Zip files will be automatically extracted, and content will be processed and tagged.
* **Browse:** View a list of all your assets. Click "Details" to see more information about a specific asset, including its associated files, tags, and projects.
* **Search:** Use the search form to find assets by asset name, associated tags, or file type.

## Contributing

Contributions are welcome! Please feel free to open a bug report, suggest a feature, or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.