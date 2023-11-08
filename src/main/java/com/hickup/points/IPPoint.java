package com.hickup.points;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import javafx.scene.canvas.GraphicsContext;

public abstract class IPPoint implements Comparable<IPPoint>{

    static String url = "jdbc:postgresql://localhost:5432/ls22";
    static String user = "lab";
    static String password = "lab";
    // static string tableName = "packets";
    static String tableName = "capture";
    public static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(TimeZone.getTimeZone("UTC").toZoneId());
    public static Connection connection;

    public static final int UDP_PROTOCOL = 1;
    public static final int TCP_PROTOCOL = 0;
    public static final int ANY_PROTOCOL = 2;


    public int packetSize;
    public java.sql.Timestamp time;
    public String srcIp;
    public String dstIp;
    public double val;
    double x;
    double y;

    public IPPoint(int packetSize, java.sql.Timestamp time, String srcIp, String dstIp) {
        this.packetSize = packetSize;
        this.time = time;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        if(packetSize > 0) {
            this.val = Math.log(packetSize) - 2;
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double setX(double x) {
        return this.x = x;
    }

    public double setY(double y) {
        return this.y = y;
    }

    public static void connect() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
            + "protocol INT,"
            + "size INT,"
            + "timestamp TIMESTAMP,"    
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
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
    }

    public static IPPoint[] getPointsFromSQL(String startTime, String endTime, String observedHost, String dstHost) {
        // if connection to sql is not established, connect
        if(connection == null) {
            connect();
        }

        String query = "SELECT * FROM " + tableName + " WHERE 1 = 1";
        if(!observedHost.equals("")) {
            query += " AND (src_ip = '" + observedHost + "' OR dst_ip = '" + observedHost + "')";
        }
        if(!dstHost.equals("")) {
            query += " AND (src_ip = '" + dstHost + "' OR dst_ip = '" + dstHost + "')";
        }
        if(!startTime.equals("") && !endTime.equals("")) {
            query += " AND timestamp BETWEEN '" + startTime + "' AND '" + endTime + "'";
        }
        query += ";";
        System.out.println(query);
        IPPoint[] res = null;

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            res = getPointsFromPreparedStatement(statement);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }

        return res;
    }

    public static IPPoint ipPointFromResultSet(ResultSet resultSet) {
        IPPoint res = null;
        try {
            if(resultSet.getInt("protocol") == TCP_PROTOCOL) {
                res = TCPPoint.fromResultSet(resultSet);
            } else if(resultSet.getInt("protocol") == UDP_PROTOCOL) {
                res = UDPPoint.fromResultSet(resultSet);
            } else {
                res = AnyPoint.fromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }
        return res;
    }

    private static IPPoint[] getPointsFromPreparedStatement(PreparedStatement preparedStatement) {
        LinkedList<IPPoint> res = new LinkedList<IPPoint>();
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                IPPoint p = ipPointFromResultSet(resultSet);
                res.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }

        IPPoint[] res_arr = new IPPoint[res.size()];
        return res.toArray(res_arr);
    }


    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillOval(x - width / 2, y - height / 2, width, height);
    }

    public static IPPoint parsePacket(Packet packet, java.sql.Timestamp time) {


        IPPoint res = null;

        // check for specific network layer protocol
        if(!packet.contains(IpPacket.class)) {
            return null;
        }

        String src_addr = packet.get(IpPacket.class).getHeader().getSrcAddr().getHostAddress();
        String dest_addr  = packet.get(IpPacket.class).getHeader().getDstAddr().getHostAddress();

        // check for specific transport layer protocol
        if(packet.contains(TcpPacket.class)) {
            boolean[] flags = new boolean[6];
            int length = 0;
            TcpPacket tcpPacket = packet.get(TcpPacket.class);
            if(tcpPacket.getPayload() != null) {
                length = tcpPacket.getPayload().length();
            }
            int src_port = tcpPacket.getHeader().getSrcPort().valueAsInt();
            int dst_port = tcpPacket.getHeader().getDstPort().valueAsInt();
            TCPPoint tcpPoint = new TCPPoint(length, time, src_addr, dest_addr, src_port, dst_port);

            // set flags
            flags[0] = tcpPacket.getHeader().getFin();
            flags[1] = tcpPacket.getHeader().getSyn();
            flags[2] = tcpPacket.getHeader().getRst();
            flags[3] = tcpPacket.getHeader().getPsh();
            flags[4] = tcpPacket.getHeader().getAck();
            flags[5] = tcpPacket.getHeader().getUrg();
            tcpPoint.setFlags(flags);

            res = tcpPoint;

        } else if(packet.contains(UdpPacket.class)) {

            int length = 0;
            UdpPacket udpPacket = packet.get(UdpPacket.class);
            if(udpPacket.getPayload() != null) {
                length = udpPacket.getPayload().length();
            }
            int src_port = udpPacket.getHeader().getSrcPort().valueAsInt();
            int dst_port = udpPacket.getHeader().getDstPort().valueAsInt();
            UDPPoint udpPoint = new UDPPoint(length, time, src_addr, dest_addr, src_port, dst_port);
            res = udpPoint;
        } else {
            int length = packet.length();
            AnyPoint otherPoint = new AnyPoint(length, time, src_addr, dest_addr);
            res = otherPoint;
        }

        return res;
    }

    // serializes to string with toString method of dynamic type
    public abstract String toString();

    // deserializes from string
    public static IPPoint fromString(String s) {
        IPPoint res = null;

        // use deserialization of dynamic type. String in csv style
        String[] parts = s.split(",");
        if(parts[0].equals("0")) {
            res = TCPPoint.fromString(s);
        } else if(parts[0].equals("1")) {
            res = UDPPoint.fromString(s);
        } else if(parts[0].equals("2")) {
            res = AnyPoint.fromString(s);
        } else {
            throw new IllegalArgumentException("Invalid string format");
        }
        return res;
    }

    public static java.sql.Timestamp timeFromString(String timeString) {
        // Parse the string to Instant
        Instant instant = Instant.from(timeFormatter.parse(timeString));

        // Convert Instant to Timestamp
        java.sql.Timestamp timestamp = java.sql.Timestamp.from(instant);

        return timestamp;
    }

    public static String timeToString(java.sql.Timestamp timestamp) {
        // Convert Timestamp to Instant
        Instant instant = timestamp.toInstant();

        // Format the timestamp with nanoseconds
        String formattedTimestamp = timeFormatter.format(instant);

        return formattedTimestamp;
    }


    // static function to read ip points from a file. returns array of ippoints.
    public static IPPoint[] readFromFile(String path) {
        List<IPPoint> res = new LinkedList<IPPoint>();
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(path));

            String line = reader.readLine();
            
            while(line != null && !line.equals("")) {
                res.add(IPPoint.fromString(line));
                line = reader.readLine();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        IPPoint[] res_arr = new IPPoint[res.size()];
        return res.toArray(res_arr);
    }

    public static IPPoint[] readFromFiles(String folderPath, String filterIP) throws IOException {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        // sort list to have the files in the correct order
        java.util.Arrays.sort(listOfFiles);

        // List that is returned
        List<IPPoint> res = new LinkedList<IPPoint>();

        // go over all files
        for(int i = 0; i < listOfFiles.length; i++) {
            System.out.println("Reading file " + listOfFiles[i].getName());
            int numPackets = 0;
            BufferedReader reader = new BufferedReader(new FileReader(listOfFiles[i]));

            String line = reader.readLine();
            
            while(line != null && !line.equals("")) {
                // apply filter:
                IPPoint p = IPPoint.fromString(line);
                if(p.dstIp.equals(filterIP) || p.srcIp.equals(filterIP)) {
                    res.add(p);
                    numPackets++;
                }
                line = reader.readLine();
            }
            System.out.println("Read " + numPackets + " Packets");
            reader.close();
        }

        IPPoint[] res_arr = new IPPoint[res.size()];
        return res.toArray(res_arr);
    }

    public abstract void insertPointToSql(Connection connection) throws SQLException;
    public abstract void insertPointToSqlBatch(PreparedStatement preparedStatement) throws SQLException;

    public static void loadIPPointsIntoSql(String folderPath) throws IOException, SQLException {

        // assumes valid connection to sql.

        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        // sort list to have the files in the correct order
        java.util.Arrays.sort(listOfFiles);

        // List that is returned
        IPPoint res = null;

        String insertDataSQL = "INSERT INTO packets (protocol, size, timestamp, src_ip, dst_ip, src_port, dst_port, FIN, SYN, RST, PSH, ACK, URG)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        // go over all files
        for(int i = 0; i < listOfFiles.length; i++) {
            System.out.println("Reading file " + listOfFiles[i].getName());
            int numPackets = 0;
            BufferedReader reader = new BufferedReader(new FileReader(listOfFiles[i]));

            String line = reader.readLine();

            PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL);
            
            while(line != null && !line.equals("")) {
                // apply filter:
                res = IPPoint.fromString(line);
                res.insertPointToSqlBatch(preparedStatement);
                line = reader.readLine();
                numPackets++;

                if(numPackets % 1000 == 0) {
                    preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                }
            }               
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();

            System.out.println("Ingested " + numPackets + " Packets");
            reader.close();
        }
    }

    public static long getMicroseconds(java.sql.Timestamp time) {
        Instant instant = time.toInstant();
        long micros = instant.toEpochMilli() * 1000 + instant.getNano() / 1000;

        return micros;
    }

    public long getMicroseconds() {
        return IPPoint.getMicroseconds(this.time);
    }

    public static Instant microToInstant(long micros) {
        long millis = micros / 1000;
        int nanos = (int) ((micros % 1000) * 1000);
        return Instant.ofEpochMilli(millis).plusNanos(nanos);
    }
    
    @Override
    public int compareTo(IPPoint other) {
        return Long.compare(this.getMicroseconds(), other.getMicroseconds());
    }
}