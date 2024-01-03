package ch.cydcampus.hickup.core.filter;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.cydcampus.hickup.core.Packet;

public class IPFilter implements Filter {

    Set<InetAddress> ips;
    boolean isBlacklist;

    public IPFilter(List<String> ips, String policy) {
        this.ips = new HashSet<>();
        for (String ip : ips) {
            try {
                this.ips.add(InetAddress.getByName(ip));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.isBlacklist = policy.equals("blacklist");
    }

    @Override
    public String getFilterName() {
        return "ip";
    }

    @Override
    public boolean filterMatch(Packet packet) {
        if (isBlacklist) {
            return ips.contains(packet.getSrcIP()) || ips.contains(packet.getDstIP());
        } else {
            return !ips.contains(packet.getSrcIP()) && !ips.contains(packet.getDstIP());
        }
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.IP;
    }

    public Set<InetAddress> getIps() {
        return ips;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }
    
}
