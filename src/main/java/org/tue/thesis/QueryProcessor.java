package org.tue.thesis;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.io.CsvInputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.tue.thesis.dto.Edge;
import org.tue.thesis.dto.EdgeReaderFormat;
import org.tue.thesis.dto.LabelDirection;

import java.time.Duration;
import java.util.List;
import java.util.Objects;


public class QueryProcessor {

    private static final String inputPath = "file:///home/aditya/Documents/projects/flinkGraphProcessor/input.txt";
    private static final String outputPath = "file:///home/aditya/Documents/projects/flinkGraphProcessor/";


    public static void main(String[] args) throws Exception {
        try (var env = StreamExecutionEnvironment.getExecutionEnvironment()) {
            env.setParallelism(1);

            var fileSource = FileSource.forRecordStreamFormat(new EdgeReaderFormat(), new Path(inputPath));

            DataStream<Edge> edgeStream = env
                    .fromSource(fileSource.build(), WatermarkStrategy.noWatermarks(), "adjacencyMatrix");

            DataStream<Integer> frontier = env.fromCollection(List.of(1));

            var query = List.of(new LabelDirection(2, true),
                    new LabelDirection(1, true),
                    new LabelDirection(1, true));

            for (var lblDir : query) {
                frontier = bfsNext(edgeStream, frontier, lblDir);
            }
            frontier.sinkTo(FileSink.forRowFormat(new Path(outputPath), new SimpleStringEncoder<Integer>()).build());

            env.execute("File reader");
        }
    }

    private static DataStream<Integer> bfsNext(DataStream<Edge> allEdges, DataStream<Integer> frontier, LabelDirection lblDir) {
        frontier.print();
        return allEdges
                .join(frontier)
                .where(e -> {
                    System.out.println("Edge is " + e);
                    return e.getSrc();
                })
                .equalTo(i -> {
                    System.out.println("Selecting " + i);
                    return i;
                })
                .window(GlobalWindows.create())
                .trigger(CountTrigger.of(1))
                .apply((JoinFunction<Edge, Integer, Integer>) (edge, integer) -> {
                    System.out.println("Generated next " + edge.getDest());
                    return edge.getDest();
                });
    }
}
