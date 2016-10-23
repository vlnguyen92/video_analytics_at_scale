package com.clarity.stormCaffe.topology;

import com.clarity.stormCaffe.topology.Classifier;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Scanner;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_core.Mat;

import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class GrabberOnly {

    public static void main(String[] args) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("/home/cc/video_data/testvid.mp4");
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        try {
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Mat mat = converter.convert(grabber.grabImage());
            while(!mat.empty()) {
                long start = System.nanoTime();
                mat = converter.convert(grabber.grabImage());
                System.out.println(System.nanoTime() - start);
            }
            //            mat = new Mat(image);

        } catch (FrameGrabber.Exception e){
            e.printStackTrace();
        }

    }
}
