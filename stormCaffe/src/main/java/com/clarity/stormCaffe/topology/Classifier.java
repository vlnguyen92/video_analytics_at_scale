package com.clarity.stormCaffe.topology;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.caffe.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class Classifier {

    private FloatNet  net_;

    private Size input_geometry_;

    int num_channels_;

    private Mat mean_;

    private List<String> labels_;

    public Classifier(String model,String training, String mean, String label) throws IOException {
        //this is where you set mode
        Caffe.set_mode(Caffe.GPU);
        net_ = new FloatNet(model, caffe.TEST);
        net_.CopyTrainedLayersFrom(training);
        if(net_.num_inputs() != 1) {
            System.out.println("Network should have exactly one input.");  
        }
        if(net_.num_outputs() != 1) {
            System.out.println("Network should have exactly one output.");
        }

        FloatBlob input_layer = net_.input_blobs().get(0);
        num_channels_ = input_layer.channels();

        if (num_channels_ != 3 && num_channels_ != 1) {
            System.out.println("Input layer should have 1 or 3 channels.");
        }
        input_geometry_ = new Size(input_layer.width(), input_layer.height());
        setMean(mean);
        labels_ = Files.lines(Paths.get(label)).collect(Collectors.toList());
        FloatBlob output_layer = net_.output_blobs().get(0);
        if(labels_.size() != output_layer.channels()){
            System.out.println("Number of labels is different from the output layer dimension.");
            System.out.println(labels_.size());
        }
    }

    private void preprocess(Mat img) {
        Mat sample = new Mat();
        if (img.channels() == 3 && num_channels_ == 1) {
            cvtColor(img, sample, opencv_imgproc.COLOR_BGR2GRAY);
        } else if (img.channels() == 4 && num_channels_ == 1) {
            cvtColor(img, sample, opencv_imgproc.COLOR_BGRA2GRAY);
        }  else if (img.channels() == 4 && num_channels_ == 3) {
            cvtColor(img, sample, opencv_imgproc.COLOR_BGRA2BGR);
        } else if (img.channels() == 1 && num_channels_ == 3) {
            cvtColor(img, sample, opencv_imgproc.COLOR_GRAY2BGR);
        } else {
            sample = img;
        }

        Mat sample_resized = new Mat();
        if (sample.size() != input_geometry_) {
            resize(sample, sample_resized, input_geometry_);
        }  else {
            sample_resized = sample;
        }

        Mat sample_float = new Mat();
        if (num_channels_ == 3) {
            sample_resized.convertTo(sample_float, opencv_core.CV_32FC3);
        } else {
            sample_resized.convertTo(sample_float, opencv_core.CV_32FC1);
        }

        Mat sample_normalized = new Mat();

        subtract(sample_float, mean_, sample_normalized);

        FloatRawIndexer indexer = sample_normalized.createIndexer();
        int length = ((int) indexer.width()) * ((int) indexer.height());
        int count = 0;
        float[] data = new float[length * ((int) indexer.channels())];
        for (int z = 0; z < indexer.channels(); z++) {
            for (int x = 0; x < indexer.width(); x++) {
                for (int y = 0; y < indexer.height(); y++) {
                    float value = indexer.get(x,y,z);
                    data[count++] = value;
                }
            }
        }

        FloatBlob input_layer = net_.input_blobs().get(0);
        input_layer.cpu_data().put(data);

    }

    private List<Float> predict(Mat img) {
        FloatBlob input_layer = net_.input_blobs().get(0);
        input_layer.Reshape(1,
                num_channels_,
                input_geometry_.height(),
                input_geometry_.width());
        net_.Reshape();

        MatVector input_channels = new MatVector();

        preprocess(img);

        net_.Forward();
        FloatBlob output_layer = net_.output_blobs().get(0);
        FloatPointer begin = output_layer.cpu_data();
        List<Float> rvalue = new ArrayList<>();

        for (int i =0; i < labels_.size(); i++) {
            rvalue.add(begin.get(i));
        }

        return rvalue;
    }

    private void setMean(String meanFile) {
        BlobProto blob_proto = new BlobProto();
        caffe.ReadProtoFromBinaryFileOrDie(meanFile, blob_proto);

        FloatBlob mean_blob = new FloatBlob();
        mean_blob.FromProto(blob_proto);
        if(mean_blob.channels() != num_channels_) {
            System.out.println("Number of channels of mean file doesn't match input layer.");
        }

        Mat[] mats = new Mat[num_channels_];
        for (int i = 0; i < num_channels_; i++) {
            mats[i] = new Mat();
        }
        MatVector channels = new MatVector(mats);
        FloatPointer cpuData = mean_blob.mutable_cpu_data();

        int totalSize = num_channels_ * mean_blob.height() * mean_blob.width();
        float[] data = new float[totalSize];
        cpuData.get(data);

        for (int i = 0; i < num_channels_; i++) {
            int base =  i * (mean_blob.height() * mean_blob.width());
            float[] data_= new float[mean_blob.height() * mean_blob.width()];
            for (int j = 0; j < mean_blob.height() * mean_blob.width(); j++) {
                data_[j] = data[base + j];
            }
            channels.put(i, new Mat(data_));
        }

        Mat tmpMean = new Mat();
        merge(channels, tmpMean);

        Scalar channel_mean = mean(tmpMean);
        mean_ = new Mat(input_geometry_, tmpMean.type(), channel_mean);
    }

    public List<Prediction> classify(Mat matImage, int amount) {
        List<Float> output = predict(matImage);
        if(output.size() != labels_.size()) {
            System.out.println("Expected a probability for every class");
        }
        List<Prediction> predictions = new ArrayList<>();
        for (int i = 0; i < output.size(); i++) {
            //			log.info(String.format("Read: %s as prediction", output.get(i)));
            predictions.add(new Prediction(labels_.get(i), output.get(i)));
        }
        return predictions.stream()
            .sorted((l, r) -> -1 * Float.compare(l.getRight(), r.getRight()))
            .limit(amount)
            .collect(Collectors.toList());
    }

    //    public List<Prediction> classify(File image, int amount) throws IOException {
    //        Mat matImage = imread(image.getAbsolutePath(), -1);
    //        return classify(matImage, amount);
    //    }
    //
    public List<Prediction> classify(String fname, int amount) throws IOException {
        Mat matImage = imread(fname, -1);
        return classify(matImage, amount);
    }

}
