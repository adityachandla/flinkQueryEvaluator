package org.tue.thesis.ops;

import org.apache.flink.graph.pregel.MessageCombiner;
import org.apache.flink.graph.pregel.MessageIterator;

public final class ReachabilityCombiner extends MessageCombiner<Integer, Integer> {

    @Override
    public void combineMessages(MessageIterator<Integer> messageIterator) throws Exception {
        var min = Integer.MAX_VALUE;
        for (var message : messageIterator) {
            min = Integer.min(message, min);
        }
        sendCombinedMessage(min);
    }
}

