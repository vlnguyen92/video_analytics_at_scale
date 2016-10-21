package com.clarity.stormCaffe.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;

import java.nio.ByteBuffer;

/**
 * This class provides kryo serialization for the JavaCV's CVMat and Rect objects, so that Storm can wrap them in tuples.
 * Serializable.CVMat - kryo serializable analog of opencv_core.Mat object.<p>
 * Serializable.Rect - kryo serializable analog of opencv_core.Rect object.<p>
 * Serializable.PatchIdentifier is also kryo serializable object,
 * which is used to identify each patch of the frame.<p>
 * <p>
 *
 * @author Nurlan Kanapin
 */
public class Serializable {

    /**
     * Kryo Serializable CVMat class.
     * Essential fields are image data itself, rows and columns count and type of the data.
     */
    public static class CVMatSerializer extends Serializer<opencv_core.Mat> {
        private opencv_core.Mat mat;

        @Override
        public void write(Kryo kryo, Output output, opencv_core.Mat mat) {
            output.writeInt(mat.rows());
            output.writeInt(mat.cols());
            output.writeInt(mat.type());
            output.writeLong(mat.elemSize());
            output.writeBytes(mat.data().asBuffer().array());
        }

        @Override
        public opencv_core.Mat read(Kryo kryo, Input input, Class<opencv_core.Mat> aClass) {
            int rows = input.readInt();
            int cols = input.readInt();
            int type = input.readInt();
            long size = input.readLong();
            byte[] data = input.readBytes((int) size);

            opencv_core.Mat mat = new opencv_core.Mat(rows, cols, type,
                    new BytePointer(ByteBuffer.wrap(data)));
            // Make sure we are using a safe copy
            opencv_core.Mat copied = new opencv_core.Mat();
            mat.copyTo(copied);
            return copied;
        }
    }
}
