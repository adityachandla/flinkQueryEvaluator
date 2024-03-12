package org.tue.thesis;

import org.apache.commons.cli.CommandLine;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.tue.thesis.dto.Edge;
import org.tue.thesis.dto.EdgeReaderFormat;
import org.tue.thesis.ops.Deduplicate;
import org.tue.thesis.parser.Direction;
import org.tue.thesis.parser.GeneratedQuery;

import java.util.List;

public class PipelineGenerator {

    public static StreamExecutionEnvironment getExecutionEnvironment(CommandLine cliArgs,
                                                                     List<GeneratedQuery> queries) {
        var inputPath = cliArgs.getOptionValue("input");
        var outputPath = cliArgs.getOptionValue("output");

        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        var fileSource = FileSource.forRecordStreamFormat(new EdgeReaderFormat(), new Path(inputPath));
        DataStream<Edge> edgeStream = env
                .fromSource(fileSource.build(), WatermarkStrategy.noWatermarks(), "adjacencyMatrix");

        for (var q : queries) {
            DataStream<Integer> frontier = env.fromCollection(List.of(q.getStart()));
            for (var lblDir : q.getLabelDirections()) {
                frontier = bfsNext(edgeStream, frontier, lblDir);
            }
            frontier.sinkTo(FileSink.forRowFormat(new Path(outputPath), new SimpleStringEncoder<Integer>())
                    .build());
        }
        return env;
    }


    private static DataStream<Integer> bfsNext(DataStream<Edge> allEdges,
                                               DataStream<Integer> frontier,
                                               GeneratedQuery.LabelDirection lblDir) {
        DataStream<Integer> joinedStream;
        if (lblDir.getDirection() == Direction.OUTGOING) {
            joinedStream = allEdges
                    .join(frontier)
                    .where(Edge::getSrc)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<Edge, Integer, Integer>) (edge, integer) -> edge.getDest());
        } else if (lblDir.getDirection() == Direction.INCOMING) {
            joinedStream = allEdges
                    .join(frontier)
                    .where(Edge::getDest)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<Edge, Integer, Integer>) (edge, integer) -> edge.getSrc());
        } else if (lblDir.getDirection() == Direction.BOTH) {
            var incoming = allEdges
                    .join(frontier)
                    .where(Edge::getDest)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<Edge, Integer, Integer>) (edge, integer) -> edge.getSrc());
            var outgoing = allEdges
                    .join(frontier)
                    .where(Edge::getSrc)
                    .equalTo(i -> i)
                    .window(GlobalWindows.create())
                    .trigger(CountTrigger.of(1))
                    .apply((JoinFunction<Edge, Integer, Integer>) (edge, integer) -> edge.getDest());
            joinedStream = outgoing.union(incoming);
        } else {
            throw new IllegalArgumentException();
        }
        return joinedStream.keyBy(i -> i)
                .flatMap(new Deduplicate());
    }

}