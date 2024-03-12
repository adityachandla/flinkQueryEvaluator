package org.tue.thesis;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.tue.thesis.parser.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class QueryProcessor {

    public static void main(String[] args) throws Exception {
        Options opts = new Options();
        var inputPathOpt = new Option("i", "input", true, "Input path");
        opts.addOption(inputPathOpt);
        var outputPathOpt = new Option("o", "output", true, "Output path");
        opts.addOption(outputPathOpt);
        var parser = new DefaultParser();
        var commandLine = parser.parse(opts, args);

        var queries = getQueries();
        var edgeMap = EdgeMap.fromFile(getResourceInputStream("edgeMap.csv"));
        System.out.println("Got edge map");
        var intervalMap = IntervalMap.fromFile(getResourceInputStream("nodeMap1.csv"));
        System.out.println("Got interval map");
        var generator = new QueryGenerator(edgeMap, intervalMap);
        System.out.println("Got query generator");
        var generatedQueries = generator.generateAll(queries);
        System.out.println("Generated queries: " + generatedQueries);

        var env = PipelineGenerator.getExecutionEnvironment(commandLine, generatedQueries);
        env.execute("File reader");
    }

    public static List<Query> getQueries() throws Exception {
        var reader = new BufferedReader(new InputStreamReader(getResourceInputStream("queries.txt")));
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
