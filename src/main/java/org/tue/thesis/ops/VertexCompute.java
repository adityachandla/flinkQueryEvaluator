package org.tue.thesis.ops;


import lombok.AllArgsConstructor;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.pregel.ComputeFunction;
import org.apache.flink.graph.pregel.MessageIterator;
import org.tue.thesis.parser.GeneratedQuery;

import java.util.List;

@AllArgsConstructor
public final class VertexCompute extends ComputeFunction<Integer, Integer, Integer, Integer> {

    private List<GeneratedQuery.LabelDirection> lblDirections;


    @Override
    public void compute(Vertex<Integer, Integer> vertex, MessageIterator<Integer> messageIterator) throws Exception {
        int stepNumber = getSuperstepNumber();
        if (vertex.getValue() == stepNumber) {
//            System.out.println("Processing vertex "  + vertex.getId() + " in iteration " + stepNumber);
            var label = lblDirections.get(stepNumber-1).getLabel();
            for (var edge : getEdges()) {
                if (edge.getValue() == label) {
                    sendMessageTo(edge.getTarget(), stepNumber + 1);
                }
            }
        } else {
            int minMessage = vertex.getValue();
            for (Integer message : messageIterator) {
                minMessage = Integer.min(minMessage, message);
            }
            if (minMessage != vertex.getValue()) {
//                System.out.println("Passing messages from vertex " + vertex.getId() + " in iteration " + stepNumber);
                setNewVertexValue(minMessage);
                //Last iteration.
                if (minMessage-1 == lblDirections.size()) return;

                int label = lblDirections.get(minMessage-1).getLabel();
                for (var edge : getEdges()) {
                    if (edge.getValue() == label) {
                        sendMessageTo(edge.getTarget(), minMessage + 1);
                    }
                }
            }
        }
    }
}
