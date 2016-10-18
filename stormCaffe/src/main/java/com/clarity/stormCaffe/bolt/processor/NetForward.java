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
    boolean useGPU;

    caffeC3DOverlapLoss.FloatNet net;

    public NetForward(String model, String weight, String mean, String outputLayer, boolean useGPU) {
        this.model = model;
        this.weight = weight;
        this.mean = mean;
        this.outputLayer = outputLayer;
        this.useGPU = useGPU;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        caffeC3DOverlapLoss.Caffe.set_mode(useGPU ? caffeC3DOverlapLoss.Caffe.GPU : caffeC3DOverlapLoss.Caffe.CPU);

        net = new caffeC3DOverlapLoss.FloatNet(model);
        net.CopyTrainedLayersFrom(weight);

        if (net.num_inputs() > 1) {
            throw new IllegalArgumentException("Network should have exactly 1 input");
        }

        caffeC3DOverlapLoss.FloatBlob input_blob = net.input_blobs().get(0);
    }

    @Override
    public Frame execute(Frame input) throws Exception {
        return input;
    }
}
