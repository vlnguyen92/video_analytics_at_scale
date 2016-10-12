package com.clarity.stormCaffe.topology;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

import org.bytedeco.javacv.Frame;

import com.clarity.stormCaffe.util.Serializable;

public class Test
{
    private static FFmpegFrameGrabber grabber;

    public static void main(String[] args)
    {
        System.out.println(args[0]);
        grabber = new FFmpegFrameGrabber(args[0]);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        opencv_core.Mat mat;

        int count = 0;
        while (count < 10) {
            try {
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame = grabber.grabImage();
                mat = converter.convert(frame);

                //                mat = new opencv_core.Mat(image);
                System.out.println("dims: " + frame.imageChannels + " " + frame.imageDepth + " " + frame.imageHeight);
                Serializable.CVMat sMat = new Serializable.CVMat(mat);

            } catch (FrameGrabber.Exception e){
                e.printStackTrace();
            }
            count++;
        }

        System.out.println("Hello world");
    }

}
