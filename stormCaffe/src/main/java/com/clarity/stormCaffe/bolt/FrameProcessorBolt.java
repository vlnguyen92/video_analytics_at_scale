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
import org.bytedeco.javacpp.opencv_core.Mat;

import com.clarity.stormCaffe.util.Serializable;
import com.clarity.stormCaffe.topology.Classifier;

import java.util.Map;

public class FrameProcessorBolt extends BaseRichBolt{

    OutputCollector collector;
    Classifier classifier;

    public FrameProcessorBolt(Classifier classifier) {
        this.classifier = classifier;
    }

    @Override 
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        Serializable.CVMat smat = (Serializable.CVMat) tuple.getValueByField("raw-frame");
        Mat img = smat.toJavaCVMat();
        classifier.classify(img,2).get(0).getLeft();
        
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
