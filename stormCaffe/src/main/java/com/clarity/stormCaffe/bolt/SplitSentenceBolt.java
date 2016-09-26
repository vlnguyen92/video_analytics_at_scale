package com.clarity.stormCaffe.bolt;

import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.task.ShellBolt;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.utils.Utils;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;

import java.util.Map;

public class SplitSentenceBolt extends BaseBasicBolt{

    //    public SplitSentenceBolt() {
    //        super("bash","split_sentence.sh");
    //    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String sentence = tuple.getString(0);
        for(String word: sentence.split("\\s+")) {
            collector.emit(new Values(word));
        }
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("dummy"));
    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}
