#!/bin/bash

# Script to install the Farm Digital Asset Manager
# This version is simplified for Docker Compose setup.

# Stop on first error
set -e

# Define directories
APP_DIR="$PWD"  # The current directory
PUBLIC_DIR="$APP_DIR/public"
VAR_DIR="$APP_DIR/var"
SRC_DIR="$APP_DIR/src"
PHP_FPM_DIR="$APP_DIR/php-fpm" # New directory for PHP-FPM config
DOCS_DIR="$APP_DIR/docs" # Path to docs, including database.sql
NGINX_DIR="$APP_DIR/nginx" # Path to nginx config

# Check if the script is being run in the correct directory
if [ ! -d "$SRC_DIR" ] || [ ! -d "$PUBLIC_DIR" ] || [ ! -d "$DOCS_DIR" ] || [ ! -d "$NGINX_DIR" ]; then
  echo "Error: This script must be run in the project's root directory."
  echo "Please navigate to the project's root directory and try again."
  exit 1
fi

# Create the uploads and previews directories and set permissions
echo "Creating and setting permissions for uploads and previews directories on host..."
mkdir -p "$PUBLIC_DIR/uploads"
mkdir -p "$PUBLIC_DIR/previews"
# These permissions will be set inside the Docker container by the Dockerfile as well,
# but setting them on the host ensures Docker can bind-mount correctly without issues.
chmod -R 0777 "$PUBLIC_DIR/uploads"
chmod -R 0777 "$PUBLIC_DIR/previews"

# Create the var directories
echo "Creating var directories on host..."
mkdir -p "$VAR_DIR/logs"
mkdir -p "$VAR_DIR/cache"
chmod -R 0777 "$VAR_DIR/logs"
chmod -R 0777 "$VAR_DIR/cache"

# Create php-fpm config directory
echo "Creating php-fpm config directory on host..."
mkdir -p "$PHP_FPM_DIR"

# Handle .env file
ENV_SAMPLE_FILE="$APP_DIR/sample.env"
ENV_FILE="$APP_DIR/.env"

if [ -f "$ENV_SAMPLE_FILE" ]; then
  if [ -f "$ENV_FILE" ]; then
    read -p "'.env' already exists. Overwrite with '$ENV_SAMPLE_FILE'? (y/n) " overwrite_env
    if [[ "$overwrite_env" == "y" ]]; then
      cp "$ENV_SAMPLE_FILE" "$ENV_FILE"
      echo "'.env' overwritten from '$ENV_SAMPLE_FILE'."
      echo "IMPORTANT: Please review and update database credentials in '$ENV_FILE'."
    else
      echo "Skipping '.env' creation. Ensure your existing '.env' is correctly configured."
    fi
  else
    cp "$ENV_SAMPLE_FILE" "$ENV_FILE"
    echo "'.env' created from '$ENV_SAMPLE_FILE'."
    echo "IMPORTANT: Please review and update database credentials in '$ENV_FILE'."
  fi
else
  echo "Warning: '$ENV_SAMPLE_FILE' not found. You will need to create a '.env' file manually."
  echo "Refer to 'docker-compose.yml' for required environment variables."
fi

# Move index.php to public/ if it's in src/
if [ -f "$SRC_DIR/index.php" ]; then
    echo "Moving src/index.php to public/index.php..."
    mv "$SRC_DIR/index.php" "$PUBLIC_DIR/index.php"
    echo "Done. All public requests will now be handled by public/index.php."
fi

echo "Installation setup complete!"
echo " "
echo "Next steps for Docker Compose environment:"
echo "1.  Ensure you have Docker and Docker Compose installed."
echo "2.  Review and update the '.env' file in your project root with your desired database credentials (if you haven't already)."
echo "3.  From the project root ('$APP_DIR'), run:  docker compose up --build -d"
echo "4.  Access the application in your browser at: http://localhost:$(grep -E '^APP_PORT=' "$ENV_FILE" | cut -d'=' -f2 || echo "6118")"
echo " "
echo "Note: The database schema ('$DOCS_DIR/database.sql') will be automatically imported by the MySQL Docker container on its first run."
exit 0