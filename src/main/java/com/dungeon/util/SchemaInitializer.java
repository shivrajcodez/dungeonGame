package com.dungeon.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs automatically on webapp startup (no web.xml entry needed — uses @WebListener).
 *
 * If the `users` table does not exist, reads schema.sql from WEB-INF/sql/
 * and executes it — making the app fully self-seeding on Railway.
 *
 * Retries up to 10 times (3 s apart) so it handles Docker/Railway DB
 * startup race conditions gracefully.
 */
@WebListener
public class SchemaInitializer implements ServletContextListener {

    private static final int  MAX_RETRIES   = 10;
    private static final long RETRY_DELAY_MS = 3_000;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[SchemaInitializer] Checking database schema...");

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (Connection conn = DBConnection.getConnection()) {

                if (tablesExist(conn)) {
                    System.out.println("[SchemaInitializer] ✅ Tables exist — skipping init.");
                    return;
                }

                System.out.println("[SchemaInitializer] Tables missing — running schema.sql...");
                runSchema(conn, sce);
                System.out.println("[SchemaInitializer] ✅ Schema initialised successfully.");
                return;

            } catch (Exception e) {
                System.err.printf("[SchemaInitializer] Attempt %d/%d failed: %s%n",
                        attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                } else {
                    System.err.println("[SchemaInitializer] ❌ Could not initialise schema after "
                            + MAX_RETRIES + " attempts. Check DB connection.");
                }
            }
        }
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private boolean tablesExist(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, "users", new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private void runSchema(Connection conn, ServletContextEvent sce) throws Exception {
        // Look for schema.sql packaged inside the WAR at WEB-INF/sql/schema.sql
        InputStream is = sce.getServletContext()
                .getResourceAsStream("/WEB-INF/sql/schema.sql");

        if (is == null) {
            // Fallback: classpath root (e.g. src/main/resources/schema.sql)
            is = getClass().getClassLoader().getResourceAsStream("schema.sql");
        }
        if (is == null) {
            throw new RuntimeException(
                "schema.sql not found in /WEB-INF/sql/ — cannot initialise database.");
        }

        List<String> statements = parseSql(is);
        System.out.println("[SchemaInitializer] Executing " + statements.size() + " SQL statements...");

        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                if (!sql.isBlank()) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException ex) {
                        String msg = ex.getMessage();
                        // Ignore "already exists" — makes init idempotent
                        if (msg != null && msg.toLowerCase().contains("already exists")) {
                            System.out.println("[SchemaInitializer] Skipping (already exists): "
                                    + sql.substring(0, Math.min(60, sql.length())).trim() + "...");
                        } else {
                            // Log but don't abort — best-effort
                            System.err.println("[SchemaInitializer] Warning: " + msg);
                        }
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Parses a SQL file into individual statements split on ';'.
     * Skips -- comments, CREATE DATABASE, USE, DROP DATABASE statements
     * (Railway already provides the DB — we just need the tables).
     */
    private List<String> parseSql(InputStream is) throws Exception {
        List<String> statements = new ArrayList<>();
        StringBuilder current   = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String upper   = line.trim().toUpperCase();
                String trimmed = line.trim();

                // Skip comment lines
                if (trimmed.startsWith("--") || trimmed.startsWith("#")) continue;

                // Skip DB-level DDL (Railway owns the database)
                if (upper.startsWith("CREATE DATABASE")
                 || upper.startsWith("DROP DATABASE")
                 || upper.startsWith("USE ")) continue;

                // Strip inline comments
                int dash = line.indexOf("--");
                if (dash >= 0) line = line.substring(0, dash);

                current.append(line).append("\n");

                if (trimmed.endsWith(";")) {
                    String s = current.toString().trim();
                    if (!s.isBlank()) statements.add(s);
                    current.setLength(0);
                }
            }
            // Flush anything without a trailing semicolon
            String leftover = current.toString().trim();
            if (!leftover.isBlank()) statements.add(leftover);
        }
        return statements;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) { /* nothing */ }
}
