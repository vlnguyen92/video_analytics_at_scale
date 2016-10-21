package com.clarity.stormCaffe.bolt;

import com.clarity.stormCaffe.topology.Classifier;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.Map;

public class FrameProcessorBolt extends BaseRichBolt{

    OutputCollector collector;
    Classifier classifier = null;

    @Override 
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        String rootDir = "/home/cc/video_analytics_at_scale/stormCaffe/";
        String modelFile = rootDir + "resources/model/deploy.prototxt";
        String trainFile = rootDir + "resources/model/bvlc_googlenet.caffemodel";
        String meanFile = rootDir + "resources/model/imagenet_mean.binaryproto";
        String labelFile = rootDir + "resources/model/synset_words.txt";
	try{
        classifier = new Classifier(modelFile,trainFile,meanFile,labelFile);
	}catch (java.io.IOException e) {
        e.printStackTrace();
	}

        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        Mat smat = (Mat) tuple.getValueByField("raw-frame");
        classifier.classify(smat,2).get(0).getLeft();
        
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
