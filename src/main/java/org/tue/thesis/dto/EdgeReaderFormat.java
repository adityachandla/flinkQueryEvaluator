package org.tue.thesis.dto;


import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.reader.SimpleStreamFormat;
import org.apache.flink.connector.file.src.reader.StreamFormat;
import org.apache.flink.core.fs.FSDataInputStream;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EdgeReaderFormat extends SimpleStreamFormat<Edge> {
    private static final long serialVersionUID = 2834737L;

    @Override
    public Reader<Edge> createReader(Configuration configuration, FSDataInputStream fsDataInputStream) throws IOException {
        return new EdgeReader(fsDataInputStream);
    }

    @Override
    public TypeInformation<Edge> getProducedType() {
        return TypeInformation.of(Edge.class);
    }

    public static final class EdgeReader implements StreamFormat.Reader<Edge> {

        private final BufferedReader br;

        public EdgeReader(FSDataInputStream inputStream) {
            this.br = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Nullable
        @Override
        public Edge read() throws IOException {
            String line = br.readLine();
            if (line == null) {
                return null;
            }
            String[] parts = line.split(",");
            boolean isOutgoing = parts[3].startsWith("true");
            if (!isOutgoing) {
                return null;
            }
            int src = Integer.parseInt(parts[0].substring(1));
            int label = Integer.parseInt(parts[1]);
            int dest = Integer.parseInt(parts[2]);
            return new Edge(src, label, dest);
        }

        @Override
        public void close() throws IOException {
            br.close();
        }
    }
}
