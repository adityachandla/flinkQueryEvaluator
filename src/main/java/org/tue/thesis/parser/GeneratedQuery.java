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
    private List<Integer> labels;
}
