package com.bank.config;


import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new RuntimeException("Unable to find db.properties in classpath");
            }
            prop.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(prop.getProperty("db.url"));
            config.setUsername(prop.getProperty("db.username"));
            config.setPassword(prop.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(prop.getProperty("db.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(prop.getProperty("db.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(prop.getProperty("db.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(prop.getProperty("db.connectionTimeout", "20000")));

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Database Connection Pool Initialization Failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}