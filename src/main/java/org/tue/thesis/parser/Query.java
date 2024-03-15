package org.tue.thesis.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class Query implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String sourceType;
    private List<String> labels;
}
