package org.tue.thesis.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.tue.thesis.dto.Edge;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EdgeMap {

    private Map<String, Integer> edgeMap;

    private static final Pattern edgeMapPattern = Pattern.compile("(\\w+)|(\\d+)");

    public int valueOf(String name)  {
        return edgeMap.get(name);
    }

    @SneakyThrows
    public static EdgeMap fromFile(String filePath) {
        var reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        Map<String, Integer> edgeMap = new HashMap<>();
        reader.readLine();
        String line = reader.readLine();
        while (line != null) {
            var matcher = edgeMapPattern.matcher(line);
            matcher.find();
            String strName = matcher.group(1);
            int intName = Integer.parseInt(matcher.group(2));
            edgeMap.put(strName, intName);
            line = reader.readLine();
        }
        return new EdgeMap(edgeMap);
    }
}
