package com.clarity.stormCaffe.util;

import com.clarity.stormCaffe.util.Serializable.CVMat;

public class SentWithTime {

    public final CVMat mat;
    public final long time;

    public SentWithTime(CVMat mat, long time) {
        this.mat = mat;
        this.time = time;
    }
}
