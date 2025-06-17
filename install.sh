#!/bin/bash

# Script to install the Farm Digital Asset Manager
# This version is simplified for Docker Compose setup.

# Stop on first error
set -e

# Define directories
APP_DIR="$PWD"  # The current directory
PUBLIC_DIR="$APP_DIR/public"
VAR_DIR="$APP_DIR/var"
DOCS_DIR="$APP_DIR/docs" # Path to docs, including database.sql
NGINX_DIR="$APP_DIR/nginx" # Path to nginx config

# Check if the script is being run in the correct directory
if [ ! -d "$APP_DIR/src" ] || [ ! -d "$PUBLIC_DIR" ] || [ ! -d "$DOCS_DIR" ] || [ ! -d "$NGINX_DIR" ]; then
  echo "Error: This script must be run in the project's root directory."
  echo "Please navigate to the project's root directory and try again."
  exit 1
fi

# Create the uploads and previews directories and set permissions
echo "Creating and setting permissions for uploads and previews directories..."
mkdir -p "$PUBLIC_DIR/uploads"
mkdir -p "$PUBLIC_DIR/previews"
# These permissions will be set inside the Docker container by the Dockerfile as well,
# but it's good to have them on the host too if working directly.
chmod -R 0777 "$PUBLIC_DIR/uploads"
chmod -R 0777 "$PUBLIC_DIR/previews"

# Create the var directories
echo "Creating var directories..."
mkdir -p "$VAR_DIR/logs"
mkdir -p "$VAR_DIR/cache"
chmod -R 0777 "$VAR_DIR/logs"
chmod -R 0777 "$VAR_DIR/cache"

echo "Installation setup complete!"
echo " "
echo "Next steps for Docker Compose environment:"
echo "1.  Ensure you have Docker and Docker Compose installed."
echo "2.  Review and update the '.env' file in your project root with your desired database credentials."
echo "3.  From the project root ('$APP_DIR'), run:  docker-compose up --build -d"
echo "4.  Access the application in your browser at: http://localhost"
echo " "
echo "For manual database import (if not using Docker Compose init):"
echo "   You can still manually import '$DOCS_DIR/database.sql' into your MySQL database."
exit 0