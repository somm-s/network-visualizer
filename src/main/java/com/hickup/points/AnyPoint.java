package com.hickup.points;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class AnyPoint extends IPPoint {
    public AnyPoint(int packetSize, Timestamp time, String srcIp, String dstIp) {
        super(packetSize, time, srcIp, dstIp);
    }

    @Override
    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        // draw triangle with (x, y) in the center
        gc.fillPolygon(new double[] {x - width / 2, x + width / 2, x}, 
        new double[] {y - height / 2, y - height / 2, y + height / 2}, 3);
    }

    @Override
    public String toString() {
        // serialize to string
        String s = "";
        s += "2,"; // type
        s += packetSize + ",";
        s += IPPoint.timeToString(time) + ",";
        s += srcIp + ",";
        s += dstIp;
        return s;
    }

    public static AnyPoint fromString(String s) {
        // deserialize from string
        String[] parts = s.split(",");
        int packetSize = Integer.parseInt(parts[1]);
        Timestamp time = IPPoint.timeFromString(parts[2]);
        String srcIp = parts[3];
        String dstIp = parts[4];
        return new AnyPoint(packetSize, time, srcIp, dstIp);
    }

    @Override
    public void insertPointToSql(Connection connection) throws SQLException {
        String insertDataSQL = "INSERT INTO " + tableName + " (timestamp, protocol, size, src_ip, dst_ip)"
        + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
            // Set values for parameters
            preparedStatement.setInt(1, this.ANY_PROTOCOL);
            preparedStatement.setInt(2, this.packetSize);
            preparedStatement.setTimestamp(3, this.time);
            preparedStatement.setString(4, this.srcIp);
            preparedStatement.setString(5, this.dstIp);
            // Execute the SQL statement to insert data
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void insertPointToSqlBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, this.ANY_PROTOCOL);
        preparedStatement.setInt(2, this.packetSize);
        preparedStatement.setTimestamp(3, this.time);
        preparedStatement.setString(4, this.srcIp);
        preparedStatement.setString(5, this.dstIp);         
        preparedStatement.setNull(6, java.sql.Types.INTEGER);
        preparedStatement.setNull(7, java.sql.Types.INTEGER);
        preparedStatement.setNull(8, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(9, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(10, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(11, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(12, java.sql.Types.BOOLEAN);
        preparedStatement.setNull(13, java.sql.Types.BOOLEAN);
        preparedStatement.addBatch();
    }

    public static AnyPoint fromResultSet(ResultSet resultSet) throws SQLException {
        int packetSize = resultSet.getInt("size");
        Timestamp time = resultSet.getTimestamp("timestamp");
        String srcIp = resultSet.getString("src_ip");
        String dstIp = resultSet.getString("dst_ip");
        return new AnyPoint(packetSize, time, srcIp, dstIp);
    }
}