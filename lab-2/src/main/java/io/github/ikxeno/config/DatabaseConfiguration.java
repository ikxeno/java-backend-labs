package io.github.ikxeno.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.util.Properties;

public final class DatabaseConfiguration {
    private DatabaseConfiguration() {
    }

    public static HikariDataSource openDataSource() throws IOException {
        Properties properties = new Properties();
        try (var input = DatabaseConfiguration.class
                .getResourceAsStream("/database.properties")) {
            if (input == null) {
                throw new IOException("Missing database.properties");
            }
            properties.load(input);
        }
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(setting(properties, "DB_URL"));
        configuration.setUsername(setting(properties, "DB_USER"));
        configuration.setPassword(setting(properties, "DB_PASSWORD"));
        configuration.setSchema(setting(properties, "DB_SCHEMA"));
        configuration.setMaximumPoolSize(
                Integer.parseInt(properties.getProperty("pool.maximum-size")));
        configuration.setConnectionTimeout(Long
                .parseLong(properties.getProperty("pool.connection-timeout")));
        return new HikariDataSource(configuration);
    }

    public static void migrate(HikariDataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .resourceProvider(new MigrationResources())
                .defaultSchema(dataSource.getSchema())
                .schemas(dataSource.getSchema())
                .load()
                .migrate();
    }

    private static String setting(Properties properties, String name) {
        String value = System.getenv(name);
        return value == null ? properties.getProperty(name) : value;
    }
}
