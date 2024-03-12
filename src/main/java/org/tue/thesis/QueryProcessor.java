package org.tue.thesis;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.util.Collector;
import org.tue.thesis.dto.Edge;
import org.tue.thesis.dto.EdgeReaderFormat;
import org.tue.thesis.parser.LabelDirection;

import java.util.List;


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

//            var query = List.of(new LabelDirection(2, true),
//                    new LabelDirection(1, true),
//                    new LabelDirection(1, true));
//
//            for (var lblDir : query) {
//                frontier = bfsNext(edgeStream, frontier, lblDir);
//            }
            frontier.sinkTo(FileSink.forRowFormat(new Path(outputPath), new SimpleStringEncoder<Integer>()).build());

            env.execute("File reader");
        }
    }

    private static DataStream<Integer> bfsNext(DataStream<Edge> allEdges, DataStream<Integer> frontier, LabelDirection lblDir) {
        return allEdges
                .join(frontier)
                .where(Edge::getSrc)
                .equalTo(i -> i)
                .window(GlobalWindows.create())
                .trigger(CountTrigger.of(1))
                .apply((JoinFunction<Edge, Integer, Integer>) (edge, integer) -> edge.getDest())
                .keyBy(i -> i)
                .flatMap(new Deduplicate());
    }

    private static class Deduplicate extends RichFlatMapFunction<Integer, Integer> {

        private ValueState<Boolean> seen;

        @Override
        public void open(Configuration config) {
            ValueStateDescriptor<Boolean> desc = new ValueStateDescriptor<>("seen", Types.BOOLEAN);
            seen = getRuntimeContext().getState(desc);
        }

        @Override
        public void flatMap(Integer input, Collector<Integer> collector) throws Exception {
            if (seen.value() == null) {
                collector.collect(input);
                seen.update(true);
            }
        }
    }
}
