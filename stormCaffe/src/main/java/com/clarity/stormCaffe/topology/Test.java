package com.clarity.stormCaffe.topology;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

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
        opencv_core.IplImage image;

        int count = 0;
        while (count < 10) {
            try {
                OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
                image = converter.convert(grabber.grab());

                mat = new opencv_core.Mat(image);
                System.out.println("dims: " + mat.empty());
                //                Serializable.CVMat sMat = new Serializable.CVMat(mat);

            } catch (FrameGrabber.Exception e){
                e.printStackTrace();
            }
            count++;
        }

        System.out.println("Hello world");
    }

}
