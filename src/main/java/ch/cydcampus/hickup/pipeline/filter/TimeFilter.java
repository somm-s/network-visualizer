package ch.cydcampus.hickup.pipeline.filter;

import ch.cydcampus.hickup.core.Packet;
import ch.cydcampus.hickup.util.TimeInterval;

public class TimeFilter implements Filter {


    public static final String SMALLEST_TIME = "1970-01-01 00:00:00";
    public static final String BIGGEST_TIME = "9999-12-31 23:59:59";

    private String min;
    private String max;
    TimeInterval timeInterval;
    boolean isBlacklist;

    public TimeFilter(String min, String max, String policy) {
    
        this.isBlacklist = policy.equals("blacklist");
        if(min == null || min.equals("min")) {
            min = SMALLEST_TIME;
        }
        if(max == null || max.equals("max")) {
            max = BIGGEST_TIME;
        }
        this.min = min;
        this.max = max;
        this.timeInterval = new TimeInterval(min, max);
    }

    @Override
    public String getFilterName() {
        return "time";
    }

    @Override
    public boolean filterMatch(Packet packet) {
        if (isBlacklist) {
            return timeInterval.contains(packet.getTimeInterval());
        } else {
            return !timeInterval.contains(packet.getTimeInterval());
        }
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.TIME;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }
    
}
