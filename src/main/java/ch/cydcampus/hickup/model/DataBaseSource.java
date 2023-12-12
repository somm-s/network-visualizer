package ch.cydcampus.hickup.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    private static final String HOST_PAIR_FILE_PATH = "/home/lab/Documents/thesis/host_to_host_pairs.csv";
    private static final int MIN_PACKETS = 1;
    private static final int MAX_PACKETS = 1000;
    private static final int NUM_REPETITIONS = 2000;
    private String table;
    private String observedHostFilter;
    private String hostFilter;
    private String startTime;
    private String endTime;
    private String url;
    private boolean sampling = false;
    private int sampleCount = 0;
    private Token[] points = null;
    private int index = 0;

    private boolean dataLoaded = false;

    private Connection connection;

    public DataBaseSource(String host, int port, String database, String user, String password, String table, String hostFilter, String observedHostFilter, String startTime, String endTime) {
        this.table = table;
        this.hostFilter = hostFilter;
        this.observedHostFilter = observedHostFilter;
        this.startTime = startTime;
        this.endTime = endTime;
        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        if(hostFilter.equals("") && observedHostFilter.equals("")) {
            sampling = true;
            getRandomHostPair();
        }

        // connect to the database
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
    }

    private void getRandomHostPair() {
        try {
            int numPackets = sampleHostPair();
            while(numPackets < MIN_PACKETS || numPackets > MAX_PACKETS) {
                System.out.println("Sampled host pair with less than " + MIN_PACKETS + " packets, sampling again.");
                numPackets = sampleHostPair();
            }
        } catch (IOException e) {
            System.err.println("Error sampling host pair: " + e.getMessage());
        }
    }

    private int sampleHostPair() throws IOException {
        // open file with host pairs (csv)
        File hostPairFile = new File(HOST_PAIR_FILE_PATH);
        if(!hostPairFile.exists()) {
            System.err.println("Host pair file not found at " + HOST_PAIR_FILE_PATH);
            System.exit(1);
        }

        // sample from file
        BufferedReader br = new BufferedReader(new FileReader(hostPairFile));
        // read header line
        String line = br.readLine();
        // read everything
        LinkedList<String> hostPairs = new LinkedList<String>();
        while((line = br.readLine()) != null) {
            hostPairs.add(line);
        }
        br.close();

        // select random host pair
        String[] hostPair = hostPairs.get((int) (Math.random() * hostPairs.size())).split(",");

        this.hostFilter = hostPair[0];
        this.observedHostFilter = hostPair[1];
        this.startTime = "";
        this.endTime = "";
        System.out.println("Loading Host Pair: " + hostPair[0] + " -> " + hostPair[1]);
        return Integer.parseInt(hostPair[2]);
    }

    @Override
    public Token consume() throws InterruptedException {
        if(!dataLoaded) {
            points = getPointsFromSQL(startTime, endTime, hostFilter, observedHostFilter, 0);
            System.out.println("Loaded " + points.length + " points from database.");
            dataLoaded = true;
        }

        if(points == null || index >= points.length) {
            if(sampling && sampleCount < NUM_REPETITIONS) {
                sampleCount++;
                getRandomHostPair();
                index = 0;
                dataLoaded = false;
                points = null;
                return consume();
            }

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
        query += " ORDER BY timestamp";
        
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
