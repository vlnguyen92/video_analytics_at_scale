package com.clarity.stormCaffe.spout;

import com.clarity.stormCaffe.model.Frame;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.util.Map;

public class FrameGetterSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;

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
        //        kp.deallocate();
    } 

    Mat mat;

    @Override
    public void nextTuple() {

        //Emit cv Mat 
        try {
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            mat = converter.convert(grabber.grabImage());
            if (mat != null) {
                Frame frame = new Frame(mat);
                _collector.emit(new Values(frame));
            }
            //            mat = new Mat(image);

        } catch (FrameGrabber.Exception e){
            e.printStackTrace();
        }
    }

    //    @Override 
    //    public void ack(Object id) {}
    //
    //    @Override 
    //    public void fail(Object id) {}
    //
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("raw-frame"));
    }
}
