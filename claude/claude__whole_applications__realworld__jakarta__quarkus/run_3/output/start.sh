#!/bin/bash

echo "=== Starting PostgreSQL ==="
pg_ctlcluster $(pg_lsclusters -h | head -1 | awk '{print $1, $2}') start

# Wait for PostgreSQL to be ready
echo "=== Waiting for PostgreSQL ==="
for i in $(seq 1 30); do
    if su - postgres -c "pg_isready" 2>/dev/null; then
        echo "PostgreSQL is ready"
        break
    fi
    sleep 1
done

# Create database and user
echo "=== Setting up database ==="
su - postgres -c "psql -c \"CREATE USER postgres_user WITH PASSWORD 'S3cret' SUPERUSER;\"" 2>/dev/null || true
su - postgres -c "psql -c \"CREATE DATABASE postgres_db OWNER postgres_user;\"" 2>/dev/null || true
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE postgres_db TO postgres_user;\"" 2>/dev/null || true

# Set environment variables
export POSTGRESQL_HOSTNAME=localhost
export POSTGRESQL_PORT=5432
export POSTGRESQL_DBNAME=postgres_db
export POSTGRESQL_USER=postgres_user
export POSTGRESQL_PW=S3cret

echo "=== Starting Quarkus Application ==="
# Run the Quarkus application
java -jar /app/target/quarkus-app/quarkus-run.jar &
APP_PID=$!

# Wait for app to be ready
echo "=== Waiting for application ==="
APP_READY=false
for i in $(seq 1 60); do
    if curl -s http://localhost:8080/api/tags > /dev/null 2>&1; then
        echo "Application is ready!"
        APP_READY=true
        break
    fi
    sleep 2
done

if [ "$APP_READY" = "false" ]; then
    echo "=== Application failed to start ==="
    exit 1
fi

# Run smoke tests (don't exit on failure so container stays up)
echo "=== Running Smoke Tests ==="
python3 /app/smoke.py http://localhost:8080
TEST_EXIT=$?

if [ "$TEST_EXIT" -eq 0 ]; then
    echo "=== Smoke tests passed! ==="
else
    echo "=== Smoke tests failed with exit code $TEST_EXIT ==="
fi

# Keep the container running
wait $APP_PID
