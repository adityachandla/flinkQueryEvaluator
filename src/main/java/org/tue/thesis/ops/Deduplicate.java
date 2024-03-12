package org.tue.thesis.ops;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class Deduplicate extends RichFlatMapFunction<Integer, Integer> {

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