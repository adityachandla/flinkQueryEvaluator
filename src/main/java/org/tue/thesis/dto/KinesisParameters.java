package org.tue.thesis.dto;

import java.util.Properties;

public class KinesisParameters implements Parameters{

    private Properties props;

    public KinesisParameters(Properties props) {
        this.props = props;
    }
    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public String getInputPath() {
        return props.getProperty("input");
    }

    @Override
    public String getOutputPath() {
        return props.getProperty("output");
    }

    @Override
    public boolean hasQueryNumber() {
        return props.containsKey("query");
    }

    @Override
    public int getQueryNumber() {
        return Integer.parseInt(props.getProperty("query"));
    }

    @Override
    public ScalingFactor getScalingFactor() {
        var sf = props.getProperty("sf");
        if ("1".equals(sf)) {
            return ScalingFactor.ONE;
        } else if ("10".equals(sf)) {
            return ScalingFactor.TEN;
        }
        throw new IllegalArgumentException("Invalid scaling factor");
    }
}
