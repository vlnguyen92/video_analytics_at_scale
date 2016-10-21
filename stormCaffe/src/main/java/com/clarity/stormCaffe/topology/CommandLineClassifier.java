package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.topology.Classifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Scanner;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_core.Mat;

public class CommandLineClassifier {

    public static void main(String[] args) throws Exception {
        String rootDir = "/home/cc/video_analytics_at_scale/stormCaffe/";
        String modelFile = rootDir + "resources/model/deploy.prototxt";
        String trainFile = rootDir + "resources/model/bvlc_googlenet.caffemodel";
        String meanFile = rootDir + "resources/model/imagenet_mean.binaryproto";
        String labelFile = rootDir + "resources/model/synset_words.txt";
        Classifier classifier= new Classifier(modelFile,trainFile,meanFile,labelFile);

        String file = args[0];
        Mat img = imread(file,-1);
        System.out.println(file);
        System.out.println("Classified as: " + classifier.classify(img, 2).get(0).getLeft());
//        Scanner in = new Scanner(System.in);
//        int testNum;
//        testNum = in.nextInt();
    }
}
