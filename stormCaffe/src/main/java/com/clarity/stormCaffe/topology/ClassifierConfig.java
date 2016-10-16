package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.util.Utils;

import java.io.IOException;
import java.io.InputStream;

public class ClassifierConfig {

    private Utils utils;

    private String modelName;

    //@Value("${trainedModel}")
    private String trainingName;

    //@Value("${meanFile}")
    private String meanName;

    //@Value("${labelFile}")
    private String labelName;

    public ClassifierConfig(String model,String training, String mean, String label) {
        modelName = model;
        trainingName = training;
        meanName = mean;
        labelName = label;
    }


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
