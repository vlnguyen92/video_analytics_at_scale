package com.clarity.stormCaffe.bolt;

import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.utils.Utils;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;

import java.util.Map;

public class FrameProcessorBolt extends BaseRichBolt{

    OutputCollector collector;

    @Override 
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = collector;
    }
    @Override
    public void execute(Tuple tuple) {
        String id = tuple.getSourceStreamId();
        String word = "abcded";
        collector.emit(new Values(word));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("dummy"));
    }
}
