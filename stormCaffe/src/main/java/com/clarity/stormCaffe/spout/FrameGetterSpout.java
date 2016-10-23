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
import static com.clarity.stormCaffe.util.Constants.*;
import com.clarity.stormCaffe.util.SentWithTime;

public class FrameGetterSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;

    private String SOURCE_FILE;
    private FFmpegFrameGrabber grabber;

    int fps = 0;
    long stepBwFrameNano;
    long emitAmount;
    long nextFrameEmitTime;
    long emitLeft;

    public FrameGetterSpout(String file) {
        this(file, 40);
    }

    public FrameGetterSpout(String SOURCE_FILE, int fps) {
        this.SOURCE_FILE = SOURCE_FILE;
        stepBwFrameNano = Math.max(1,1000000000/fps);
        emitAmount = 1;
        // Pre-compute some values
    }

    @Override
    public void open(Map conf, TopologyContext context,
            SpoutOutputCollector collector) {
        _collector = collector;
        grabber = new FFmpegFrameGrabber(SOURCE_FILE);
        System.out.println("Created: " + SOURCE_FILE);

        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        nextFrameEmitTime = System.nanoTime();
        emitLeft = emitAmount;
    } 

    Mat mat;

    @Override
    public void nextTuple() {
        if (emitLeft <= 0 && nextFrameEmitTime <= System.nanoTime()) {
            emitLeft = emitAmount;
            nextFrameEmitTime += stepBwFrameNano;
        }

        if(emitLeft > 0) {
            //Emit cv Mat 
            try {
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                long start = System.nanoTime();
                mat = converter.convert(grabber.grabImage());
                System.out.println("TIMER " + (System.nanoTime() - start));
                if (mat != null) {
                    Serializable.CVMat sMat = new Serializable.CVMat(mat);
                    _collector.emit(new Values(sMat), new SentWithTime(sMat, nextFrameEmitTime - stepBwFrameNano));
                }
            } catch (FrameGrabber.Exception e){
                e.printStackTrace();
            }
            emitLeft--;
        }

    }

    @Override 
    public void ack(Object id) {
        long end = System.nanoTime();
        SentWithTime st = (SentWithTime)id;
        //        System.out.println("TIMER " + (end - st.time));
    }
    //
    //    @Override 
    //    public void fail(Object id) {}
    //
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("raw-frame"));
    }
}
