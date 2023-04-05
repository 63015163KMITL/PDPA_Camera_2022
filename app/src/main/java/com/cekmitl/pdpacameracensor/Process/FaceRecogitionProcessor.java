package com.cekmitl.pdpacameracensor.Process;


import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FaceRecogitionProcessor {
    private Interpreter faceNetModelInterpreter;
    private ImageProcessor faceNetImageProcessor;
    private int INPUT_SIZE = 112;
    private PersonDatabase fileUtils;

    public FaceRecogitionProcessor(Interpreter faceNetModelInterpreter){
        try {
            this.fileUtils = new PersonDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.faceNetModelInterpreter = faceNetModelInterpreter;
        this.faceNetImageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(INPUT_SIZE,INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f,255f))
                .build();
    }

    public float[] recognize(Bitmap bitmap){
        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888,true) ;
        TensorImage tensorImage = TensorImage.fromBitmap(bmp);
        ByteBuffer faceNetByteBuffer = faceNetImageProcessor.process(tensorImage).getBuffer();
        float[][] faceOutputArray = new float[1][192];
        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray);
//        Log.e("FACERECOG", "faceOutputArray[0] : " +faceOutputArray[0][2]);
//        INDArray arr = Nd4j.create(faceOutputArray[0]);

        return faceOutputArray[0].clone();
    }

}
