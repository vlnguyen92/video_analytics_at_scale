package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.topology.Classifier;
import com.clarity.stormCaffe.topology.ClassifierConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

public class CommandLineClassifier {

    public static void main(String[] args) throws Exception {
        ClassifierConfig config = new ClassifierConfig("/model/deploy.prototxt","/model/iter_10000.caffemodel","/model/imagenet_mean.binaryproto","/model/synsets.txt");

        Classifier classifier = new Classifier(config);

        String file = args[0];
        System.out.println(file);
        System.out.println("Classified as: " + classifier.classify(file, 2).get(0).getLeft());
    }
}
