package nl.tno.stormcv;

import nl.tno.stormcv.spout.*;
import nl.tno.stormcv.bolt.*;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class WordCountTopology 
{
    public static void main( String[] args ) throws Exception
    {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new RandomSentenceSpoutPython(), 1);

        builder.setBolt("split", new SplitSentenceBolt(), 1).shuffleGrouping("spout");

        builder.setBolt("count", new WordCountBolt(), 1).fieldsGrouping("split", new Fields("dummy"));

        Config conf = new Config();
        conf.setDebug(true);

        if(args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar("word-count",conf,builder.createTopology());
        }
        else {
            conf.setMaxTaskParallelism(3);

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("word-count",conf,builder.createTopology());

            Thread.sleep(100000);
            cluster.shutdown();
        }

    }
}
