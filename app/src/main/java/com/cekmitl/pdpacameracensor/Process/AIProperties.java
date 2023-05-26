package com.cekmitl.pdpacameracensor.Process;

import android.os.Environment;

import java.io.File;

public class AIProperties {
    public static final String TF_OD_API_MODEL_FILE = "model_480.tflite";
    public static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";
    public static final int TF_OD_API_INPUT_SIZE = 480;
    public static float OBJ_DETECT_CONFIDENT = 0.2f;
    public static float DIFF_RATIO = 0.65f;
    public static int MIN_MATCH = 2;
    public static String FACE_NET_MODEL = "mobile_face_net.tflite";
    public static final File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
}
