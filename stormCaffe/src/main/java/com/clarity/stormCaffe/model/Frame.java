package com.clarity.stormCaffe.model;

import org.bytedeco.javacpp.opencv_core;

import java.util.HashMap;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-10-17.
 */
public class Frame {
    opencv_core.Mat image;
    HashMap<String, Object> metadata;

    public Frame(opencv_core.Mat mat) {
        metadata = new HashMap<>();
        setImage(mat);
    }

    public opencv_core.Mat getImage() {
        return image;
    }

    public void setImage(opencv_core.Mat mat) {
        image = mat;
    }

    public HashMap<String, Object> getMetadata() {
        return metadata;
    }
}
