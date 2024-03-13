package org.tue.thesis;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.tue.thesis.dto.CommandLineParameters;
import org.tue.thesis.dto.KinesisParameters;
import org.tue.thesis.dto.Parameters;
import org.tue.thesis.parser.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


public class QueryProcessor {

    public static void main(String[] args) throws Exception {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(5);
        Parameters runtimeParams;
        if (env instanceof LocalStreamEnvironment) {
            Options opts = getOptions();

            var parser = new DefaultParser();
            runtimeParams = new CommandLineParameters(parser.parse(opts, args));
        } else {
            Map<String, Properties> props =  KinesisAnalyticsRuntime.getApplicationProperties();
            runtimeParams = new KinesisParameters(props.get("RuntimeProperties"));
        }

        var queries = getQueries(runtimeParams);
        var edgeMap = EdgeMap.fromFile(getEdgeMap(runtimeParams));
        var intervalMap = IntervalMap.fromFile(getIntervalMap(runtimeParams));
        var generator = new QueryGenerator(edgeMap, intervalMap);
        List<GeneratedQuery> generatedQueries;
        if (runtimeParams.hasQueryNumber()) {
            Query queryToRun = queries.get(runtimeParams.getQueryNumber());
            generatedQueries = List.of(generator.generate(queryToRun));
        } else {
            generatedQueries = generator.generateAll(queries);
        }
        PipelineGenerator.createExecutionGraph(env, runtimeParams, generatedQueries);
        env.execute("BFS Evaluator");
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
        var is = PipelineGenerator.class.getClassLoader()
                .getResourceAsStream(fileName);
        Objects.requireNonNull(is);
        return is;
    }

}
