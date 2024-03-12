package org.tue.thesis.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class GeneratedQuery {
    private int id;
    private int start;
    private List<LabelDirection> labelDirections;

    @AllArgsConstructor
    @Getter
    public static class LabelDirection implements Serializable {
        private static final long serialVersionUID = 1L;

        private int label;
        private Direction direction;

    }
}
