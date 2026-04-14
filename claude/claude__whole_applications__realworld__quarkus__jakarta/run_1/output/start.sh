#!/bin/bash
set -e

echo "Starting Jakarta EE RealWorld application..."

# Run the fat JAR
exec java -jar /app/target/realworldapiservice-1.0-SNAPSHOT.jar
