package com.dungeon.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        String rawUrl = env("MYSQL_URL", null);
        if (rawUrl != null && rawUrl.startsWith("mysql://")) {
            try {
                String rest  = rawUrl.substring("mysql://".length());
                int    at    = rest.indexOf("@");
                String up    = rest.substring(0, at);
                String hp    = rest.substring(at + 1);
                String user  = up.substring(0, up.indexOf(":"));
                String pass  = up.substring(up.indexOf(":") + 1);
                String url   = "jdbc:mysql://" + hp
                             + "?useSSL=false&serverTimezone=UTC"
                             + "&allowPublicKeyRetrieval=true&connectTimeout=10000";
                return DriverManager.getConnection(url, user, pass);
            } catch (Exception e) {
                System.err.println("[DB] MYSQL_URL parse failed, falling back: " + e.getMessage());
            }
        }
        String host = env("MYSQLHOST",     "DB_HOST", "localhost");
        String port = env("MYSQLPORT",     "DB_PORT", "3306");
        String db   = env("MYSQLDATABASE", "MYSQL_DATABASE", "DB_NAME", "dungeon_realm");
        String user = env("MYSQLUSER",     "MYSQL_USER",     "DB_USER", "root");
        String pass = env("MYSQLPASSWORD", "MYSQL_ROOT_PASSWORD", "DB_PASS", "dungeonpass");
        String url  = "jdbc:mysql://" + host + ":" + port + "/" + db
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                    + "&connectTimeout=10000&autoReconnect=true";
        System.out.println("[DB] Connecting -> " + host + ":" + port + "/" + db);
        return DriverManager.getConnection(url, user, pass);
    }

    private static String env(String... keysAndDefault) {
        for (int i = 0; i < keysAndDefault.length - 1; i++) {
            String v = System.getenv(keysAndDefault[i]);
            if (v != null && !v.isBlank()) return v;
        }
        return keysAndDefault[keysAndDefault.length - 1];
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources)
            if (r != null) try { r.close(); } catch (Exception ignored) {}
    }
}
