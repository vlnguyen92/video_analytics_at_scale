package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.spout.*;
import com.clarity.stormCaffe.bolt.*;
import com.clarity.stormCaffe.util.Serializable;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class VisionTopology 
{
    public static void main( String[] args ) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String rootDir = "/home/lvnguyen/stormSchedulers/video_analytics_at_scale/stormCaffe/";
        String modelFile = rootDir + "resources/model/deploy.prototxt";
        String trainFile = rootDir + "resources/model/bvlc_googlenet.caffemodel";
        String meanFile = rootDir + "resources/model/imagenet_mean.binaryproto";
        String labelFile = rootDir + "resources/model/synset_words.txt";
        Classifier classifier= new Classifier(modelFile,trainFile,meanFile,labelFile);

        String file = "/home/lvnguyen/video_data/testvid.mp4";
        builder.setSpout("spout", new FrameGetterSpout(file), 1);

        builder.setBolt("get-dimension", new FrameProcessorBolt(classifier), 1).shuffleGrouping("spout");

        builder.setBolt("add", new AddBolt(), 1).shuffleGrouping("get-dimension");

        Config conf = new Config();
        conf.setDebug(true);
        conf.registerSerialization(Serializable.CVMat.class);

        if(args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar("vision",conf,builder.createTopology());
        }
        else {
            conf.setMaxTaskParallelism(3);

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("vision",conf,builder.createTopology());

            Thread.sleep(100000);
            cluster.shutdown();
        }

    }
}
