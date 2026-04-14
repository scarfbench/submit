#!/bin/bash
set -e

# Start PostgreSQL
pg_ctlcluster $(pg_lsclusters -h | head -1 | awk '{print $1, $2}') start

# Wait for PostgreSQL to be ready
for i in $(seq 1 30); do
    if pg_isready -q 2>/dev/null; then
        echo "PostgreSQL is ready"
        break
    fi
    echo "Waiting for PostgreSQL... ($i/30)"
    sleep 1
done

# Create database and user
su - postgres -c "psql -c \"CREATE USER postgres_user WITH PASSWORD 'S3cret';\"" 2>/dev/null || true
su - postgres -c "psql -c \"CREATE DATABASE postgres_db OWNER postgres_user;\"" 2>/dev/null || true
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE postgres_db TO postgres_user;\"" 2>/dev/null || true

# Export environment variables for Quarkus
export POSTGRESQL_HOSTNAME=localhost
export POSTGRESQL_PORT=5432
export POSTGRESQL_DBNAME=postgres_db
export POSTGRESQL_USER=postgres_user
export POSTGRESQL_PW=S3cret

# Run the Quarkus uber-jar
echo "Starting Quarkus application..."
exec java -jar /app/target/realworld-quarkus-runner.jar
