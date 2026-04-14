package org.eclipse.cargotracker.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;

@DataSourceDefinition(
    name = "java:app/jdbc/CargoTrackerDatabase",
    className = "org.h2.jdbcx.JdbcDataSource",
    url = "jdbc:h2:mem:cargo-tracker-database;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    user = "sa",
    password = ""
)
@ApplicationScoped
public class DataSourceConfiguration {
}
