package ch.cydcampus.hickup.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;

import ch.cydcampus.hickup.model.TokenState.Protocol;
import ch.cydcampus.hickup.util.TimeInterval;

public class DataBaseSource implements DataSource {

    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
    private String table;
    private String hostFilter;
    private String startTime;
    private String endTime;
    private String url;

    private Token[] points = null;
    private int index = 0;

    private boolean dataLoaded = false;

    private Connection connection;

    public DataBaseSource(String host, int port, String database, String user, String password, String table, String hostFilter, String startTime, String endTime) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.table = table;
        this.hostFilter = hostFilter;
        this.startTime = startTime;
        this.endTime = endTime;
        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        // connect to the database
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
    }

    @Override
    public Token consume() throws InterruptedException {
        if(!dataLoaded) {
            points = getPointsFromSQL(startTime, endTime, hostFilter, "", 0);
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


    private Token[] getPointsFromSQL(long startTime, long endTime, String observedHost, String dstHost, int limit) {
        Instant startInstant = TimeInterval.microToInstant(startTime);
        Instant endInstant = TimeInterval.microToInstant(endTime);

        Timestamp startTimestamp = Timestamp.from(startInstant);
        Timestamp endTimestamp = Timestamp.from(endInstant);

        return getPointsFromSQL(startTimestamp.toString(), endTimestamp.toString(), observedHost, dstHost, limit);
    }

    private Token[] getPointsFromSQL(String startTime, String endTime, String observedHost, String dstHost, int limit) {

        String query = "SELECT * FROM " + table + " WHERE 1 = 1";
        if(!observedHost.equals("")) {
            query += " AND (src_ip = '" + observedHost + "' OR dst_ip = '" + observedHost + "')";
        }
        if(!dstHost.equals("")) {
            query += " AND (src_ip = '" + dstHost + "' OR dst_ip = '" + dstHost + "')";
        }
        if(!startTime.equals("") && !endTime.equals("")) {
            query += " AND timestamp BETWEEN '" + startTime + "' AND '" + endTime + "'";
        }
        
        if (limit > 0) {
            query += " LIMIT " + limit;
        }
        
        query += ";";

        Token[] res = null;

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            res = getPointsFromPreparedStatement(statement);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }

        return res;
    }

    private Token ipPointFromResultSet(ResultSet resultSet) throws SQLException {
        Protocol protocol = Protocol.fromInt(resultSet.getInt("protocol"));
        int packetSize = resultSet.getInt("size");
        Timestamp time = resultSet.getTimestamp("timestamp");
        String srcIp = resultSet.getString("src_ip");
        String dstIp = resultSet.getString("dst_ip");
        int srcPort = resultSet.getInt("src_port");
        int dstPort = resultSet.getInt("dst_port");
        
        return TokenPool.getPool().allocateFromFields(protocol, packetSize, new TimeInterval(time, time), srcIp, dstIp, srcPort, dstPort);
    }

    private Token[] getPointsFromPreparedStatement(PreparedStatement preparedStatement) {
        LinkedList<Token> res = new LinkedList<Token>();
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                Token p = ipPointFromResultSet(resultSet);
                res.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }

        Token[] res_arr = new Token[res.size()];
        return res.toArray(res_arr);
    }
}
