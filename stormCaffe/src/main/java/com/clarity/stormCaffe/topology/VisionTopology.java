package com.clarity.stormCaffe.topology;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.io.FileNotFoundException;

import static com.clarity.stormCaffe.util.Constants.*;
import static com.clarity.stormCaffe.util.StormConfigManager.getInt;
import static com.clarity.stormCaffe.util.StormConfigManager.readConfig;
import com.clarity.stormCaffe.spout.*;
import com.clarity.stormCaffe.bolt.*;

public class VisionTopology {
    public static void main(String args[]) throws InterruptedException,AlreadyAliveException,InvalidTopologyException,FileNotFoundException {
        Config conf = readConfig(args[0]);
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("frame-getter",new FrameRetrieverSpout(),1).setNumTasks(2);
        builder.setBolt("dummy",new FrameProcessorBolt(),2).shuffleGrouping("frame-getter");

        StormTopology topology = builder.createTopology();

        conf.setNumWorkers(1);
        try{
            StormSubmitter.submitTopologyWithProgressBar("test",conf,topology);
        }
        catch(AuthorizationException e) {
            e.printStackTrace();
        }
    }
}
