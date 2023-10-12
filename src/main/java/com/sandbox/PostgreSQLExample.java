package com.sandbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

            // Create a new table
            createSampleTable(connection);
            insertSampleData(connection);

            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
    }
    
    private static void createSampleTable(Connection connection) throws SQLException {
        // SQL statement to create a new table
        String createTableSQL = "CREATE TABLE IF NOT EXISTS packets ("
                + "id SERIAL PRIMARY KEY,"
                + "timestamp TIMESTAMP,"
                + "protocol INT,"
                + "size INT,"
                + "src_ip VARCHAR(255),"
                + "dst_ip VARCHAR(255),"
                + "src_port INT,"
                + "dst_port INT,"
                + "FIN BOOL,"
                + "SYN BOOL,"
                + "RST BOOL,"
                + "PSH BOOL,"
                + "ACK BOOL,"
                + "URG BOOL"
                + ");";
        System.out.println(createTableSQL);
        try (Statement statement = connection.createStatement()) {
            // Execute the SQL statement to create the table
            statement.execute(createTableSQL);
        }
    }

    private static void insertSampleData(Connection connection) throws SQLException {
        // SQL statement to insert data into the table
        String insertDataSQL = "INSERT INTO packets (protocol, size, src_ip, dst_ip, src_port, dst_port, FIN, SYN, RST, PSH, ACK, URG)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
            // Set values for parameters
            preparedStatement.setInt(1, 1); // Sample value for protocol
            preparedStatement.setInt(2, 1500); // Sample value for size
            preparedStatement.setString(3, "192.168.1.1"); // Sample value for src_ip
            preparedStatement.setString(4, "192.168.1.2"); // Sample value for dst_ip
            preparedStatement.setInt(5, 1234); // Sample value for src_port
            preparedStatement.setInt(6, 5678); // Sample value for dst_port
            preparedStatement.setBoolean(7, true); // Sample value for FIN
            preparedStatement.setBoolean(8, false); // Sample value for SYN
            preparedStatement.setBoolean(9, true); // Sample value for RST
            preparedStatement.setBoolean(10, false); // Sample value for PSH
            preparedStatement.setBoolean(11, true); // Sample value for ACK
            preparedStatement.setBoolean(12, false); // Sample value for URG

            // Execute the SQL statement to insert data
            preparedStatement.executeUpdate();
        }
    }

}
