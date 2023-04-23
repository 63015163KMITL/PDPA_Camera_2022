package com.cekmitl.pdpacameracensor.Process;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.cekmitl.pdpacameracensor.MainCameraActivity;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;


public class YoloV5Classifier implements Classifier {


    private int INPUT_SIZE = -1;
    private  int output_box;

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
        options.setNumThreads(4); //7 thread = 280 - 300 ms
        options.setUseXNNPACK(true);

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
        return MainCameraActivity.MINIMUM_CONFIDENCE_TF_OD_API;
    }

    protected float mNmsThresh = 0.4f;
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
        bitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        imgData.rewind();
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                int pixelValue = intValues[i * INPUT_SIZE + j];
                imgData.putFloat((((pixelValue >> 16) & 0xFF)) / 255.0f);
                imgData.putFloat((((pixelValue >> 8) & 0xFF)) / 255.0f);
                imgData.putFloat(((pixelValue & 0xFF)) / 255.0f);
            }
        }
    }

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

                final RectF rect = new RectF(
                        Math.max(0, xPos - w/2),
                        Math.max(0, yPos - h/2),
                        Math.min(bitmap.getWidth() - 1, xPos + w / 2),
                        Math.min(bitmap.getHeight() - 1, yPos+h/2));
                if (detectedClass == 0){
                    detections.add(new Recognition("" + offset, confidenceInClass, rect, detectedClass,xPos,yPos));
                }

            }
        }
        return nms(detections);
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