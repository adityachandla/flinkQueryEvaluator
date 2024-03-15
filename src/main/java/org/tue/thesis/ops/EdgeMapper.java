package org.tue.thesis.ops;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.graph.Edge;

public final class EdgeMapper implements MapFunction<String, Edge<Integer, Integer>> {
    @Override
    public Edge<Integer, Integer> map(String line) throws Exception {
        String[] splits = line.split(",");
        int src = Integer.parseInt(splits[0]);
        int label = Integer.parseInt(splits[1]);
        int dest = Integer.parseInt(splits[2]);
        return new Edge<>(src, dest, label);
    }
}
