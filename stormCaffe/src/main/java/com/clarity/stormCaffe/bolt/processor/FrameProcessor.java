package com.clarity.stormCaffe.bolt.processor;

import com.clarity.stormCaffe.model.Frame;
import org.apache.storm.task.TopologyContext;

import java.util.Map;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-10-17.
 */
public abstract class FrameProcessor {
    /**
     * Subclasses must implement this method which is responsible for analysis of
     * received Frame objects.
     *
     * @param input
     * @return
     */
    public abstract Frame execute(Frame input) throws Exception;

    public abstract void prepare(Map stormConf, TopologyContext context);
}
