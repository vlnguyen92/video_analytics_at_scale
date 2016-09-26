package com.clarity.stormCaffe.spout;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.KeyPoint;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import com.clarity.stormCaffe.util.Serializable;

import java.util.Map;
import java.util.Random;
import static com.clarity.stormCaffe.util.Constants.*;

public class FrameGetterSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;
    Random _rand;

    private String SOURCE_FILE;
    private FFmpegFrameGrabber grabber;

    public FrameGetterSpout(String SOURCE_FILE) {
        this.SOURCE_FILE = SOURCE_FILE;
    }

    @Override
    public void open(Map conf, TopologyContext context,
            SpoutOutputCollector collector) {
        _collector = collector;
        grabber = new FFmpegFrameGrabber(SOURCE_FILE);
//        KeyPoint kp = new KeyPoint();
        System.out.println("Created: " + SOURCE_FILE);

        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        _rand = new Random();
//        kp.deallocate();
    } 

    @Override
    public void nextTuple() {
        Utils.sleep(100);
        String[] sentences = new String[]{"Sentence AAA", "Sentence BBB", "Sentence CCC"};

        String sentence = sentences[_rand.nextInt(sentences.length)];
        _collector.emit(new Values(sentence));
    }

    @Override 
    public void ack(Object id) {}

    @Override 
    public void fail(Object id) {}

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }
}
/*
public class FrameRetrieverSpout extends BaseRichSpout {
    SpoutOutputCollector collector;
    private String SOURCE_FILE;
    private FFmpegFrameGrabber grabber;
    private long lastFrameTime;
    private int delayInMS;

    public FrameRetrieverSpout(String SOURCE_FILE) {
        this.SOURCE_FILE = SOURCE_FILE;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {

        grabber = new FFmpegFrameGrabber(SOURCE_FILE);
        KeyPoint kp = new KeyPoint();
        System.out.println("Created capture: " + SOURCE_FILE);

        this.collector = spoutOutputCollector;
        try {
            grabber.start();

        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        kp.deallocate();
    }

    IplImage image;
    Mat mat;

    @Override
    public void nextTuple() {
        long now = System.currentTimeMillis();
        if (now - lastFrameTime < delayInMS) {
            return;
        } else {
            lastFrameTime = now;
        }

        try {
            long start = System.currentTimeMillis();
            OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

            image = converter.convert(grabber.grab());
            mat = new Mat(image);

            Serializable.Mat sMat = new Serializable.Mat(mat);

            double fx = .25, fy = .25;
            double fsx = .5, fsy = .5;

            int W = sMat.getCols(), H = sMat.getRows();
            int w = (int) (W * fx + .5), h = (int) (H * fy + .5);
            int dx = (int) (w * fsx + .5), dy = (int) (h * fsy + .5);
            int patchCount = 0;
            for (int x = 0; x + w <= W; x += dx)
                for (int y = 0; y + h <= H; y += dy)
                    patchCount++;

            collector.emit(RAW_FRAME_STREAM, new Values(sMat, patchCount));
            for (int x = 0; x + w <= W; x += dx) {
                for (int y = 0; y + h <= H; y += dy) {
                    Serializable.PatchIdentifier identifier = new
                        Serializable.PatchIdentifier(new Serializable.Rect(x, y, w, h));
                    collector.emit(PATCH_STREAM, new Values(identifier, patchCount), identifier.toString());
                }
            }
            long nowTime = System.currentTimeMillis();
            System.out.printf("Sendout: " + nowTime + "," + ",used: " + (nowTime -start));
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(PATCH_STREAM, new Fields(FIELD_PATCH_IDENTIFIER, FIELD_PATCH_COUNT));
        outputFieldsDeclarer.declareStream(RAW_FRAME_STREAM, new Fields(FIELD_FRAME_ID, FIELD_FRAME_MAT, FIELD_PATCH_COUNT));
    }


}
*/
