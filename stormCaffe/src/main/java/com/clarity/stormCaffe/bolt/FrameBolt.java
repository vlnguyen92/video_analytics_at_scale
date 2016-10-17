package com.clarity.stormCaffe.bolt;

import com.clarity.stormCaffe.bolt.processor.FrameProcessor;
import com.clarity.stormCaffe.model.Frame;
import com.clarity.stormCaffe.util.Serializable;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-10-17.
 */
public class FrameBolt extends BaseRichBolt {
    private Logger logger = LoggerFactory.getLogger(FrameBolt.class);

    protected OutputCollector collector;
    protected String boltName;

    protected List<FrameProcessor> processors;

    public FrameBolt(FrameProcessor processor) {
        this(Collections.singletonList(processor));
    }

    public FrameBolt(List<FrameProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.boltName = context.getThisComponentId();

        Config.registerSerialization(stormConf, Serializable.CVMat.class);
        Config.registerSerialization(stormConf, Frame.class);

        try {
            for (FrameProcessor pro : processors) {
                pro.prepare(stormConf, context);
            }
        } catch (Exception ex) {
            logger.error("Failed to prepare bolt {}, due to exception", boltName, ex);
        }
    }

    @Override
    public void execute(Tuple input) {
        boolean hadError = false;
        try {
            Frame frame = (Frame) input.getValueByField("raw-frame");

            for (FrameProcessor pro : processors) {
                frame = pro.execute(frame);
            }
            collector.emit(input, new Values(frame));
        } catch (Exception e) {
            logger.error("Unable to process input", e);
            hadError = true;
        }

        if (hadError) {
            collector.fail(input);
        } else {
            collector.ack(input);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("raw-frame"));
    }
}
