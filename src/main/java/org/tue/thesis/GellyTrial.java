package org.tue.thesis;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.pregel.ComputeFunction;
import org.apache.flink.graph.pregel.MessageCombiner;
import org.apache.flink.graph.pregel.MessageIterator;
import org.apache.flink.types.NullValue;
import org.apache.flink.util.Collector;

public class GellyTrial {

    //    private static final String localInputPath =
//            "file:///home/aditya/Documents/projects/flinkGraphProcessor/localEdges.txt";
    private static final String localInputPath =
            "file:///home/aditya/Documents/projects/ldbc_converter/sf1/adjacency/s_0_e_2225.csv";
    private static final String localOutputPath =
            "file:///home/aditya/Documents/projects/flinkGraphProcessor/oup";

    public static void main(String[] args) throws Exception {
        var env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataSet<Edge<Integer, Integer>> edges = env.readTextFile(localInputPath)
                .filter(line -> line.endsWith("ue)"))
                .map(new EdgeMapper())
                .returns(new TypeHint<Edge<Integer, Integer>>() {
                });
        DataSet<Vertex<Integer, NullValue>> vertices = edges.flatMap(new FlatMapFunction<Edge<Integer, Integer>, Integer>() {
                    @Override
                    public void flatMap(Edge<Integer, Integer> edge, Collector<Integer> collector) throws Exception {
                        collector.collect(edge.f0);
                        collector.collect(edge.f1);
                    }
                })
                .distinct()
                .map(e -> new Vertex<>(e, NullValue.getInstance()))
                .returns(new TypeHint<Vertex<Integer, NullValue>>() {
                });

        Graph<Integer, NullValue, Integer> g = Graph.fromDataSet(vertices, edges, env);
        int start = 1;
        var frontierGraph = g.mapVertices(new MapFunction<Vertex<Integer, NullValue>, Boolean>() {
                    @Override
                    public Boolean map(Vertex<Integer, NullValue> vertex) throws Exception {
                        return vertex.getId() == start;
                    }
                })
                .runVertexCentricIteration(new VertexComputeFunction(), new ReachabilityCombiner(), 4);

        DataSet<Integer> result = frontierGraph.filterOnVertices(Vertex::getValue).getVertexIds();
        result.writeAsText(localOutputPath);
        env.execute("BFS Job");
    }

    public static final class EdgeMapper implements MapFunction<String, Edge<Integer, Integer>> {
        @Override
        public Edge<Integer, Integer> map(String line) throws Exception {
            String[] splits = line.split(",");
            int src = Integer.parseInt(splits[0].substring(1));
            int label = Integer.parseInt(splits[1]);
            int dest = Integer.parseInt(splits[2]);
            return new Edge<>(src, dest, label);
        }
    }

    public static final class ReachabilityCombiner extends MessageCombiner<Integer, Boolean> {

        @Override
        public void combineMessages(MessageIterator<Boolean> messageIterator) throws Exception {
            for (var message : messageIterator) {
                if (message) {
                    sendCombinedMessage(Boolean.TRUE);
                    return;
                }
            }
            sendCombinedMessage(Boolean.FALSE);
        }
    }

    public static final class VertexComputeFunction extends ComputeFunction<Integer, Boolean, Integer, Boolean> {

        @Override
        public void compute(Vertex<Integer, Boolean> vertex, MessageIterator<Boolean> messageIterator) throws Exception {
            if (vertex.getValue()) {
                sendMessageToAllNeighbors(Boolean.TRUE);
            } else {
                for (Boolean message : messageIterator) {
                    if (message) {
                        setNewVertexValue(Boolean.TRUE);
                        sendMessageToAllNeighbors(Boolean.TRUE);
                        break;
                    }
                }
            }
        }
    }
}
