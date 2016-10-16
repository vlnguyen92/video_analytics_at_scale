package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.util.Utils;

import java.io.IOException;
import java.io.InputStream;

public class ClassifierConfig {

    private Utils utils;

    private String modelName = "/model/deploy.prototxt";

    //@Value("${trainedModel}")
    private String trainingName = "/model/iter_10000.caffemodel";

    //@Value("${meanFile}")
    private String meanName = "/model/imagenet_mean.binaryproto";

    //@Value("${labelFile}")
    private String labelName = "/model/synsets.txt";


    public InputStream getModelFile() throws IOException {
        return utils.getStream(modelName);
    }


    public InputStream getTrainedFile() throws IOException {
        return utils.getStream(trainingName);
    }

    public InputStream getMeanFile() throws IOException {
        return utils.getStream(meanName);
    }


    public InputStream getLabelFile() throws IOException {
        return utils.getStream(labelName);
    }


}
