package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.util.OpBuilder;
import org.apache.storm.topology.TopologyBuilder;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-10-17.
 */
public class ScnnTopology {
    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        OpBuilder opBuilder = new OpBuilder(args);

        String file = "/home/lvnguyen/video_data/testvid.mp4";
        builder.setSpout("spout", opBuilder.buildSpout(), 1);

    }
}
