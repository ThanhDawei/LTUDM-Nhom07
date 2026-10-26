package com.quizzlive.server.database;

import com.quizzlive.server.config.ServerConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static HikariDataSource dataSource;
    private static boolean isInitialized = false;

    public static synchronized void initialize() {
        if (isInitialized) return;

        System.out.println("[DatabaseManager] Initializing database connection pool...");
        HikariConfig config = new HikariConfig();

        boolean useMysql = true;
        try {
            config.setDriverClassName(ServerConfig.getDbDriver());
            config.setJdbcUrl(ServerConfig.getDbUrl());
            config.setUsername(ServerConfig.getDbUsername());
            config.setPassword(ServerConfig.getDbPassword());
            config.setMaximumPoolSize(15);
            config.setMinimumIdle(3);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(5000);

            dataSource = new HikariDataSource(config);
            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                String dbType = ServerConfig.getDbUrl().contains("postgresql") ? "PostgreSQL" : "MySQL";
                System.out.println("[DatabaseManager] Successfully connected to " + dbType + " Database!");
            }
        } catch (Exception e) {
            String dbType = ServerConfig.getDbUrl().contains("postgresql") ? "PostgreSQL" : "MySQL";
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("[DatabaseManager] KHÔNG THỂ KẾT NỐI TỚI " + dbType.toUpperCase() + "!");
            System.err.println("[DatabaseManager] Nguyên nhân: " + e.getMessage());
            System.err.println("[DatabaseManager] -> Hệ thống đang tạm thời chuyển sang H2 Database (lưu tạm trong RAM).");
            System.err.println("[DatabaseManager] -> Dữ liệu trên pgAdmin sẽ KHÔNG có cho đến khi bạn sửa đúng mật khẩu trong application.properties!");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            useMysql = false;
            if (dataSource != null) {
                dataSource.close();
            }

            HikariConfig h2Config = new HikariConfig();
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setJdbcUrl("jdbc:h2:mem:quizzlive_db;DB_CLOSE_DELAY=-1;MODE=MySQL");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(h2Config);
            System.out.println("[DatabaseManager] Connected to in-memory H2 Database (MySQL mode).");
        }

        if (ServerConfig.isDbAutoInit() || !useMysql) {
            if (useMysql && ServerConfig.getDbUrl().contains("postgresql")) {
                executeSqlScript("database/schema_postgres.sql");
                executeSqlScript("database/seed_data_postgres.sql");
            } else {
                executeSqlScript("database/schema.sql");
                executeSqlScript("database/seed_data.sql");
            }
        }

        isInitialized = true;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static void executeSqlScript(String resourcePath) {
        System.out.println("[DatabaseManager] Running SQL script: " + resourcePath);
        try (InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[DatabaseManager] Script not found: " + resourcePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("//")) {
                        continue;
                    }
                    // Strip inline SQL comment: -- ...
                    int commentIdx = line.indexOf("--");
                    if (commentIdx >= 0) {
                        line = line.substring(0, commentIdx).trim();
                    }
                    if (line.isEmpty()) {
                        continue;
                    }
                    // Skip USE quizzlive_db or CREATE DATABASE in H2/Postgres
                    if (line.toUpperCase().startsWith("CREATE DATABASE") || line.toUpperCase().startsWith("USE ")) {
                        continue;
                    }
                    sqlBuilder.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String sql = sqlBuilder.toString().trim();
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 1).trim();
                        }
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException ex) {
                                // Ignore duplicate key or existing table notices
                            }
                        }
                        sqlBuilder.setLength(0);
                    }
                }
                System.out.println("[DatabaseManager] Executed " + resourcePath + " successfully.");
            }
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Error running script " + resourcePath + ": " + e.getMessage());
        }
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DatabaseManager] Connection pool closed.");
        }
    }
}
