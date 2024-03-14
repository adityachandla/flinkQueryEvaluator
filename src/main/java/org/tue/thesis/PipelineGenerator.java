package org.tue.thesis;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.tue.thesis.dto.EdgeWithLabel;
import org.tue.thesis.ops.Deduplicate;
import org.tue.thesis.parser.Direction;
import org.tue.thesis.parser.GeneratedQuery;

import java.util.List;

public class PipelineGenerator {

    public static DataStream<Integer> createExecutionGraph(StreamExecutionEnvironment env,
                                                           DataStream<EdgeWithLabel> inputStream,
                                                           GeneratedQuery query) {

        DataStream<Integer> frontier = env.fromCollection(List.of(query.getStart()));
        for (var lblDir : query.getLabelDirections()) {
            frontier = bfsNext(inputStream, frontier, lblDir);
        }
        return frontier;
    }

    private static DataStream<Integer> bfsNext(DataStream<EdgeWithLabel> allEdges,
                                               DataStream<Integer> frontier,
                                               GeneratedQuery.LabelDirection lblDir) {
        DataStream<Integer> joinedStream;
        if (lblDir.getDirection() == Direction.OUTGOING) {
            joinedStream = allEdges
                    .join(frontier)
                    .where(EdgeWithLabel::getSrc)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<EdgeWithLabel, Integer, Integer>) (edge, integer) -> edge.getDest());
        } else if (lblDir.getDirection() == Direction.INCOMING) {
            joinedStream = allEdges
                    .join(frontier)
                    .where(EdgeWithLabel::getDest)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<EdgeWithLabel, Integer, Integer>) (edge, integer) -> edge.getSrc());
        } else if (lblDir.getDirection() == Direction.BOTH) {
            var incoming = allEdges
                    .join(frontier)
                    .where(EdgeWithLabel::getDest)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<EdgeWithLabel, Integer, Integer>) (edge, integer) -> edge.getSrc());
            var outgoing = allEdges
                    .join(frontier)
                    .where(EdgeWithLabel::getSrc)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<EdgeWithLabel, Integer, Integer>) (edge, integer) -> edge.getDest());
            joinedStream = outgoing.union(incoming);
        } else {
            throw new IllegalArgumentException();
        }
        return joinedStream.keyBy(i -> i)
                .flatMap(new Deduplicate());
    }

}
