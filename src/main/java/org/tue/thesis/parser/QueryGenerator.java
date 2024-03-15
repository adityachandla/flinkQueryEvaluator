package org.tue.thesis.parser;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@AllArgsConstructor
public class QueryGenerator {
    private final EdgeMap edgeMap;
    private final IntervalMap intervalMap;
    private final Random random = new Random(17041919L);

    public GeneratedQuery generate(Query query) {
        var nodeInterval = intervalMap.valueOf(query.getSourceType());
        int node = nodeInterval.getStart() + random.nextInt(nodeInterval.getEnd() - nodeInterval.getStart()+1);
        var labels = new ArrayList<Integer>();
        for (var label : query.getLabels()) {
            int labelInt = edgeMap.valueOf(label);
            labels.add(labelInt);
        }
        return new GeneratedQuery(query.getId(), node, labels);
    }

    public List<GeneratedQuery> generateAll(List<Query> queries) {
        return queries.stream()
                .map(this::generate)
                .collect(Collectors.toList());
    }
}
