package org.tue.thesis.dto;

public interface Parameters {
    boolean isLocal();
    String getInputPath();
    String getOutputPath();
    boolean hasQueryNumber();
    int getQueryNumber();
    ScalingFactor getScalingFactor();

    int getNumRepetitions();

    enum ScalingFactor {
        ONE, TEN
    }
}
