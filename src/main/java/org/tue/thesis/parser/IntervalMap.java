package org.tue.thesis.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntervalMap {
    @Data
    @AllArgsConstructor
    public static class Interval {
        int start, end;
    }

    private Map<String, Interval> intervalMap;

    private static final Pattern intervalPattern = Pattern.compile("(\\w+)|(\\d+)(\\d+)");

    public Interval valueOf(String name)  {
        return intervalMap.get(name);
    }

    @SneakyThrows
    public static IntervalMap fromFile(String filePath) {
        var reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        Map<String, Interval> intervalMap = new HashMap<>();
        reader.readLine();
        String line = reader.readLine();
        while (line != null) {
            var matcher = intervalPattern.matcher(line);
            matcher.find();
            String strName = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            int end = Integer.parseInt(matcher.group(3));
            intervalMap.put(strName, new Interval(start, end));
            line = reader.readLine();
        }
        return new IntervalMap(intervalMap);
    }
}
