package org.tue.thesis.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QueryParser {

    private static final Pattern sourcePattern = Pattern.compile("^(\\w+)");
    private static final Pattern edgePattern = Pattern.compile("[\\s,](\\w+)");

    public static Query parserQuery(String line, int id) {
        var srcMatcher = sourcePattern.matcher(line);
        if (!srcMatcher.find()) {
            throw new IllegalArgumentException("Source not found");
        }
        String name = srcMatcher.group(1);

        var edgeMatcher = edgePattern.matcher(line);
        List<String> labels = new ArrayList<>();
        while(edgeMatcher.find()) {
            var label = edgeMatcher.group(1);
            labels.add(label);
        }
        return new Query(id, name, labels);
    }
}
