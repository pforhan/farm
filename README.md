# Farm: Digital Asset Manager

NOTE: EVERYTHING BELOW AND EVERY FILE IN THIS PROJECT IS AI-GENERATED AND NOT CONFIRMED YET.  USE AT YOUR OWN RISK.

## Files, Assets, Resources, Metadata

Farm is a simple, open-source web application for tracking and managing your digital  assets. It allows you to organize assets by store (source), author, license, tags, and projects, and provides a way to upload files (including zip archives), preview graphics, and play audio.

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
├── config/             # Configuration files (copy config.php.dist to config.php)
├── src/                # Core application source code
│   ├── lib/            # Core PHP functions
│   ├── templates/      # HTML templates
│   └── index.php       # Front controller (main entry point)
├── public/             # Web server document root (publicly accessible files)
│   ├── css/            # Stylesheets
│   ├── js/             # JavaScript files
│   ├── uploads/        # Directory for uploaded asset files (managed by PHP)
│   ├── previews/       # Directory for generated image thumbnails (managed by PHP)
│   └── .htaccess       # Apache configuration (optional)
├── var/                # Volatile data: logs, cache (excluded from version control)
├── docs/               # Project documentation, including database schema
├── .gitignore          # Git ignore file
├── composer.json       # Composer dependency definitions
├── LICENSE             # Project license
└── install.sh          # Installation script
```

## Installation

**Prerequisites:**

* Web server (Apache, Nginx, etc.)
* PHP (>= 7.4) with `mysqli`, `zip`, `gd`, and `fileinfo` extensions enabled.
* MySQL/MariaDB database server.
* Composer (PHP dependency manager).
* MySQL client command-line tool.

**Steps:**

1.  **Create the project directory:**
    Create a new directory named `farm` on your local machine.

2.  **Create the file structure and populate files:**
    Inside the `farm` directory, manually create all the subdirectories (e.g., `config/`, `src/lib/`, `public/css/`, etc.) as shown in the "Project Structure" above. Then, copy and paste the content for each file into its corresponding newly created file.

3.  **Make the install script executable:**
    Open your terminal or command prompt, navigate to the `farm` directory, and run:
    ```bash
    chmod +x install.sh
    ```

4.  **Run the installation script:**
    ```bash
    ./install.sh
    ```
    The script will:
    * Check for required software.
    * Copy `config/config.php.dist` to `config/config.php`.
    * Prompt you for database connection details (host, name, user, password) and update `config/config.php`.
    * Create the necessary `public/uploads`, `public/previews`, `var/logs`, and `var/cache` directories.
    * Install PHP dependencies via Composer (including `ext-fileinfo` for `mime_content_type`).
    * Import the database schema (`docs/database.sql`) into your MySQL database.

5.  **Configure your web server:**
    Point your web server's document root to the `public/` directory within the `farm` project.

    **Example (Apache):**
    Add a Virtual Host entry similar to this (adjust `DocumentRoot` and `Directory` paths):
    ```apache
    <VirtualHost *:80>
        ServerName farm.localhost
        DocumentRoot /path/to/your/farm/public

        <Directory /path/to/your/farm/public>
            AllowOverride All
            Require all granted
        </Directory>

        ErrorLog ${APACHE_LOG_DIR}/farm_error.log
        CustomLog ${APACHE_LOG_DIR}/farm_access.log combined
    </VirtualHost>
    ```
    Remember to enable `mod_rewrite` if you plan to use clean URLs (not implemented yet, but good practice for future).

6.  **Access the application:**
    Open your web browser and navigate to the configured URL (e.g., `http://farm.localhost`).

## Usage

* **Upload:** Use the "Upload New Asset" form to add new digital game assets. You can now provide the asset name, source URL, store, author, license, and initial tags/projects directly during upload. Zip files will be automatically extracted, and content will be processed and tagged.
* **Browse:** View a list of all your assets. Click "Details" to see more information about a specific asset, including its associated files, tags, and projects.
* **Search:** Use the search form to find assets by asset name, associated tags, or file type.

## Contributing

Contributions are welcome! Please feel free to open a bug report, suggest a feature, or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.