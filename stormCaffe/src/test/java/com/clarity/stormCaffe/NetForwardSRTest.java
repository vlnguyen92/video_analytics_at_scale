package com.clarity.stormCaffe;

import com.clarity.stormCaffe.bolt.processor.NetForwardSR;
import com.clarity.stormCaffe.model.Frame;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for NetForwardSR processor.
 */
public class NetForwardSRTest {
    private static File modelFolder;
    private static Map<String, String> models;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("bvlc_reference_caffenet.caffemodel", "http://dl.caffe.berkeleyvision.org/bvlc_reference_caffenet.caffemodel");
        aMap.put("caffenet.prototxt", "https://github.com/BVLC/caffe/raw/master/models/bvlc_reference_caffenet/deploy.prototxt");
        aMap.put("imagenet_mean.binaryproto", "https://github.com/vlnguyen92/video_analytics_at_scale/raw/javacpp-caffe/stormCaffe/resources/model/imagenet_mean.binaryproto");
        models = Collections.unmodifiableMap(aMap);
    }

    @ClassRule
    public static TemporaryFolder modelFolderCreator = new TemporaryFolder();

    @BeforeClass
    public static void downloadModels() throws Exception {
        modelFolder = modelFolderCreator.newFolder();
        File modelFile = new File(modelFolder, "model");
        for (String modelName : models.keySet()) {
            URL url = new URL(models.get(modelName));
            try (ReadableByteChannel ch = Channels.newChannel(url.openStream());
                 FileOutputStream fout = new FileOutputStream(getModel(modelName))) {
                fout.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
            } catch (Exception ex) {
                System.err.println("Error downloading " + modelName + " from " + models.get(modelName));
                ex.printStackTrace();
            }
        }
    }

    private static String getModel(String name) {
        return new File(modelFolder, name).getAbsolutePath();
    }


    /**
     * Test forward pass with simple reference model
     */
    @Test
    public void testSimpleForward() throws Exception {
        NetForwardSR nf = new NetForwardSR(getModel("caffenet.prototxt"),
                                           getModel("bvlc_reference_caffenet.caffemodel"),
                                           getModel("imagenet_mean.binaryproto"), false);

        // NOTE: NetForwardSR doesn't use these two, so it's safe to pass in null.
        nf.prepare(null, null);

        opencv_core.Mat catMat = opencv_imgcodecs.imread("/tmp/workspace/cat.jpg");
        Frame catFrame = nf.execute(new Frame(catMat));
        List<opencv_core.Mat> prob = (List<opencv_core.Mat>) catFrame.getMetadata().get("prob");

        Assert.assertEquals(prob.size(), 1);

        opencv_core.Mat probMat = prob.get(0);
        System.out.println("probMat size: " + probMat.size().height() + " " + probMat.size().width());
    }
}
