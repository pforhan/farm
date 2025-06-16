#!/bin/bash

# Script to install the Farm Digital Asset Manager

# Stop on first error
set -e

# Check for required commands
command -v composer >/dev/null 2>&1 || { echo >&2 "Composer is required.  Please install it: [https://getcomposer.org](https://getcomposer.org)"; exit 1; }
command -v mysql >/dev/null 2>&1 || { echo >&2 "MySQL client is required."; exit 1; }

# Define directories
APP_DIR="$PWD"  # The current directory
CONFIG_DIR="$APP_DIR/config"
PUBLIC_DIR="$APP_DIR/public"
VAR_DIR="$APP_DIR/var"

# Check if the script is being run in the correct directory
if [ ! -d "$CONFIG_DIR" ] || [ ! -d "$APP_DIR/src" ] || [ ! -d "$PUBLIC_DIR" ]; then
  echo "Error: This script must be run in the project's root directory."
  echo "Please navigate to the project's root directory and try again."
  exit 1
fi

# Check if config.php exists, if so, prompt to overwrite or skip
if [ -f "$CONFIG_DIR/config.php" ]; then
  read -p "config.php already exists. Overwrite? (y/n) " overwrite
  if [[ "$overwrite" != "y" ]]; then
    echo "Skipping config.php creation."
  else
    echo "Overwriting config.php"
    cp "$CONFIG_DIR/config.php.dist" "$CONFIG_DIR/config.php"
  fi
else
    # Copy the configuration template
    echo "Creating config.php"
    cp "$CONFIG_DIR/config.php.dist" "$CONFIG_DIR/config.php"
fi

# Prompt for database details and update config.php
echo "Setting up database configuration..."
read -p "Enter database host (default: localhost): " DB_HOST
DB_HOST=${DB_HOST:-"localhost"} #default
read -p "Enter database name: " DB_NAME
read -p "Enter database user: " DB_USER
read -sp "Enter database password: " DB_PASS
echo "" # Add a newline

# Update the config.php file
sed -i "s/your_db_host/$DB_HOST/g" "$CONFIG_DIR/config.php"
sed -i "s/your_db_name/$DB_NAME/g" "$CONFIG_DIR/config.php"
sed -i "s/your_db_user/$DB_USER/g" "$CONFIG_DIR/config.php"
sed -i "s/your_db_password/$(echo "$DB_PASS" | sed 's/[\\\/&]/\\&/g')/g" "$CONFIG_DIR/config.php" #escape

# Create the uploads and previews directories and set permissions
echo "Creating and setting permissions for uploads and previews directories..."
mkdir -p "$PUBLIC_DIR/uploads"
mkdir -p "$PUBLIC_DIR/previews"
chmod -R 0777 "$PUBLIC_DIR/uploads" # Make them writable by the web server
chmod -R 0777 "$PUBLIC_DIR/previews"

# Create the var directories
echo "Creating var directories..."
mkdir -p "$VAR_DIR/logs"
mkdir -p "$VAR_DIR/cache"

# Install Composer dependencies
echo "Installing Composer dependencies..."
composer install --no-interaction --optimize-autoloader --working-dir="$APP_DIR"

# Set up the database schema (import SQL)
echo "Setting up the database schema..."
read -p "Enter path to the database schema file (default: docs/database.sql): " SCHEMA_FILE
SCHEMA_FILE=${SCHEMA_FILE:-"$APP_DIR/docs/database.sql"}
if [ ! -f "$SCHEMA_FILE" ]; then
  echo "Error: Database schema file not found at $SCHEMA_FILE"
  exit 1
fi
mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SCHEMA_FILE"

echo "Installation complete!"
echo " "
echo "Next steps:"
echo "1.  Edit the config/config.php file to customize your installation (if you did not do so above)."
echo "2.  Configure your web server to point to the public directory ($PUBLIC_DIR)."
echo "3.  Browse to your website."
exit 0