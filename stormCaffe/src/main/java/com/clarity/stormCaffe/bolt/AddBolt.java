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

import com.clarity.stormCaffe.util.Serializable;

import java.util.Map;

public class AddBolt extends BaseRichBolt{

    OutputCollector collector;

    @Override 
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = collector;
    }
    @Override
    public void execute(Tuple tuple) {
        Integer height = tuple.getIntegerByField("height");
        Integer width = tuple.getIntegerByField("width");

        Integer sum = height + width;

        collector.emit(new Values(sum));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("total"));
    }
}
