package org.tue.thesis.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EdgeMap {

    private static final Pattern edgeMapPattern = Pattern.compile("(\\w+)\\s*\\|\\s*(\\d+)");
    private Map<String, Integer> edgeMap;

    @SneakyThrows
    public static EdgeMap fromFile(InputStream inputStream) {
        var reader = new BufferedReader(new InputStreamReader(inputStream));
        Map<String, Integer> edgeMap = new HashMap<>();
        reader.readLine();
        String line = reader.readLine();
        while (line != null) {
            var matcher = edgeMapPattern.matcher(line);
            if (!matcher.find()) {
                System.out.println(line);
                throw new IllegalArgumentException();
            }
            String strName = matcher.group(1);
            int intName = Integer.parseInt(matcher.group(2));
            edgeMap.put(strName.toLowerCase(), intName);
            line = reader.readLine();
        }
        return new EdgeMap(edgeMap);
    }

    public int valueOf(String name) {
        return edgeMap.get(name.toLowerCase());
    }
}
