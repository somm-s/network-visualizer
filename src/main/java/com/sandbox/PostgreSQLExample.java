package com.sandbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLExample {

    public static void main(String[] args) {
        // JDBC URL, username, and password of PostgreSQL server
        String url = "jdbc:postgresql://localhost:5432/ls22";
        String user = "lab";
        String password = "lab";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to the PostgreSQL server successfully.");
            // Grant necessary privileges to the user
            grantPrivileges(connection, "public", user);
            // Create a new table
            createSampleTable(connection);

            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
    }
    
    private static void grantPrivileges(Connection connection, String schema, String user) throws SQLException {
        // Grant privileges to the user on the specified schema
        String grantPrivilegesSQL = "GRANT ALL PRIVILEGES ON SCHEMA " + schema + " TO " + user + ";";

        try (Statement statement = connection.createStatement()) {
            // Execute the SQL statement to grant privileges
            statement.execute(grantPrivilegesSQL);
        }
    }

    private static void createSampleTable(Connection connection) throws SQLException {
        // SQL statement to create a new table
        String createTableSQL = "CREATE TABLE IF NOT EXISTS sample_table ("
                + "id SERIAL PRIMARY KEY,"
                + "name VARCHAR(255),"
                + "age INT"
                + ");";

        try (Statement statement = connection.createStatement()) {
            // Execute the SQL statement to create the table
            statement.execute(createTableSQL);
        }
    }
}
