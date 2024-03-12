package org.tue.thesis.parser;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Random;

@AllArgsConstructor
public class QueryGenerator {
    private final EdgeMap edgeMap;
    private final IntervalMap intervalMap;
    private final Random random = new Random(17041998L);

    public GeneratedQuery generate(Query query) {
        var nodeInterval = intervalMap.valueOf(query.getSourceType());
        int node = nodeInterval.getStart() + random.nextInt(nodeInterval.getEnd()-nodeInterval.getStart());
        var labelDirections = new ArrayList<GeneratedQuery.LabelDirection>();
        for (var lblDir : query.getLabelDirections() ) {
            int labelInt = edgeMap.valueOf(lblDir.getLabel());
            labelDirections.add(new GeneratedQuery.LabelDirection(labelInt, lblDir.getDirection()));
        }
        return new GeneratedQuery(query.getId(), node, labelDirections);
    }
}
