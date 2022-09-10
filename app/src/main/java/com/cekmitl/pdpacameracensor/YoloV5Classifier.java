package com.cekmitl.pdpacameracensor;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import com.cekmitl.pdpacameracensor.MainActivity;
import com.cekmitl.pdpacameracensor.Logger;
import com.cekmitl.pdpacameracensor.Utils;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;


/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public class YoloV5Classifier implements Classifier {

    // Float model
    private int INPUT_SIZE = -1;
    private  int output_box;

    // Number of threads in the java app
    // Pre-allocated buffers.
    private final Vector<String> labels = new Vector<>();
    private int[] intValues;

    private ByteBuffer imgData;
    private ByteBuffer outData;

    private int numClass;
    private Interpreter tfLite;

    public static YoloV5Classifier create(final AssetManager assetManager, final String modelFilename, final String labelFilename, final int inputSize) throws IOException {
        final YoloV5Classifier d = new YoloV5Classifier();
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        InputStream labelsInput = assetManager.open(actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            d.labels.add(line);
        }
        br.close();
        Interpreter.Options options = (new Interpreter.Options());
//        options.setNumThreads(NUM_THREADS);
//        options.addDelegate(new NnApiDelegate());
        options.setNumThreads(7); //7 thread = 280 - 300 ms
//        options.setUseNNAPI(true);
//                    options.setUseNNAPI(false);
//        options.setAllowFp16PrecisionForFp32(true);
        options.setUseXNNPACK(true);
        options.setCancellable(true);
        options.setAllowBufferHandleOutput(true);
        d.tfLite = new Interpreter(Utils.loadModelFile(assetManager, modelFilename), options);
        int numBytesPerChannel;
        numBytesPerChannel = 4; // Floating point
        d.INPUT_SIZE = inputSize;
        d.imgData = ByteBuffer.allocateDirect(d.INPUT_SIZE * d.INPUT_SIZE * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.INPUT_SIZE * d.INPUT_SIZE];
        d.output_box = (int) ((Math.pow((inputSize / 32.0), 2) + Math.pow((inputSize / 16.0), 2) + Math.pow((inputSize / 8.0), 2)) * 3);
        int[] shape = d.tfLite.getOutputTensor(0).shape();
        int numClass = shape[shape.length - 1] - 5;
        d.numClass = numClass;
        d.outData = ByteBuffer.allocateDirect(d.output_box * (numClass + 5) * numBytesPerChannel);
        d.outData.order(ByteOrder.nativeOrder());

        return d;
    }

    public float getObjThresh() {
        return MainActivity.MINIMUM_CONFIDENCE_TF_OD_API;
    }


    private YoloV5Classifier() {
    }

    protected float mNmsThresh = 0.6f;
    protected float box_iou(RectF a, RectF b) {
        return box_intersection(a, b) / box_union(a, b);
    }

    protected float box_intersection(RectF a, RectF b) {
        float w = overlap((a.left + a.right) / 2, a.right - a.left,(b.left + b.right) / 2, b.right - b.left);
        float h = overlap((a.top + a.bottom) / 2, a.bottom - a.top,(b.top + b.bottom) / 2, b.bottom - b.top);
        if (w < 0 || h < 0) return 0;
        return w * h;
    }

    protected float box_union(RectF a, RectF b) {
        float i = box_intersection(a, b);
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
    }

    protected float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = Math.max(l1, l2);
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = Math.min(r1, r2);
        return right - left;
    }

    protected void convertBitmapToByteBuffer(Bitmap bitmap) {
        bitmap.getPixels(intValues, 0, 640, 0, 0, 640, 640);

        imgData.rewind();
        for (int i = 0; i < 640; ++i) {
            for (int j = 0; j < 640; ++j) {
                int pixelValue = intValues[i * 640 + j];
                imgData.putFloat((((pixelValue >> 16) & 0xFF)) / 255.0f);
                imgData.putFloat((((pixelValue >> 8) & 0xFF)) / 255.0f);
                imgData.putFloat(((pixelValue & 0xFF)) / 255.0f);
            }
        }
    }

//    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
//        ByteBuffer byteBuffer;
//        byteBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * d.INPUT_SIZE * 3 * numBytesPerChannel);
//        byteBuffer.order(ByteOrder.nativeOrder());
//        int[] intValues = new int[640 * 640];
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
//                bitmap.getWidth(), bitmap.getHeight());
//        int pixel = 0;
//        for (int i = 0; i < 640; ++i) {
//            for (int j = 0; j < 640; ++j) {
//                final int val = intValues[pixel++];
//                byteBuffer.putFloat(
//                        (((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
//                byteBuffer.putFloat(
//                        (((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
//                byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
//            }
//        }
//        return byteBuffer;
//    }

    //    long startTime = 0;
    public ArrayList<Recognition> recognizeImage(Bitmap bitmap) {

        convertBitmapToByteBuffer(bitmap); // 20 - 40 ms
        Map<Integer, Object> outputMap = new HashMap<>();
        outData.rewind();
        outputMap.put(0, outData);

        Object[] inputArray = {imgData};

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //700ms -> 300ms

        ByteBuffer byteBuffer = (ByteBuffer) outputMap.get(0);
        assert byteBuffer != null;
        byteBuffer.rewind();

        ArrayList<Recognition> detections = new ArrayList<>();

        float[][][] out = new float[1][output_box][numClass + 5];

        for (int i = 0; i < output_box; ++i) {
            for (int j = 0; j < numClass + 5; ++j) {
                out[0][i][j] = byteBuffer.getFloat();
            }
            // ทำการแปลงจากค่าในช่วง [0,1] ให้เป็นตำแหน่งของภาพ (*ด้วยขนาดของภาพ)
            // Denormalize xywh
//            for (int j = 0; j < 4; ++j) {
//                out[0][i][j] *= INPUT_SIZE;
//
//            }
        }
        for (int i = 0; i < output_box; ++i){
            final int offset = 0;
            final float confidence = out[0][i][4];
            int detectedClass = -1;
            float maxClass = 0;
            final float[] classes = new float[labels.size()];

            System.arraycopy(out[0][i], 5, classes, 0, labels.size());

            for (int c = 0; c < labels.size(); ++c) {
                if (classes[c] > maxClass) {
                    detectedClass = c;
                    maxClass = classes[c];
                }
            }
            final float confidenceInClass = maxClass * confidence;
            if (confidenceInClass > getObjThresh()) {
                final float xPos = out[0][i][0];
                final float yPos = out[0][i][1];
                final float w = out[0][i][2];
                final float h = out[0][i][3];

                //final RectF rect = new RectF(
               //         Math.max(0, xPos),
                //        Math.max(0, yPos),
                //        Math.min(bitmap.getWidth() - 1, xPos + w),
                //        Math.min(bitmap.getHeight() - 1, yPos + h));
/*
                final RectF rect = new RectF(
                        yPos - h/2,
                        xPos - w/2,
                        Math.min(bitmap.getHeight() - 1, h),
                        Math.min(bitmap.getWidth() - 1, w));
                        */
                final RectF rect = new RectF(
                        Math.max(0, yPos - h/2),
                        Math.max(0, xPos - w/2),
                        Math.min(bitmap.getHeight() - 1, yPos+h/2),
                        Math.min(bitmap.getWidth() - 1, xPos + w / 2));

                detections.add(new Recognition("" + offset, labels.get(detectedClass), confidenceInClass, rect, detectedClass,xPos,yPos));
            }
        }
        return nms(detections);
    }

    @Override
    public void enableStatLogging(boolean debug) {

    }

    @Override
    public String getStatString() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void setNumThreads(int num_threads) {

    }

    @Override
    public void setUseNNAPI(boolean isChecked) {

    }

    protected ArrayList<Recognition> nms(ArrayList<Recognition> list) {
        ArrayList<Recognition> nmsList = new ArrayList<>();

        for (int k = 0; k < labels.size(); k++) {

            //1.find max confidence per class
            PriorityQueue<Recognition> pq = new PriorityQueue<>(50, (lhs, rhs) -> {
                // Intentionally reversed to put high confidence at the head of the queue.
                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
            });

            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).getDetectedClass() == k) {
                    pq.add(list.get(i));
                }
            }
            //2.do non maximum suppression
            while (pq.size() > 0) {

                //insert detection with max confidence
                Recognition[] a = new Recognition[pq.size()];
                Recognition[] detections = pq.toArray(a);
                Recognition max = detections[0];
                nmsList.add(max);
                pq.clear();

                for (int j = 1; j < detections.length; j++) {
                    Recognition detection = detections[j];
                    RectF b = detection.getLocation();
                    if (box_iou(max.getLocation(), b) < mNmsThresh) {
                        pq.add(detection);
                    }
                }
            }
        }
        return nmsList;
    }
}