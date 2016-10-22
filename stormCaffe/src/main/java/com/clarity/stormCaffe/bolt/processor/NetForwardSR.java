package com.clarity.stormCaffe.bolt.processor;

import com.clarity.stormCaffe.model.Frame;
import org.apache.storm.task.TopologyContext;
import org.bytedeco.javacpp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aetf (aetf at unlimitedcodeworks dot xyz) on 16-10-17.
 */
public class NetForwardSR extends FrameProcessor {
    String model;
    String weight;
    String mean;
    String outputBlobName;
    boolean useGPU;

    caffeC3DOverlapLoss.FloatNet net;
    caffeC3DOverlapLoss.FloatBlob inputBlob;
    caffeC3DOverlapLoss.FloatBlob outputBlob;
    opencv_core.Size inputGeometry;
    opencv_core.Mat meanMat;

    public NetForwardSR(String model, String weight, String mean, boolean useGPU) {
        this(model, weight, mean, "", useGPU);
    }

    public NetForwardSR(String model, String weight, String mean, String outputLayer, boolean useGPU) {
        this.model = model;
        this.weight = weight;
        this.mean = mean;
        this.outputBlobName = outputLayer;
        this.useGPU = useGPU;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        caffeC3DOverlapLoss.Caffe.set_mode(useGPU ? caffeC3DOverlapLoss.Caffe.GPU : caffeC3DOverlapLoss.Caffe.CPU);

        net = new caffeC3DOverlapLoss.FloatNet(model);
        net.CopyTrainedLayersFrom(weight);

        if (net.num_inputs() > 1) {
            throw new IllegalArgumentException("Network should have exactly 1 input");
        }

        inputBlob = net.input_blobs().get(0);

        if (outputBlobName.isEmpty()) {
            outputBlob = net.output_blobs().get(0);
        } else {
            if (!net.has_blob(outputBlobName)) {
                throw new IllegalArgumentException("The network doesn't have a blob named " + outputBlobName);
            }
            outputBlob = net.blob_by_name(outputBlobName);
        }

        if (inputBlob.channels() != 3 && inputBlob.channels() != 1) {
            throw new IllegalArgumentException("Input layer should have 1 or 3 channels");
        }
        inputGeometry = new opencv_core.Size(inputBlob.width(), inputBlob.height());

        loadMean();
    }

    private void loadMean() {
        caffeC3DSampleRate.BlobProto meanProto = new caffeC3DSampleRate.BlobProto();
        if (!caffeC3DSampleRate.ReadProtoFromBinaryFile(mean, meanProto)) {
            throw new IllegalArgumentException("Invalid mean file " + mean);
        }

        caffeC3DSampleRate.FloatBlob meanBlob = new caffeC3DSampleRate.FloatBlob();
        meanBlob.FromProto(meanProto);
        if (meanBlob.channels() != inputBlob.channels()) {
            throw new IllegalArgumentException("Number of channels of mean file doesn't match input layer");
        }

        int channels = meanBlob.channels();
        int height = meanBlob.height();
        int width = meanBlob.width();

        FloatPointer meanData = meanBlob.cpu_data();
        opencv_core.MatVector vecMat = new opencv_core.MatVector();
        vecMat.resize(meanBlob.channels());

        for (int c = 0; c != channels; ++c) {
            FloatPointer curr = meanData.position(inputBlob.offset(0, c));
            opencv_core.Mat mat = new opencv_core.Mat(height, width, opencv_core.CV_32FC1, curr);
            vecMat.put(mat);
        }

        opencv_core.Mat mat = new opencv_core.Mat();
        opencv_core.merge(vecMat, mat);
        opencv_core.Scalar means = opencv_core.mean(mat);
        meanMat = new opencv_core.Mat(inputGeometry, mat.type(), means);
    }

    @Override
    public Frame execute(Frame input) throws Exception {
        HashMap<String, Object> meta = input.getMetadata();
        meta.put(outputBlobName, forward(input.getImage()));
        return input;
    }

    private List<opencv_core.Mat> forward(opencv_core.Mat img) {
        // resize to handle one image a time, change this when we are ready for batch processing
        inputBlob.Reshape(1, inputBlob.channels(), inputBlob.height(), inputBlob.width());

        preprocess(img, inputBlobAsMats(0));
        net.ForwardPrefilled();

        int height = outputBlob.height();
        int width = outputBlob.width();
        int channels = outputBlob.channels();
        FloatPointer outputData = outputBlob.cpu_data();
        List<opencv_core.Mat> listMats = new ArrayList<>();
        for (int i = 0; i != outputBlob.num(); ++i) {
            FloatPointer curr = outputData.position(outputBlob.offset(i));
            // no copy here, we will copy data when construct CVMat serializer
            listMats.add(new opencv_core.Mat(height, width, opencv_core.CV_32FC(channels), curr));
        }
        return listMats;
    }

    /**
     * Wrap input blob as a vector of opencv.Mat representing each channel.
     * @param n which image in blob to wrap
     * @return the wrapped vector of Mat
     */
    private opencv_core.MatVector inputBlobAsMats(int n) {
        int channels = inputBlob.channels();
        int height = inputBlob.height();
        int width = inputBlob.width();

        FloatPointer inputData = inputBlob.mutable_cpu_data();
        opencv_core.MatVector vecMat = new opencv_core.MatVector();
        vecMat.resize(inputBlob.channels());

        for (int c = 0; c != channels; ++c) {
            FloatPointer curr = inputData.position(inputBlob.offset(n, c));
            opencv_core.Mat mat = new opencv_core.Mat(height, width, opencv_core.CV_32FC1, curr);
            vecMat.put(mat);
        }
        return vecMat;
    }

    /**
     * Preprocess the image so it's suitable for the network, write output to outputChannels
     * @param img image to process
     * @param outputChannels outputChannels
     * @return the outputChannels
     */
    private opencv_core.MatVector preprocess(opencv_core.Mat img, opencv_core.MatVector outputChannels) {
        if (img.channels() == 3 && outputChannels.size() == 1) {
            opencv_imgproc.cvtColor(img, img, opencv_imgproc.COLOR_BGR2GRAY);
        } else if (img.channels() == 4 && outputChannels.size() == 1) {
            opencv_imgproc.cvtColor(img, img, opencv_imgproc.COLOR_BGRA2GRAY);
        }  else if (img.channels() == 4 && outputChannels.size() == 3) {
            opencv_imgproc.cvtColor(img, img, opencv_imgproc.COLOR_BGRA2BGR);
        } else if (img.channels() == 1 && outputChannels.size() == 3) {
            opencv_imgproc.cvtColor(img, img, opencv_imgproc.COLOR_GRAY2BGR);
        }

        if (img.size() != inputGeometry) {
            opencv_imgproc.resize(img, img, inputGeometry);
        }

        opencv_core.Mat img_float = new opencv_core.Mat();
        if (outputChannels.size() == 3) {
            img.convertTo(img_float, opencv_core.CV_32FC3);
        } else {
            img.convertTo(img_float, opencv_core.CV_32FC1);
        }

        opencv_core.split(img_float, outputChannels);
        return outputChannels;
    }
}
