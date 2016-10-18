package com.clarity.stormCaffe.topology;

import org.apache.commons.lang3.tuple.MutablePair;

public class Prediction extends MutablePair<String, Float> {
    public Prediction(String left, Float right) {
        setLeft(left);
        setRight(right);
    }
}