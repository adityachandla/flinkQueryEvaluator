package org.tue.thesis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class LabelDirection implements Serializable {
    private static final long serialVersionUID = 3284234L;

    int label;
    boolean outgoing;
}
