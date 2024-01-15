package ch.cydcampus.hickup.pipeline;

import ch.cydcampus.hickup.pipeline.feature.Feature.FeatureType;

public class PipelineConfig {
    
    public static final int NUM_ABSTRACTION_LEVELS = 4;
    public static final int MAX_ABSTRACTION_LEVEL = NUM_ABSTRACTION_LEVELS - 1;

    public static final int[] TIMEOUTS = { 0, 30000, 1000000, 1000000 };

    public static final String[] LEVEL_0_FEATURES = new String[] {
        "srcIP", "dstIP", "srcPort", "dstPort", "protocol", "bytes", "timestamp", "flowID", "hostPairID"
    };
    public static final FeatureType[] LEVEL_0_FEATURE_TYPES = new FeatureType[] {
        FeatureType.IP, FeatureType.IP, FeatureType.INT, FeatureType.INT, FeatureType.PROTOCOL, FeatureType.LONG, FeatureType.LONG, FeatureType.STRING, FeatureType.STRING
    };

    public static final String[] LEVEL_1_FEATURES = new String[] {
        "srcIP", "flowID", "hostPairID", "sum(bytes)", "interval(timestamp)"
    };

    public static final FeatureType[] LEVEL_1_FEATURE_TYPES = new FeatureType[] {
        FeatureType.IP, FeatureType.STRING, FeatureType.STRING, FeatureType.LONG, FeatureType.LONG_INTERVAL
    };

    public static final String[] LEVEL_2_FEATURES = new String[] {
        "hostPairID", "sum(bytes)", "interval(timestamp)"
    };

    public static final FeatureType[] LEVEL_2_FEATURE_TYPES = new FeatureType[] {
        FeatureType.STRING, FeatureType.LONG, FeatureType.LONG_INTERVAL
    };

    public static final String[] LEVEL_3_FEATURES = new String[] {
        "hostPairID", "sum(bytes)", "interval(timestamp)"
    };

    public static final FeatureType[] LEVEL_3_FEATURE_TYPES = new FeatureType[] {
        FeatureType.STRING, FeatureType.LONG, FeatureType.LONG_INTERVAL
    };

}
