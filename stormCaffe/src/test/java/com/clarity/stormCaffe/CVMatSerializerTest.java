package com.clarity.stormCaffe;

import com.clarity.stormCaffe.util.Serializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bytedeco.javacpp.opencv_core;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.FloatBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_32FC3;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-10-22.
 */
public class CVMatSerializerTest {
    private static Kryo kryo;

    private opencv_core.Mat mat;
    private Output output;

    @BeforeClass
    public static void setUp() {
        kryo = new Kryo();
        kryo.register(opencv_core.Mat.class, new Serializable.CVMatSerializer());
    }

    @Before
    public void before() {
        mat = new opencv_core.Mat();
        mat = opencv_core.Mat.eye(3, 3, CV_32FC3).asMat();
        output = new Output(2048);
    }

    @Test
    public void testSerialize() {
        kryo.writeObject(output, mat);
        output.close();
    }

    @Test
    public void testRoundabout() {
        kryo.writeObject(output, mat);
        Input input = new Input(output.getBuffer());
        opencv_core.Mat newMat = kryo.readObject(input, opencv_core.Mat.class);
        Assert.assertEquals(mat.rows(), newMat.rows());
        Assert.assertEquals(mat.cols(), newMat.cols());
        Assert.assertEquals(mat.type(), newMat.type());

        FloatBuffer buf = mat.createBuffer();
        float[] d1 = new float[buf.capacity()];
        buf.get(d1);

        buf = newMat.createBuffer();
        float[] d2 = new float[buf.capacity()];
        buf.get(d2);
        Assert.assertArrayEquals(d1, d2, 1e-15f);
    }
}
