package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.spout.*;
import com.clarity.stormCaffe.bolt.*;
import com.clarity.stormCaffe.util.Serializable;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import com.clarity.stormCaffe.util.Serializable.CVMat;

public class VisionTopology 
{
    public static void main( String[] args ) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        String file = "/home/cc/video_data/testvid.mp4";

        builder.setSpout("spout", new FrameGetterSpout(file, 50), 1);

        builder.setBolt("classifier", new FrameProcessorBolt(), 1).shuffleGrouping("spout");

//        builder.setBolt("add", new AddBolt(), 1).shuffleGrouping("get-dimension");

        Config conf = new Config();
        conf.setDebug(true);
        conf.registerSerialization(Serializable.CVMat.class);
        // True if Storm should timeout messages or not.
        conf.put(Config.TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS, false);

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
