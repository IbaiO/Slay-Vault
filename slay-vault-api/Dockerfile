FROM php:8.3-apache

RUN apt-get update && apt-get install -y libcurl4-openssl-dev && rm -rf /var/lib/apt/lists/*

RUN docker-php-ext-install mysqli curl

WORKDIR /var/www/html

# Create uploads directory with correct permissions for Apache
RUN mkdir -p /var/www/html/slay-vault-api/uploads/users && \
    mkdir -p /var/www/html/slay-vault-api/uploads/queens && \
    chown -R www-data:www-data /var/www/html/slay-vault-api/uploads && \
    chmod -R 775 /var/www/html/slay-vault-api/uploads

