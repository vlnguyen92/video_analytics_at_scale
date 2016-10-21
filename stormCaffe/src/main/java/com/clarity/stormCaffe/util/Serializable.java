package com.clarity.stormCaffe.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;

import java.io.*;
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
 * @see Serializable.CVMat
 * @see Serializable.Rect
 * @see Serializable.PatchIdentifier
 */
public class Serializable {

    /**
     * Kryo Serializable CVMat class.
     * Essential fields are image data itself, rows and columns count and type of the data.
     */
    public static class CVMat implements KryoSerializable, java.io.Serializable {
        private byte[] data;
        private int rows, cols, type;

        public int getRows() {
            return rows;
        }

        public int getCols() {
            return cols;
        }

        public int getType() {
            return type;
        }

        public CVMat() {
        }

        /**
         * Creates new serializable CVMat given its format and data.
         *
         * @param rows Number of rows in the CVMat object
         * @param cols Number of columns in the CVMat object
         * @param type OpenCV type of the data in the CVMat object
         * @param data Byte data containing image.
         */
        public CVMat(int rows, int cols, int type, byte[] data) {
            this.rows = rows;
            this.cols = cols;
            this.type = type;
            this.data = data;
        }

        /**
         * Creates new serializable CVMat from opencv_core.CVMat
         *
         * @param mat The opencv_core.Mat
         */
        public CVMat(opencv_core.Mat mat) {

            if (!mat.isContinuous())
                mat = mat.clone();

            this.rows = mat.rows();
            this.cols = mat.cols();
            this.type = mat.type();
            int size = mat.arraySize();
//            this.data = new byte[size];
//            ByteBuffer buf = (ByteBuffer) mat.createBuffer();
//            buf.get(this.data);
//            System.out.println("HERE");

            //            ByteBuffer bb = mat.getByteBuffer();
            //            bb.rewind();
            //            this.data = new byte[size];
            //            while (bb.hasRemaining())  // should happen only once
            //                bb.get(this.data);
        }

        /**
         * Creates new serializable CVMat given its format and data.
         *
         * @param input Byte data containing image.
         */
        public CVMat(byte[] input) {
            ByteArrayInputStream bis = new ByteArrayInputStream(input);
            ObjectInput in = null;
            try {
                in = new ObjectInputStream(bis);
                this.rows = in.readInt();
                this.cols = in.readInt();
                this.type = in.readInt();
                int size = in.readInt();
                this.data = new byte[size];
                int readed = 0;
                while (readed < size) {
                    readed += in.read(data, readed, size - readed);
                }
                //System.out.println("in: " + this.rows + "-" + this.cols + "-" + this.type + "-" + size + "-" + readed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public byte[] toByteArray() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            try {
                out = new ObjectOutputStream(bos);
                out.writeInt(this.rows);
                out.writeInt(this.cols);
                out.writeInt(this.type);
                out.writeInt(this.data.length);
                out.write(this.data);
                out.close();
                byte[] int_bytes = bos.toByteArray();
                bos.close();

                //System.out.println("out: " + this.rows + "-" + this.cols + "-" + this.type + "-" + this.data.length + "-" + int_bytes.length);
                return int_bytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static byte[] toByteArray(Serializable.CVMat rawFrame, Serializable.CVMat optFlow) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            try {
                out = new ObjectOutputStream(bos);

                out.writeInt(rawFrame.rows);
                out.writeInt(rawFrame.cols);
                out.writeInt(rawFrame.type);
                out.writeInt(rawFrame.data.length);
                out.write(rawFrame.data);

                out.writeInt(optFlow.rows);
                out.writeInt(optFlow.cols);
                out.writeInt(optFlow.type);
                out.writeInt(optFlow.data.length);
                out.write(optFlow.data);

                out.close();
                byte[] int_bytes = bos.toByteArray();
                bos.close();

                //System.out.println("out: " + this.rows + "-" + this.cols + "-" + this.type + "-" + this.data.length + "-" + int_bytes.length);
                return int_bytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static Serializable.CVMat[] toSCVMat(byte[] input) {
            ByteArrayInputStream bis = new ByteArrayInputStream(input);
            ObjectInput in = null;
            Serializable.CVMat rawFrame = new Serializable.CVMat();
            Serializable.CVMat optFlow = new Serializable.CVMat();

            try {
                in = new ObjectInputStream(bis);
                rawFrame.rows = in.readInt();
                rawFrame.cols = in.readInt();
                rawFrame.type = in.readInt();
                int size = in.readInt();
                rawFrame.data = new byte[size];
                int readed = 0;
                while (readed < size) {
                    readed += in.read(rawFrame.data, readed, size - readed);
                }
                optFlow.rows = in.readInt();
                optFlow.cols = in.readInt();
                optFlow.type = in.readInt();
                size = in.readInt();
                optFlow.data = new byte[size];
                readed = 0;
                while (readed < size) {
                    readed += in.read(optFlow.data, readed, size - readed);
                }

                return new Serializable.CVMat[]{rawFrame, optFlow};
                //System.out.println("in: " + this.rows + "-" + this.cols + "-" + this.type + "-" + size + "-" + readed);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * @return Converts this Serializable CVMat into JavaCV's CVMat
         */
        public opencv_core.Mat toJavaCVMat() {
            return new opencv_core.Mat(rows, cols, type, new BytePointer(data));
        }

        @Override
        public void write(Kryo kryo, Output output) {
            data = new byte[size];
            ByteBuffer buf = (ByteBuffer) mat.createBuffer();
            buf.get(data);

            output.writeInt(rows);
            output.writeInt(cols);
            output.writeInt(type);
            output.writeInt(data.length);
            output.writeBytes(data);
        }

        @Override
        public void read(Kryo kryo, Input input) {
            this.rows = input.readInt();
            this.cols = input.readInt();
            this.type = input.readInt();
            int size = input.readInt();
            this.data = input.readBytes(size);
        }
    }

    /**
     * Kryo Serializable Rect class.
     */
 

    /**
     * Kryo Serializable ScoredRect class. Subclass of Serializable.Rect, it includes a score for the rectangle.
     * The score would usually be used to represent a matched score (e.g. HOG matching).
     */


    /**
     * This is a serializable class used for patch identification. Each patch needs to be distinguished form others.
     * Each patch is uniquely identified by the id of its frame and by the rectangle it corresponds to.
     */




    /**
     * This is a serializable class used for patch identification. Each patch needs to be distinguished form others.
     * Each patch is uniquely identified by the id of its frame and by the rectangle it corresponds to.
     */


    public static opencv_core.Mat ByteArray2CvCVMat(byte[] input){
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            int rows = in.readInt();
            int cols = in.readInt();
            int type = in.readInt();
            int size = in.readInt();
            byte[] data = new byte[size];
            int readed = 0;
            while (readed < size) {
                readed += in.read(data, readed, size - readed);
            }

            return new opencv_core.Mat(rows, cols, type, new BytePointer(data));
            //            System.out.println("in: " + this.rows + "-" + this.cols + "-" + this.type + "-" + input.length + "-" + size + "-" + readed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
