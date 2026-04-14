#!/bin/bash
# Startup script for WildFly with PetClinic

# Configure the datasource
$JBOSS_HOME/bin/jboss-cli.sh --file=/opt/wildfly-config.cli

# Copy the WAR to deployments
cp /opt/petclinic.war $JBOSS_HOME/standalone/deployments/

# Copy import.sql to a location accessible by the app
cp /opt/import.sql $JBOSS_HOME/standalone/

# Start WildFly
exec $JBOSS_HOME/bin/standalone.sh -b 0.0.0.0
