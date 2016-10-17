package com.clarity.stormCaffe.bolt;

import com.clarity.stormCaffe.model.Frame;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.bytedeco.javacpp.opencv_core;

import java.util.Map;

public class FrameProcessorBolt extends BaseRichBolt{

    OutputCollector collector;

    @Override 
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }
    @Override
    public void execute(Tuple tuple) {
        opencv_core.Mat smat = ((Frame) tuple.getValueByField("raw-frame")).getImage();
        if(smat == null) {
            System.out.println("NULLPOINTER");
        }

        int W = smat.cols();
        int H = smat.rows();

        collector.emit(tuple, new Values(H,W));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
//        declarer.declare(new Fields());
        declarer.declare(new Fields("height","width"));
    }
}
