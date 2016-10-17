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

public class FrameProcessorBolt extends BaseRichBolt{

    OutputCollector collector;

    @Override 
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }
    @Override
    public void execute(Tuple tuple) {
        Serializable.CVMat smat = (Serializable.CVMat) tuple.getValueByField("raw-frame");
        if(smat == null) {
            System.out.println("NULLPOINTER");
        }

        int W = smat.getCols();
        int H = smat.getRows();

        collector.emit(tuple, new Values(H,W));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
//        declarer.declare(new Fields());
        declarer.declare(new Fields("height","width"));
    }
}
