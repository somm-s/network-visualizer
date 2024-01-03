package ch.cydcampus.hickup.core.filter;

import ch.cydcampus.hickup.core.Packet;

public class PacketSizeFilter implements Filter {

    long minBytes; // inclusive
    long maxBytes; // inclusive
    boolean isBlacklist;

    public PacketSizeFilter(String min, String max, String policy) {
        this.isBlacklist = policy.equals("blacklist");
        if(min == null || min.equals("min")) {
            min = "0";
        }
        if(max == null || max.equals("max")) {
            max = Long.toString(Long.MAX_VALUE);
        }
        this.minBytes = Long.parseLong(min);
        this.maxBytes = Long.parseLong(max);
    }

    @Override
    public String getFilterName() {
        return "packet_size";
    }

    @Override
    public boolean filterMatch(Packet packet) {
        long bytes = packet.getBytes();
        if (isBlacklist) {
            return bytes >= minBytes && bytes <= maxBytes;
        } else {
            return bytes < minBytes || bytes > maxBytes;
        }
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.PACKET_SIZE;
    }
 
    public long getMinBytes() {
        return minBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

}
