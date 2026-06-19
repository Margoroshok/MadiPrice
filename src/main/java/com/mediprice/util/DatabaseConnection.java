package com.mediprice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Database connection utility - manages SQLite JDBC connections.
 * Uses a single shared connection (single-user desktop app pattern).
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DB_URL = "jdbc:sqlite:mediprice.db";
    private static Connection connection;

    private DatabaseConnection() {}

    /**
     * Returns the shared connection, creating it if necessary.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Enable WAL mode for better concurrency
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA foreign_keys=ON");
                stmt.execute("PRAGMA synchronous=NORMAL");
            }
            LOGGER.info("Database connection established.");
        }
        return connection;
    }

    /**
     * Initializes the database schema and loads test data on first run.
     */
    public static void initializeDatabase() {
        try {
            executeSqlFile("/db/schema.sql");
            LOGGER.info("Schema initialized successfully.");

            // Only seed data if tables are empty
            Connection conn = getConnection();
            var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                executeSqlFile("/db/data.sql");
                LOGGER.info("Sample data loaded successfully.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Executes a SQL file from the classpath resources.
     */
    private static void executeSqlFile(String resourcePath) throws IOException, SQLException {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Strip full-line comments before splitting, so a leading
            // "-- comment" line doesn't cause an entire multi-line
            // statement to be mistaken for a comment-only chunk.
            String cleanedSql = sql.lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            Connection conn = getConnection();
            // Split on semicolons (skip empty statements)
            String[] statements = cleanedSql.split(";");
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    conn.createStatement().execute(trimmed);
                }
            }
        }
    }

    /**
     * Closes the shared connection.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }
}
