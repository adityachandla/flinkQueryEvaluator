package org.tue.thesis.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QueryParser {

    private static final Pattern sourcePattern = Pattern.compile("^(\\w+)");
    private static final Pattern edgePattern = Pattern.compile("\\(([<>]{1,2}),\\s?(\\w+)\\)");

    public static Query parserQuery(String line, int id) {
        var srcMatcher = sourcePattern.matcher(line);
        if (!srcMatcher.find()) {
            throw new IllegalArgumentException("Source not found");
        }
        String name = srcMatcher.group(1);

        var edgeMatcher = edgePattern.matcher(line);
        List<LabelDirection> labelDirections = new ArrayList<>();
        while(edgeMatcher.find()) {
            var direction = LabelDirection.Direction.parseDirection(edgeMatcher.group(1));
            var label = edgeMatcher.group(2);
            labelDirections.add(new LabelDirection(label, direction));
        }
        return new Query(id, name, labelDirections);
    }
}
