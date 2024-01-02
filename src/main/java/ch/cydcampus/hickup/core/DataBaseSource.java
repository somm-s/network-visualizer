package ch.cydcampus.hickup.core;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import ch.cydcampus.hickup.core.Packet.Protocol;
import ch.cydcampus.hickup.util.TimeInterval;

/*
 * TODO: Add functionality to go through data in chunks (e.g. 1 minute at a time)
 * First collect metadata about the query and then divide into chunks of right size.
 */
public class DataBaseSource implements DataSource {
    
    private String query;
    private String querysuffix;
    private String table;
    private String observedHostFilter;
    private String hostFilter;
    private String startTime;
    private String endTime;
    private String url;
    private Packet[] points = null;
    private int index = 0;
    private boolean dataLoaded = false;
    private String portFilter;

    private Connection connection;

    public DataBaseSource(String host, int port, String database, String user, String password, String table) {
        this.table = table;
        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        // connect to the database
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the PostgreSQL server: " + e.getMessage());
        }

        this.query = "SELECT * FROM " + table + " WHERE 1 = 1";
        this.querysuffix = " ORDER BY timestamp";
    }

    @Override
    public Packet consume() throws InterruptedException {
        if(!dataLoaded) {
            points = getPointsFromSQL(startTime, endTime, hostFilter, observedHostFilter, 0);
            System.out.println("Loaded " + points.length + " points from database.");
            dataLoaded = true;
        }

        if(points == null || index >= points.length) {
            return null;
        }
        return points[index++];
    }

    @Override
    public void stopProducer() {
        // do nothing
    }

    @Override
    public void registerReader() {
        // do nothing
    }


    private Packet[] getPointsFromSQL(long startTime, long endTime, String observedHost, String dstHost, int limit) {
        Instant startInstant = TimeInterval.microToInstant(startTime);
        Instant endInstant = TimeInterval.microToInstant(endTime);

        Timestamp startTimestamp = Timestamp.from(startInstant);
        Timestamp endTimestamp = Timestamp.from(endInstant);

        return getPointsFromSQL(startTimestamp.toString(), endTimestamp.toString(), observedHost, dstHost, limit);
    }

    private Packet[] getPointsFromSQL(String startTime, String endTime, String observedHost, String dstHost, int limit) {

        String query = "SELECT * FROM " + table + " WHERE 1 = 1";
        if(!observedHost.equals("")) {
            query += " AND (src_ip = '" + observedHost + "' OR dst_ip = '" + observedHost + "')";
        }
        if(!dstHost.equals("")) {
            query += " AND (src_ip = '" + dstHost + "' OR dst_ip = '" + dstHost + "')";
        }
        if(!portFilter.equals("")) {
            query += " AND (src_port = " + portFilter + " OR dst_port = " + portFilter + ")";
        }
        if(!startTime.equals("") && !endTime.equals("")) {
            query += " AND timestamp BETWEEN '" + startTime + "' AND '" + endTime + "'";
        }
        query += " ORDER BY timestamp";
        
        if (limit > 0) {
            query += " LIMIT " + limit;
        }
        
        query += ";";

        Packet[] res = null;

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            res = getPointsFromPreparedStatement(statement);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }

        return res;
    }

    private Packet ipPointFromResultSet(ResultSet resultSet) throws SQLException, UnknownHostException {
        Protocol protocol = Protocol.fromInt(resultSet.getInt("protocol"));
        int packetSize = resultSet.getInt("size");
        Timestamp time = resultSet.getTimestamp("timestamp");
        String srcIp = resultSet.getString("src_ip");
        String dstIp = resultSet.getString("dst_ip");
        int srcPort = resultSet.getInt("src_port");
        int dstPort = resultSet.getInt("dst_port");
        
        return PacketPool.getPool().allocateFromFields(protocol, packetSize, new TimeInterval(time, time), srcIp, dstIp, srcPort, dstPort);
    }

    private Packet[] getPointsFromPreparedStatement(PreparedStatement preparedStatement) {
        LinkedList<Packet> res = new LinkedList<Packet>();
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                Packet p = ipPointFromResultSet(resultSet);
                res.add(p);
            }
        } catch (SQLException | UnknownHostException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }

        Packet[] res_arr = new Packet[res.size()];
        return res.toArray(res_arr);
    }
}
