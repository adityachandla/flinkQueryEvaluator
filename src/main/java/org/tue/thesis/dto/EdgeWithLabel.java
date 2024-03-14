package org.tue.thesis.dto;

import lombok.*;

import java.io.Serializable;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EdgeWithLabel implements Serializable {
    private static final long serialVersionUID = 2348234L;

    private int src;
    private int label;
    private int dest;
}
