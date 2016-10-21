package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.bolt.AddBolt;
import com.clarity.stormCaffe.bolt.FrameProcessorBolt;
import com.clarity.stormCaffe.spout.FrameGetterSpout;
import com.clarity.stormCaffe.util.Serializable;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.bytedeco.javacpp.opencv_core;

public class VisionTopology 
{
    public static void main( String[] args ) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String file = "/home/cc/video_data/testvid.mp4";
        builder.setSpout("spout", new FrameGetterSpout(file), 1);

        builder.setBolt("get-dimension", new FrameProcessorBolt(), 1).shuffleGrouping("spout");

        builder.setBolt("add", new AddBolt(), 1).shuffleGrouping("get-dimension");

        Config conf = new Config();
        conf.setDebug(true);
        conf.registerSerialization(opencv_core.Mat.class, Serializable.CVMatSerializer.class);

        if(args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0],conf,builder.createTopology());
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
