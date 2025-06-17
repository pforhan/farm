# Use an official PHP image as the base
FROM php:8.2-fpm-alpine

# Install necessary PHP extensions
# alpine is used for smaller image size, so use apk instead of apt
# gd: for image manipulation (thumbnails)
# mysqli: for MySQL database connection
# zip: for ZipArchive to handle zip uploads
# fileinfo: for mime_content_type
RUN apk add --no-cache \
    libzip-dev \
    libpng-dev \
    libjpeg-turbo-dev \
    freetype-dev \
    zlib-dev \
    mysql-client \
    git \
    supervisor \
    nginx

# Install PHP extensions
RUN docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install -j$(nproc) gd mysqli pdo_mysql zip exif pcntl fileinfo

# Set working directory to /var/www/html which is the default for PHP-FPM images
WORKDIR /var/www/html

# Copy your application code into the container
# . means current directory on host (farm/)
# /var/www/html is the WORKDIR in the container
COPY . /var/www/html

# Adjust permissions for the web server (Nginx/Apache) to be able to write to uploads and previews
# This is crucial for file uploads and thumbnail generation.
# Ensure 'config' directory is also owned by www-data if config.php isn't writable by root after copy
RUN chown -R www-data:www-data /var/www/html/public/uploads \
    && chown -R www-data:www-data /var/www/html/public/previews \
    && chown -R www-data:www-data /var/www/html/var/logs \
    && chown -R www-data:www-data /var/www/html/var/cache \
    && chmod -R 777 /var/www/html/public/uploads \
    && chmod -R 777 /var/www/html/public/previews \
    && chmod -R 777 /var/www/html/var/logs \
    && chmod -R 777 /var/www/html/var/cache

# Expose port 9000 for PHP-FPM (Nginx will connect to this)
EXPOSE 9000

# Start PHP-FPM
CMD ["php-fpm"]