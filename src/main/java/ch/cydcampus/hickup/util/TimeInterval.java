package ch.cydcampus.hickup.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.TimeZone;

/*
 * Represents a microsecond precision time interval and provides methods for conversion.
 */
public class TimeInterval {

    public static final DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true) // Append microseconds with 0-6 digits
        .toFormatter()
        .withZone(TimeZone.getTimeZone("UTC").toZoneId());

    private long start;
    private long end;

    public static long timeToMicro(Timestamp timestamp) {
        return timestamp.getTime() * 1000 + (timestamp.getNanos() / 1000) % 1000;
    }

    public TimeInterval() {
        this.start = 0;
        this.end = 0;
    }

    public TimeInterval(TimeInterval timeInterval) {
        this.start = timeInterval.getStart();
        this.end = timeInterval.getEnd();
    }

    public TimeInterval(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public TimeInterval(Timestamp start, Timestamp end) {
        this.start = timeToMicro(start);
        this.end = timeToMicro(end);
    }

    public TimeInterval(String start, String end) {
        this.start = timeToMicro(timeFromString(start));
        this.end = timeToMicro(timeFromString(end));
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;        
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;        
    }

    public void updateTimeInterval(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public void updateTimeInterval(Timestamp start, Timestamp end) {
        this.start = timeToMicro(start);
        this.end = timeToMicro(end);
    }

    public boolean doIntersect(TimeInterval other) {
        return !(start > other.getEnd() || end < other.getStart());
    }

    public TimeInterval union(TimeInterval timeInterval) {
        long newStart = Math.min(start, timeInterval.getStart());
        long newEnd = Math.max(end, timeInterval.getEnd());

        return new TimeInterval(newStart, newEnd);
    }

    /*
     * Returns the time in the gap between this time interval and the other 
     * time interval in microseconds. Return 0 if the time intervals overlap.
     */
    public long getDifference(TimeInterval other) {
        long difference = 0;

        if (other.getStart() > end) {
            difference = other.getStart() - end;
        } else if (other.getEnd() < start) {
            difference = start - other.getEnd();
        }

        return difference;
    }

    public void setContentTo(TimeInterval timeInterval) {
        this.start = timeInterval.getStart();
        this.end = timeInterval.getEnd();
    }

    private Instant microToInstant(long micros) {
        long millis = micros / 1000;
        int nanos = (int) ((micros % 1000) * 1000);
        return Instant.ofEpochMilli(millis).plusNanos(nanos);
    }

    public String toString() {
        // transform microseconds to timestamp
        Instant startInstant = microToInstant(start);
        Instant endInstant = microToInstant(end);
        Timestamp startTimestamp = Timestamp.from(startInstant);
        Timestamp endTimestamp = Timestamp.from(endInstant);
        return "[" + startTimestamp.toString() + ", " + endTimestamp.toString() + "]";
    }


    private Timestamp timeFromString(String timeString) {
        // Parse the string to Instant
        Instant instant = Instant.from(timeFormatter.parse(timeString));


        // Convert Instant to Timestamp
        java.sql.Timestamp timestamp = java.sql.Timestamp.from(instant);

        return timestamp;
    }

    private String timeToString(Timestamp timestamp) {
        // Convert Timestamp to Instant
        Instant instant = timestamp.toInstant();

        // Format Instant to String
        String timeString = timeFormatter.format(instant);

        return timeString;
    }

}