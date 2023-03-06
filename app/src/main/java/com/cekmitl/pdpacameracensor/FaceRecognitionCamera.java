package com.cekmitl.pdpacameracensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import java.io.FileWriter;

public class FaceRecognitionCamera extends AppCompatActivity implements ImageAnalysis.Analyzer{
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.4f;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";
    private Classifier detector;


    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    public PreviewView previewView;
    private ImageCapture imageCapture;

    private TextView txt_new;

    ArrayList<Bitmap> b = new ArrayList<Bitmap>(10);

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


        txt_new = findViewById(R.id.percent_complete);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        detectThread.interrupt();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        detectThread.interrupt();
    }
    private int NUM_SAVED = 0;
    private int NUM_IMAGE = 10;
    private boolean isCamOn = false;
    private Thread detectThread;
//    private static final Object mPauseLock = new Object();
//    private static boolean mPaused = false;
    private int round = 0;

    private void threadWorking(){
        detectThread = new Thread(new Runnable() {
            public void run() {
                while (isCamOn) {
                    try {
                        detectThread.join(200);
                        Log.e("TIMER_CAM","Number of picutre"+face_crop_bitmap.size());
//                        handleResult();

//                        if (round % 2 == 0){
//                            face_crop_bitmap.clear();
                        Bitmap imageInput = null;
//                        NUM_INPUT+=1;
                        if (bb != null){
                            imageInput = BitmapEditor.rotateBitmap(bb,-90);
                        }

                        if (isFoundFace(imageInput)){
                            face_crop_bitmap.add(imageInput);
                            round += 1;
                            Log.d("PROCESSCAPTURE", "number image: "+round);
                        }

                            //txt_new.setText("" + round);
                            //Toast.makeText(FaceRecognitionCamera.this, "round : " + round, Toast.LENGTH_SHORT).show();
//
//                            detectThread.interrupt();

//                        }

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

    Bitmap myBitmap;

//    public void displayNum(){
//        //txt_new.setText(NUM_IMAGE + "/" + i);
//        Toast.makeText(this, "i = " + NUM_SAVED, Toast.LENGTH_SHORT).show();
//    }

    public void saveTemp(ArrayList<Bitmap> bitmap){
        File file_temp2 = null;
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

            String path = DOC_PATH + "/temp/";
            //File checkFile = new File(path+"/"+String.valueOf(i)+".txt");

            //ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

           // Log.e("saveTemp","PATH = " + file_temp2.getAbsolutePath());

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
                .setTargetResolution(new Size(1080, 1440))
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
            //b.remove(0);
            //b.add(bm);
            bb = bm;
            Log.e("scanface","wait 1000; = ");
            Log.e("scanface","FACE = " + bb.toString());

        image.close();

    }

    public void SelectFace() throws IOException {
//        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.test);

//        icon = BitmapEditor.getResizedBitmap(icon, 50, 50);
        //b.add(icon);
//        detectThread.interrupt();

        saveTemp(face_crop_bitmap);

        Intent intent = new Intent(getApplicationContext(), AddNewFaceActivity.class);
        intent.putExtra("key", "1");
        //intent.putExtra("bitmap", b.get(0));
        intent.putExtra("temp_num", String.valueOf(NUM_SAVED));
        Log.d("PASSINGROUND", String.valueOf(round));

        startActivity(intent);
        finish();

    }


    public boolean isFoundFace(Bitmap bitmap){
        if (bitmap != null){
            Bitmap bitmap1 = BitmapEditor.getResizedBitmap(bitmap, 320, 320);
            List<Classifier.Recognition> results = detector.recognizeImage(bitmap1);
            return results != null;
        }
        return false;
//        for (final Classifier.Recognition result : results) {
//
//            final RectF location = result.getLocation();
//            //                           X - Y - Width - Height
//            if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
//                Log.d("LOCATION", String.valueOf(location.left)+" " +String.valueOf(location.top)+" " +String.valueOf(location.right)+" " +String.valueOf(location.bottom));
//
//                Bitmap m = cropBitmap(location.left, location.top, location.right, location.bottom,  result.getX(), result.getY(), bitmap1);
//                ImageView img = new ImageView(getApplication());
//                img.setImageBitmap(m);
//            }
//        }
    }

    public Bitmap cropBitmap(double X, double Y, double width, double height, float xPos, float yPos, Bitmap bm){
        int width2 = 640;
        int height2 = 480;

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        int h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        int w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        int x = (int) Math.round((float) (X * width2));
        int y = (int) Math.round((float) (Y * height2));

        Bitmap bb = BitmapEditor.getResizedBitmap(bm, width2, height2);
        Bitmap b = BitmapEditor.crop(bb, x, y, w, h);

        return b;
    }

}

