<?php
// config/config.php

// Database Configuration - Reads directly from environment variables set by Docker Compose
// These names correspond to the environment variables set in your docker-compose.yml and .env file.
define('DB_HOST', getenv('MYSQL_HOST') ?: 'db'); // 'db' is the service name in docker-compose.yml
define('DB_USER', getenv('MYSQL_USER') ?: 'farm_user'); // Default provided for clarity, but expect it from .env
define('DB_PASS', getenv('MYSQL_PASSWORD') ?: 'farm_password'); // Default provided for clarity, but expect it from .env
define('DB_NAME', getenv('MYSQL_DATABASE') ?: 'farm_db'); // Default provided for clarity, but expect it from .env

// File System Configuration
// Ensure these paths are absolute and writable by your web server.
// These paths are relative to the project root for better portability.
define('UPLOAD_DIR', __DIR__ . '/../public/uploads/'); // Absolute path, writable by PHP
define('PREVIEW_DIR', __DIR__ . '/../public/previews/'); // Absolute path, writable by PHP
define('MAX_FILE_SIZE', 20 * 1024 * 1024); // 20MB max upload size
define('ALLOWED_TYPES', ['zip', 'png', 'jpg', 'jpeg', 'gif', 'wav', 'mp3', 'ogg', 'txt', 'md', 'html', 'json', 'xml']); // Added text-based file types

?>