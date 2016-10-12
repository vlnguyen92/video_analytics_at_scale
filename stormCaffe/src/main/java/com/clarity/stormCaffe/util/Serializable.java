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
            this.data = new byte[size];
            ByteBuffer buf = (ByteBuffer) mat.createBuffer();
            buf.get(this.data);
            System.out.println("HERE");

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
            output.writeInt(this.rows);
            output.writeInt(this.cols);
            output.writeInt(this.type);
            output.writeInt(this.data.length);
            output.writeBytes(this.data);
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
    public static class Rect implements KryoSerializable, java.io.Serializable {
        /**
         * x, y, width, height - x and y coordinates of the left upper corner of the rectangle, its width and height
         */
        public int x, y, width, height;

        public Rect() {
        }

        public Rect(opencv_core.Rect rect) {
            x = rect.x();
            y = rect.y();
            width = rect.width();
            height = rect.height();
        }

        public Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
        }

        public opencv_core.Rect toJavaCVRect() {
            return new opencv_core.Rect(x, y, width, height);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Rect rect = (Rect) o;

            if (height != rect.height) return false;
            if (width != rect.width) return false;
            if (x != rect.x) return false;
            if (y != rect.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }

        @Override
        public void write(Kryo kryo, Output output) {
            output.writeInt(x);
            output.writeInt(y);
            output.writeInt(width);
            output.writeInt(height);
        }

        @Override
        public void read(Kryo kryo, Input input) {
            x = input.readInt();
            y = input.readInt();
            width = input.readInt();
            height = input.readInt();
        }
    }

    /**
     * Kryo Serializable ScoredRect class. Subclass of Serializable.Rect, it includes a score for the rectangle.
     * The score would usually be used to represent a matched score (e.g. HOG matching).
     */
    public static class ScoredRect extends Serializable.Rect implements java.io.Serializable, Comparable<ScoredRect> {
        private static final long serialVersionUID = -6827975402116588963L;

        /**
         * Score of the rectangle. Usually used to represent the matched score.
         */
        public double score;
        public String name;

        public ScoredRect() {
            super();
        }

        public ScoredRect(opencv_core.Rect rect) {
            super(rect);
        }

        public ScoredRect(opencv_core.Rect rect, double score) {
            super(rect);
            this.score = score;
        }

        public ScoredRect(Serializable.Rect rect) {
            this(rect.x, rect.y, rect.width, rect.height);
        }

        public ScoredRect(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        public ScoredRect(int x, int y, int width, int height, double score) {
            super(x, y, width, height);
            this.score = score;
        }

        public boolean hasSameDimensions(Serializable.Rect that) {
            return super.equals(that);
        }

        public int getX1() {
            return x;
        }

        public int getX2() {
            return x + width;
        }

        public int getY1() {
            return y;
        }

        public int getY2() {
            return y + height;
        }

        public int getArea() {
            return width * height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScoredRect rect = (ScoredRect) o;

            if (height != rect.height) return false;
            if (width != rect.width) return false;
            if (x != rect.x) return false;
            if (y != rect.y) return false;
            if (score != score) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + width;
            result = 31 * result + height;

            result = result ^ Double.hashCode(score);

            return result;
        }

        @Override
        public int compareTo(ScoredRect that) {
            return Double.compare(that.score, this.score);
        }
    }

    /**
     * This is a serializable class used for patch identification. Each patch needs to be distinguished form others.
     * Each patch is uniquely identified by the id of its frame and by the rectangle it corresponds to.
     */
    public static class PatchIdentifier implements KryoSerializable, java.io.Serializable {
        /**
         * Frame id of this patch
         */
        public int frameId;
        /**
         * Rectangle or Region of Interest of this patch.
         */
        public Rect roi;

        public PatchIdentifier() {
        }

        /**
         * Creates PatchIdentifier with given frame id and rectangle.
         *
         * @param frameId
         * @param roi
         */
        public PatchIdentifier(int frameId, Rect roi) {
            this.roi = roi;
            this.frameId = frameId;
        }

        @Override
        public void write(Kryo kryo, Output output) {
            output.writeInt(frameId);
            output.writeInt(roi.x);
            output.writeInt(roi.y);
            output.writeInt(roi.width);
            output.writeInt(roi.height);
        }

        @Override
        public void read(Kryo kryo, Input input) {
            frameId = input.readInt();
            int x = input.readInt();
            int y = input.readInt();
            int width = input.readInt();
            int height = input.readInt();
            roi = new Rect(x, y, width, height);
        }

        /**
         * String representation of this patch identifier.
         *
         * @return the string in the format N%04d@%04d@%04d@%04d@%04d if roi is not null, and N%04d@null otherwise.
         */
        public String toString() {
            if (roi != null)
                return String.format("N%04d@%04d@%04d@%04d@%04d", frameId, roi.x, roi.y, roi.x + roi.width, roi.y + roi.height);
            return String.format("N%04d@null", frameId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PatchIdentifier that = (PatchIdentifier) o;

            if (frameId != that.frameId) return false;
            if (roi != null ? !roi.equals(that.roi) : that.roi != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = frameId;
            result = 31 * result + (roi != null ? roi.hashCode() : 0);
            return result;
        }
    }

    public static class CvPoint2D32f implements KryoSerializable, java.io.Serializable {
        float x;
        float y;

        public CvPoint2D32f() {
        }

        public CvPoint2D32f(opencv_core.CvPoint2D32f p) {
            this.x = p.x();
            this.y = p.y();
        }

        public CvPoint2D32f(CvPoint2D32f p) {
            this.x = p.x();
            this.y = p.y();
        }

        public opencv_core.CvPoint2D32f toJavaCvPoint2D32f() {
            return new opencv_core.CvPoint2D32f().x(this.x).y(this.y);
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public void x(float x) {
            this.x = x;
        }

        public void y(float y) {
            this.y = y;
        }

        @Override
        public void write(Kryo kryo, Output output) {
            output.writeFloat(this.x);
            output.writeFloat(this.y);
        }

        @Override
        public void read(Kryo kryo, Input input) {
            this.x = input.readFloat();
            this.y = input.readFloat();
        }
    }

    /**
     * This is a serializable class used for patch identification. Each patch needs to be distinguished form others.
     * Each patch is uniquely identified by the id of its frame and by the rectangle it corresponds to.
     */
    public static class PatchIdentifierCVMat implements java.io.Serializable {

        public PatchIdentifier identifier;
        public CVMat sCVMat;


        /**
         * Creates PatchIdentifier with given frame id and rectangle.
         * @param frameId
         * @param roi
         * @param sCVMat
         */
        public PatchIdentifierCVMat(int frameId, Rect roi, CVMat sCVMat) {
            this.identifier = new PatchIdentifier(frameId, roi);
            this.sCVMat = sCVMat;
        }

        /**
         * String representation of this patch identifier.
         *
         * @return the string in the format N%04d@%04d@%04d@%04d@%04d if roi is not null, and N%04d@null otherwise.
         */
        public String toString() {
            return this.identifier.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PatchIdentifierCVMat that = (PatchIdentifierCVMat) o;
            return this.identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            return this.identifier.hashCode();
        }
    }

    public static byte[] CvCVMat2ByteArray(opencv_core.Mat mat) {
        if (!mat.isContinuous()) {
            mat = mat.clone();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeInt(mat.rows());
            out.writeInt(mat.cols());
            out.writeInt(mat.type());
            out.writeInt(mat.arraySize());

            byte[] data = new byte[mat.arraySize()];
            ByteBuffer buf = (ByteBuffer) mat.createBuffer();
            buf.get(data);
            out.write(data);
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
