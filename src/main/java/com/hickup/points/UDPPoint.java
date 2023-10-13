package com.hickup.points;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class UDPPoint extends IPPoint {

    int srcPort;
    int dstPort;

    public UDPPoint(int packetSize, Timestamp time, String srcIp, String dstIp) {
        super(packetSize, time, srcIp, dstIp);
    }

    public UDPPoint(int packetSize, Timestamp time, String srcIp, String dstIp, int srcPort, int dstPort) {
        super(packetSize, time, srcIp, dstIp);
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    @Override
    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillRect(x - (width - 1) / 2, y - (height - 1) / 2, width - 1, height - 1);
    }

    @Override
    public String toString() {
        // serialize to string
        String s = "";
        s += "1,"; // type
        s += packetSize + ",";
        s += IPPoint.timeToString(time) + ",";
        s += srcIp + ",";
        s += dstIp + ",";
        s += srcPort + ",";
        s += dstPort;
        return s;
    }
    
    public static UDPPoint fromString(String s) {
        // deserialize from string
        String[] parts = s.split(",");
        int packetSize = Integer.parseInt(parts[1]);
        Timestamp time = IPPoint.timeFromString(parts[2]);
        String srcIp = parts[3];
        String dstIp = parts[4];
        int srcPort = Integer.parseInt(parts[5]);
        int dstPort = Integer.parseInt(parts[6]);
        return new UDPPoint(packetSize, time, srcIp, dstIp, srcPort, dstPort);
    }

    @Override
    public void insertPointToSql(Connection connection) throws SQLException {
        String insertDataSQL = "INSERT INTO packets (timestamp, protocol, size, src_ip, dst_ip, src_port, dst_port)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
            // Set values for parameters
            preparedStatement.setTimestamp(1, this.time);
            preparedStatement.setInt(2, this.UDP_PROTOCOL);
            preparedStatement.setInt(3, this.packetSize);
            preparedStatement.setString(4, this.srcIp);
            preparedStatement.setString(5, this.dstIp);         
            preparedStatement.setInt(6, this.srcPort);
            preparedStatement.setInt(7, this.dstPort);

            // Execute the SQL statement to insert data
            preparedStatement.executeUpdate();
        }
    }


    @Override
    public void insertPointToSqlBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setTimestamp(1, this.time);
        preparedStatement.setInt(2, this.UDP_PROTOCOL);
        preparedStatement.setInt(3, this.packetSize);
        preparedStatement.setString(4, this.srcIp);
        preparedStatement.setString(5, this.dstIp);         
        preparedStatement.setInt(6, this.srcPort);
        preparedStatement.setInt(7, this.dstPort);
        preparedStatement.setNull(8, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(9, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(10, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(11, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(12, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(13, java.sql.Types.BOOLEAN);
        preparedStatement.addBatch();

    }

    public static UDPPoint fromResultSet(ResultSet resultSet) throws SQLException {

        int packetSize = resultSet.getInt("size");
        Timestamp time = resultSet.getTimestamp("timestamp");
        String srcIp = resultSet.getString("src_ip");
        String dstIp = resultSet.getString("dst_ip");
        int srcPort = resultSet.getInt("src_port");
        int dstPort = resultSet.getInt("dst_port");
        return new UDPPoint(packetSize, time, srcIp, dstIp, srcPort, dstPort);

    }
}
