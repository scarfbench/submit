#!/bin/bash
set -e

# Detect and start PostgreSQL
PG_VERSION=$(ls /etc/postgresql/ 2>/dev/null | head -1)
if [ -n "$PG_VERSION" ]; then
    echo "Starting PostgreSQL version $PG_VERSION..."
    pg_ctlcluster "$PG_VERSION" main start || service postgresql start || true
else
    service postgresql start || true
fi
sleep 3

# Create database user and database
su - postgres -c "psql -c \"CREATE USER postgres_user WITH PASSWORD 'S3cret';\"" || true
su - postgres -c "psql -c \"CREATE DATABASE postgres_db OWNER postgres_user;\"" || true
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE postgres_db TO postgres_user;\"" || true

export POSTGRESQL_HOSTNAME=localhost
export POSTGRESQL_PORT=5432
export POSTGRESQL_DBNAME=postgres_db
export POSTGRESQL_USER=postgres_user
export POSTGRESQL_PW=S3cret

# Build the Quarkus application (skip tests during build)
echo "Building Quarkus application..."
mvn package -DskipTests -q 2>&1

# Start the application - Quarkus uses target/quarkus-app/quarkus-run.jar
echo "Starting the Quarkus application..."
if [ -f target/quarkus-app/quarkus-run.jar ]; then
    java -jar target/quarkus-app/quarkus-run.jar &
elif [ -f target/realworld-quarkus-runner.jar ]; then
    java -jar target/realworld-quarkus-runner.jar &
else
    echo "ERROR: Could not find Quarkus jar. Contents of target/:"
    ls -la target/ 2>/dev/null || true
    ls -la target/quarkus-app/ 2>/dev/null || true
    exit 1
fi
APP_PID=$!

# Wait for the application to start
echo "Waiting for application to start..."
for i in $(seq 1 90); do
    if curl -s http://localhost:8080/api/tags > /dev/null 2>&1; then
        echo "Application started successfully on port 8080"
        break
    fi
    if ! kill -0 $APP_PID 2>/dev/null; then
        echo "ERROR: Application process died"
        exit 1
    fi
    sleep 2
done

# Run smoke tests if smoke.py exists
if [ -f smoke.py ]; then
    echo "Running smoke tests..."
    python3 smoke.py
    SMOKE_EXIT=$?
    echo "Smoke tests finished with exit code: $SMOKE_EXIT"
fi

# Keep the container running
wait $APP_PID
