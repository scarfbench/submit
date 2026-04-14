#!/bin/bash
set -e

# Start PostgreSQL
pg_ctlcluster $(pg_lsclusters -h | head -1 | awk '{print $1, $2}') start

# Create database and user
su - postgres -c "psql -c \"CREATE USER postgres_user WITH PASSWORD 'S3cret';\"" 2>/dev/null || true
su - postgres -c "psql -c \"CREATE DATABASE postgres_db OWNER postgres_user;\"" 2>/dev/null || true
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE postgres_db TO postgres_user;\"" 2>/dev/null || true

# Build the Spring Boot application (skip tests for faster startup)
mvn clean package -DskipTests -q

# Run the Spring Boot JAR
exec java -jar target/realworld-spring-1.0.0.jar
