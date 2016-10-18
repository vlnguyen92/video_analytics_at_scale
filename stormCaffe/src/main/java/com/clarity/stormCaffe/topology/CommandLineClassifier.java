package com.clarity.stormCaffe.topology;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommandLineClassifier {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Classifier classifier;

    private ClassifierConfig config;

    private void init() {
        try {
            classifier.setUp(config);

        } catch (IOException e) {
            log.error("Could not initialize", e);

        }

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected [-m | -i] file");
        }

        String file = args[1];
        System.out.println(file);
//        log.info("Classified as: " + classifier.classify(file, 2).get(0).getLeft());
    }
}
