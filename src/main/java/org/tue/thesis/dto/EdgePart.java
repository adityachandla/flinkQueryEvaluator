package org.tue.thesis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class EdgePart implements Serializable {
    private static final long serialVersionUID = 1L;
    int dest, label;
}
