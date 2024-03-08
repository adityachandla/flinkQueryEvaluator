package org.tue.thesis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@ToString
@Getter
public class Edge implements Serializable {
    private static final long serialVersionUID = 2348234L;

    private int src;
    private int label;
    private int dest;
}
