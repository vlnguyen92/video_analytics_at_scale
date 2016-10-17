package com.clarity.stormCaffe.util;

import com.clarity.stormCaffe.spout.FrameGetterSpout;
import org.apache.storm.topology.IRichSpout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-7-19.
 */
public class OpBuilder {
    Logger logger = LoggerFactory.getLogger(OpBuilder.class);
    public OpBuilder(String[] args) {
        // process args
        final String switchKeyword = "--";
        for (String arg : args) {
            if (arg.startsWith(switchKeyword)) {
                String[] kv = arg.substring(switchKeyword.length()).split("=");
                if (kv.length != 2) continue;
                int value = 1;
                try {
                    value = Integer.parseInt(kv[1]);
                } catch (NumberFormatException ex) {
                    // nothing
                }
                switch (kv[0]) {
                    // misc
                    // topology
                    case "topology-id":
                        topologyId = kv[1];
                        break;

                    default:
                        logger.warn("Ignoring unrecognized parameter {}", kv[0]);
                }
            } else {
                // Multiple files will be spread over the available spouts
                if (files.size() == 1) {
                    logger.warn("The spout only supports one input file, ignoring {}", arg);
                } else {
                    files.add(arg);
                }
            }
        }
        if (files.size() == 0) {
            logger.error("At least one input file should be specified!");
            throw new IllegalArgumentException("At least one input file should be specified in the command line");
        }
    }

    public String topologyId = "";

    public List<String> files = new ArrayList<>();

    public IRichSpout buildSpout() {
        IRichSpout spout = null;
        spout = new FrameGetterSpout(files.get(0));
        return spout;
    }
}
