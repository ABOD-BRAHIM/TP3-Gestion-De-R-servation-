package com.example.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MigrationExecutor {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public MigrationExecutor(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void executeMigration(String scriptPath) throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             InputStream is = getClass().getClassLoader().getResourceAsStream(scriptPath)) {

            if (is == null) {
                throw new IllegalArgumentException("Script de migration introuvable dans le classpath : " + scriptPath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {

                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    // Ignorer les commentaires et lignes vides
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sql.append(line).append("\n");
                    // Exécuter chaque instruction délimitée par point-virgule
                    if (trimmed.endsWith(";")) {
                        String command = sql.toString().trim();
                        if (command.endsWith(";")) {
                            command = command.substring(0, command.length() - 1);
                        }
                        if (!command.isEmpty()) {
                            stmt.execute(command);
                        }
                        sql.setLength(0);
                    }
                }

                if (sql.length() > 0 && !sql.toString().trim().isEmpty()) {
                    stmt.execute(sql.toString().trim());
                }
            }
        }
    }
}
