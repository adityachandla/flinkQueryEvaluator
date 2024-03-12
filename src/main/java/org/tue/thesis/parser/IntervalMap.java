package org.tue.thesis.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntervalMap {
    private static final Pattern intervalPattern = Pattern.compile("(\\w+)\\|(\\d+)\\|(\\d+)");
    private Map<String, Interval> intervalMap;

    @SneakyThrows
    public static IntervalMap fromFile(InputStream is) {
        var reader = new BufferedReader(new InputStreamReader(is));
        Map<String, Interval> intervalMap = new HashMap<>();
        reader.readLine();
        String line = reader.readLine();
        while (line != null) {
            var matcher = intervalPattern.matcher(line);
            if (!matcher.find()) {
                throw new IllegalArgumentException();
            }
            String strName = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            int end = Integer.parseInt(matcher.group(3));
            intervalMap.put(strName.toLowerCase(), new Interval(start, end));
            line = reader.readLine();
        }
        return new IntervalMap(intervalMap);
    }

    public Interval valueOf(String name) {
        return intervalMap.get(name.toLowerCase());
    }

    @Data
    @AllArgsConstructor
    public static class Interval {
        int start, end;
    }
}
