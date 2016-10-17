package com.clarity.stormCaffe.bolt.processor;

import com.clarity.stormCaffe.model.Frame;
import org.apache.storm.task.TopologyContext;
import org.bytedeco.javacpp.caffeC3DOverlapLoss;

import java.util.Map;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-10-17.
 */
public class NetForward extends FrameProcessor {
    String model;
    String weight;
    String mean;
    String outputLayer;

    caffeC3DOverlapLoss.DoubleNet net;

    public NetForward(String model, String weight, String mean, String outputLayer, boolean useGPU) {
    }



    @Override
    public Frame execute(Frame input) throws Exception {
        return input;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {

    }
}
