package com.hickup.points;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class TCPPoint extends IPPoint {

    boolean[] flags = new boolean[6]; // FIN, SYN, RST, PSH, ACK, URG
    public int srcPort;
    public int dstPort;

    public TCPPoint(int packetSize, Timestamp time, String srcIp, String dstIp) {
        super(packetSize, time, srcIp, dstIp);
    }

    public TCPPoint(int packetSize, Timestamp time, String srcIp, String dstIp, int srcPort, int dstPort) {
        super(packetSize, time, srcIp, dstIp);
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }


    @Override
    public void draw(GraphicsContext gc, double x, double y, double width, double height) {

        super.draw(gc, x, y, width, height);
        width = width + 1;
        height = height + 1;
        if(flags[1]) {
            // draw cross with (x, y) in the center if SYN flag is set
            gc.setStroke(javafx.scene.paint.Color.BLACK);
            gc.strokeLine(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
            gc.strokeLine(x - width / 2, y + height / 2, x + width / 2, y - height / 2);
        } else if(flags[2]) {
            // draw cross with (x, y) in the center if RST flag is set
            gc.setStroke(javafx.scene.paint.Color.WHITE);
            gc.strokeLine(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
            gc.strokeLine(x - width / 2, y + height / 2, x + width / 2, y - height / 2);
        } else if(flags[0]) {
            // draw horizontal cross with (x, y) in the center if FIN flag is set
            gc.setStroke(javafx.scene.paint.Color.BLACK);
            gc.strokeLine(x - width / 2, y, x + width / 2, y);
            gc.strokeLine(x, y - height / 2, x, y + height / 2);
        }

    }

    public void setFlags(boolean[] flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        // serialize to string
        String s = "";
        s += "0,"; // type
        s += packetSize + ",";
        s += IPPoint.timeToString(time) + ",";
        s += srcIp + ",";
        s += dstIp + ",";
        s += srcPort + ",";
        s += dstPort + ",";
        for(int i = 0; i < flags.length; i++) {
            if(flags[i]) {
                s += "1,";
            } else {
                s += "0,";
            }
        }

        return s;
    }

    // deserialize from string
    public static TCPPoint fromString(String string) {
        String[] parts = string.split(",");
        TCPPoint tcpPoint = new TCPPoint(Integer.parseInt(parts[1]), IPPoint.timeFromString(parts[2]), parts[3], parts[4], Integer.parseInt(parts[5]), Integer.parseInt(parts[6]));

        boolean[] flags = new boolean[6];
        for(int i = 0; i < flags.length; i++) {
            if(parts[7 + i].equals("1")) {
                flags[i] = true;
            } else {
                flags[i] = false;
            }
        }
        tcpPoint.setFlags(flags);
        return tcpPoint;
    }

    @Override
    public void insertPointToSql(Connection connection) throws SQLException {
        String insertDataSQL = "INSERT INTO " + tableName + " (protocol, size, timestamp, src_ip, dst_ip, src_port, dst_port, FIN, SYN, RST, PSH, ACK, URG)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
            // Set values for parameters
            preparedStatement.setInt(1, IPPoint.TCP_PROTOCOL);
            preparedStatement.setInt(2, this.packetSize);
            preparedStatement.setTimestamp(3, this.time);
            preparedStatement.setString(4, this.srcIp);
            preparedStatement.setString(5, this.dstIp);         
            preparedStatement.setInt(6, this.srcPort);
            preparedStatement.setInt(7, this.dstPort);
            preparedStatement.setBoolean(8, this.flags[0]);
            preparedStatement.setBoolean(9, this.flags[1]);
            preparedStatement.setBoolean(10, this.flags[2]);
            preparedStatement.setBoolean(11, this.flags[3]);
            preparedStatement.setBoolean(12, this.flags[4]);
            preparedStatement.setBoolean(13, this.flags[5]);

            // Execute the SQL statement to insert data
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void insertPointToSqlBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, IPPoint.TCP_PROTOCOL);
        preparedStatement.setInt(2, this.packetSize);
        preparedStatement.setTimestamp(3, this.time);
        preparedStatement.setString(4, this.srcIp);
        preparedStatement.setString(5, this.dstIp);         
        preparedStatement.setInt(6, this.srcPort);
        preparedStatement.setInt(7, this.dstPort);
        preparedStatement.setBoolean(8, this.flags[0]);
        preparedStatement.setBoolean(9, this.flags[1]);
        preparedStatement.setBoolean(10, this.flags[2]);
        preparedStatement.setBoolean(11, this.flags[3]);
        preparedStatement.setBoolean(12, this.flags[4]);
        preparedStatement.setBoolean(13, this.flags[5]);
        preparedStatement.addBatch();
    }

    public static TCPPoint fromResultSet(ResultSet resultSet) throws SQLException {

        int packetSize = resultSet.getInt("size");
        Timestamp time = resultSet.getTimestamp("timestamp");
        String srcIp = resultSet.getString("src_ip");
        String dstIp = resultSet.getString("dst_ip");
        int srcPort = resultSet.getInt("src_port");
        int dstPort = resultSet.getInt("dst_port");
        boolean[] flags = new boolean[6];
        flags[0] = resultSet.getBoolean("FIN");
        flags[1] = resultSet.getBoolean("SYN");
        flags[2] = resultSet.getBoolean("RST");
        flags[3] = resultSet.getBoolean("PSH");
        flags[4] = resultSet.getBoolean("ACK");
        flags[5] = resultSet.getBoolean("URG");

        TCPPoint tcpPoint = new TCPPoint(packetSize, time, srcIp, dstIp, srcPort, dstPort);
        tcpPoint.setFlags(flags);
        return tcpPoint;
    }

    @Override
    public String getConnectionIdentifier() {
        // concatenate both ips, ports and protocol, start with the smaller one
        String src = this.srcIp + "-" + this.srcPort;
        String dst = this.dstIp + "-" + this.dstPort;
        if (src.compareTo(dst) > 0) {
            String tmp = src;
            src = dst;
            dst = tmp;
        }

        return src + "-" + dst + "-" + IPPoint.TCP_PROTOCOL;
    }

    @Override
    public int getSrcPort() {
        return this.srcPort;
    }

    @Override
    public int getDstPort() {
        return this.dstPort;
    }

    @Override
    public int getProtocol() {
        return IPPoint.TCP_PROTOCOL;
    }

}