package org.tue.thesis.parser;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
public class GeneratedQuery {
    private int id;
    private int start;
    private List<LabelDirection> labelDirections;

    @AllArgsConstructor
    public static class LabelDirection implements Serializable {
        private static final long serialVersionUID = 1L;

        private int label;
        private Direction direction;

    }
}
