package org.tue.thesis;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.types.NullValue;
import org.apache.flink.util.Collector;
import org.tue.thesis.dto.CommandLineParameters;
import org.tue.thesis.dto.KinesisParameters;
import org.tue.thesis.dto.Parameters;
import org.tue.thesis.ops.EdgeMapper;
import org.tue.thesis.ops.ReachabilityCombiner;
import org.tue.thesis.ops.VertexCompute;
import org.tue.thesis.parser.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GellyPipelineGenerator {

    private static final String localInputPath =
            "file:///home/aditya/Documents/projects/flinkGraphProcessor/localEdges.txt";
    //    private static final String localInputPath =
//            "file:///home/aditya/Documents/projects/ldbc_converter/sf1/adjacency/s_0_e_2225.csv";
    private static final String localOutputPath =
            "file:///home/aditya/Documents/projects/flinkGraphProcessor/oup";

    public static void main(String[] args) throws Exception {
        var env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        Parameters runtimeParams = getParameters(args);
        List<GeneratedQuery> generatedQueries = generateQueries(runtimeParams);

        Graph<Integer, NullValue, Integer> g = readGraph(env, runtimeParams);
        int idx = 0;
        for (var q : generatedQueries) {
            DataSet<Integer> result = runBfs(g, q);
            writeResult(runtimeParams, result, idx);
            idx++;
        }
        env.execute("BFS Job");
    }

    private static void writeResult(Parameters params, DataSet<Integer> result, int idx) {
        if (params.isLocal()) {
            result.writeAsText(localOutputPath + idx);
        } else {
            result.writeAsText(params.getOutputPath() + idx);
        }
    }

    private static List<GeneratedQuery> generateQueries(Parameters params) throws Exception{
        var queries = getQueries(params);
        var edgeMap = EdgeMap.fromFile(getEdgeMap(params));
        var intervalMap = IntervalMap.fromFile(getIntervalMap(params));
        var generator = new QueryGenerator(edgeMap, intervalMap);
        Query queryToGenerate = params.hasQueryNumber() ?
                queries.get(params.getQueryNumber()) : queries.get(0);
        int numRepetitions = params.getNumRepetitions();
        List<GeneratedQuery> generatedQueries = new ArrayList<>(numRepetitions);
        for (int i = 0; i < numRepetitions; i++) {
            generatedQueries.add(generator.generate(queryToGenerate));
        }
        return generatedQueries;
    }

    private static Parameters getParameters(String[] args) throws Exception {
        Map<String, Properties> props = KinesisAnalyticsRuntime.getApplicationProperties();
        if (props.isEmpty()) {
            Options opts = getOptions();

            var parser = new DefaultParser();
            return new CommandLineParameters(parser.parse(opts, args));
        }
        return new KinesisParameters(props.get("RuntimeProperties"));
    }

    private static DataSet<Integer> runBfs(Graph<Integer, NullValue, Integer> g, GeneratedQuery query) {
        int startNode = query.getStart();
        var labels = query.getLabels();
        var frontierGraph = g.mapVertices(new MapFunction<Vertex<Integer, NullValue>, Integer>() {
                    @Override
                    public Integer map(Vertex<Integer, NullValue> vertex) throws Exception {
                        return vertex.getId() == startNode ? 1 : Integer.MAX_VALUE;
                    }
                })
                .runVertexCentricIteration(new VertexCompute(labels), new ReachabilityCombiner(), labels.size() + 1);

        return frontierGraph
                .filterOnVertices(v -> v.getValue() == labels.size() + 1).getVertexIds();
    }

    private static Graph<Integer, NullValue, Integer> readGraph(ExecutionEnvironment env, Parameters params) {
        DataSource<String> input;
        if (params.isLocal()) {
            input = env.readTextFile(localInputPath);
        } else {
            input = env.readTextFile(params.getInputPath());
        }
        DataSet<Edge<Integer, Integer>> edges = input
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

        return Graph.fromDataSet(vertices, edges, env);
    }

    private static Options getOptions() {
        Options opts = new Options();
        var inputPathOpt = new Option("i", "input", true, "Input path");
        opts.addOption(inputPathOpt);
        var local = new Option("l", "local", false, "Is execution local");
        opts.addOption(local);
        var sfOption = new Option("s", "sf", true, "Scaling factor: 1 or 10");
        opts.addOption(sfOption);
        var outputPathOpt = new Option("o", "output", true, "Output path");
        opts.addOption(outputPathOpt);
        var queryIdxOpt = new Option("q", "query", true, "Index of query to run");
        opts.addOption(queryIdxOpt);
        var repetitions = new Option("r", "repetitions", true,
                "Number of repetitions of the query");
        opts.addOption(repetitions);
        return opts;
    }

    private static InputStream getEdgeMap(Parameters params) {
        if (params.isLocal()) {
            return getResourceInputStream("localEdgeMap.csv");
        }
        return getResourceInputStream("edgeMap.csv");
    }

    private static InputStream getIntervalMap(Parameters params) {
        if (params.isLocal()) {
            return getResourceInputStream("localNodeMap.csv");
        } else if (params.getScalingFactor() == Parameters.ScalingFactor.ONE) {
            return getResourceInputStream("nodeMap1.csv");
        } else if (params.getScalingFactor() == Parameters.ScalingFactor.TEN) {
            return getResourceInputStream("nodeMap10.csv");
        }
        throw new IllegalArgumentException("No suitable nodemap found");
    }

    private static InputStream getQueryFile(Parameters params) {
        if (params.isLocal()) {
            return getResourceInputStream("localQueries.txt");
        }
        return getResourceInputStream("queries.txt");
    }

    private static List<Query> getQueries(Parameters params) throws Exception {
        var reader = new BufferedReader(new InputStreamReader(getQueryFile(params)));
        List<Query> queries = new ArrayList<>();
        String line = reader.readLine();
        int id = 1;
        while (line != null) {
            if (line.isBlank()) {
                line = reader.readLine();
                continue;
            }
            var q = QueryParser.parserQuery(line, id);
            id++;
            queries.add(q);
            line = reader.readLine();
        }
        return queries;
    }

    private static InputStream getResourceInputStream(String fileName) {
        var is = GellyPipelineGenerator.class.getClassLoader()
                .getResourceAsStream(fileName);
        Objects.requireNonNull(is);
        return is;
    }

}
