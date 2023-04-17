package com.cekmitl.pdpacameracensor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.cekmitl.pdpacameracensor.ImageEditor.BitmapEditor;
import com.cekmitl.pdpacameracensor.Process.Classifier;
import com.cekmitl.pdpacameracensor.Process.Utils;
import com.cekmitl.pdpacameracensor.Process.YoloV5Classifier;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class FaceRecognitionCamera extends AppCompatActivity implements ImageAnalysis.Analyzer{
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";
    private Classifier detector;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    public PreviewView previewView;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Remove Action Bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        try {
            Log.e("TEST", "detector OK");
            //makeText(this, "detector OK", LENGTH_SHORT).show();
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            Log.e("TEST", "detector ERROR");
            //makeText(this, "detector ERROR", LENGTH_SHORT).show();
            e.printStackTrace();
        }

        setContentView(R.layout.activity_face_recognition_camera);

        previewView = findViewById(R.id.previewView);

        //เริ่มการทำงานของ Camera X
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());

        Button next_button = findViewById(R.id.next_button);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                SelectFace();
            }
        });

        isCamOn = true;
        threadWorking();
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private int NUM_SAVED = 0;
    private int NUM_IMAGE = 20;
    private boolean isCamOn = false;
    private Thread detectThread;
    private int counting = 0;
    private int count_to_capture = 2; //wait 4
    private int round = 0;
    private boolean captureState = false;

    private void threadWorking(){
        detectThread = new Thread(new Runnable() {
            public void run() {
                while (isCamOn) {
                    try {

                        Log.e("TIMER_CAM","Number of picutre" + face_crop_bitmap.size());

                        detectThread.join(200);


                        Bitmap imageInput = null;
                        if (bb != null){
                            imageInput = BitmapEditor.rotateBitmap(bb,-90);
                        }

                        if (isFoundFace(imageInput)){
                            if (captureState){
                                face_crop_bitmap.add(imageInput);
                                round += 1;
                                TextView txt_new = findViewById(R.id.percent_complete);
                                txt_new.setText((round * 100 / NUM_IMAGE) + "%");

                                ProgressBar progressBar_ring = (ProgressBar) findViewById(R.id.progressBar); // initiate the progress bar
                                progressBar_ring.setProgress(round);
                                progressBar_ring.setMax(NUM_IMAGE);

                                Button btn_x = findViewById(R.id.x_button);

                            }else{
                                if (counting < count_to_capture){
                                    ++counting;
                                    captureState = false;
                                    TextView txt_new = findViewById(R.id.percent_complete);
                                    txt_new.setText(String.valueOf(counting));
                                }else {
                                    captureState = true;
                                }
                            }

                        }else{
                            counting = 0;
                            captureState = false;
                            TextView txt_new = findViewById(R.id.percent_complete);
                            txt_new.setText("Face Not Found");
                        }

                        if (round >= NUM_IMAGE){
                            isCamOn= false;
                            SelectFace();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        detectThread.start();
    }


    public void saveTemp(ArrayList<Bitmap> bitmap){
        NUM_SAVED = 0;
        File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        File newF = new File(DOC_PATH+"/temp");
        if (!newF.exists()){
            newF.mkdirs();
        }

        for(int i = 0; i < bitmap.size(); i++){

            try (FileOutputStream out = new FileOutputStream(DOC_PATH + "/temp/" + i + ".png")) {

                bitmap.get(i).compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                NUM_SAVED += 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // เริ่มต้นการทำงานของ Camera X
    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), this);

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetResolution(new Size(480, 480))
                //.setTargetResolution(new Size(1080, 1440))
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
    }

    public ArrayList<Bitmap> face_crop_bitmap = new ArrayList<>();

    Bitmap bb;

    @Override
    public void analyze(@NonNull ImageProxy image) {
//            face_crop_bitmap.clear();
            @SuppressLint("UnsafeOptInUsageError")
            Bitmap bm = Utils.toBitmap(Objects.requireNonNull(image.getImage()));
            bm = BitmapEditor.getResizedBitmap(bm, 320, 320);
            bb = bm;
            Log.e("scanface","wait 1000; = ");
            Log.e("scanface","FACE = " + bb.toString());

        image.close();

    }

    public void SelectFace() throws IOException {
        saveTemp(face_crop_bitmap);

        Intent intent = new Intent(getApplicationContext(), AddNewFaceActivity.class);
        intent.putExtra("key", "1");
        //intent.putExtra("bitmap", b.get(0));
        intent.putExtra("temp_num", String.valueOf(NUM_SAVED));
        Log.d("PASSINGROUND", String.valueOf(round));

        startActivity(intent);
        finish();
    }

    public boolean isFoundFace(Bitmap bitmap) {
        if (bitmap != null) {
            Bitmap bitmap1 = BitmapEditor.getResizedBitmap(bitmap, 320, 320);
            List<Classifier.Recognition> results = detector.recognizeImage(bitmap1);
            for (final Classifier.Recognition result : results) {

                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                    Log.d("FOUNDFACE", "isFoundFace: True");
                    return true;
                }

            }

        }
        Log.d("FOUNDFACE", "isFoundFace: False");
        return false;
    }

}

