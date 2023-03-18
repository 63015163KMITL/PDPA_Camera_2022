package com.cekmitl.pdpacameracensor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
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

    public static Bitmap tempBitmap = null;
    List<Classifier.Recognition> results = null;

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
    private int NUM_IMAGE = 20;
    private boolean isCamOn = false;
    private Thread detectThread;
//    private static final Object mPauseLock = new Object();
//    private static boolean mPaused = false;
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
//                        handleResult();


                        detectThread.join(200);
//                        Log.e("PROCESSCAPTURE", "");
//                        Log.e("PROCESSCAPTURE", "btn X : " + btn_X);
//                        Log.e("PROCESSCAPTURE", "btn Y : " + btn_Y);
//                        Log.e("PROCESSCAPTURE", "btn W : " + btn_W);
//                        Log.e("PROCESSCAPTURE", "btn H : " + btn_H);
//
//                        Log.e("PROCESSCAPTURE", "");

//                        Log.e("PROCESSCAPTURE", "btn_X - btn_W >= focusViewX = " + (btn_X - btn_W ));
//                        Log.e("PROCESSCAPTURE", "btn_Y + btn_H >= focusViewY = " + (btn_X + btn_H));

//                        Log.e("PROCESSCAPTURE", "WIDTH : " + (focusViewX - (focusViewW / 2)) + " < " + ( (btn_X - btn_W / 2) + btn_W ) + " > " + ((focusViewX + (focusViewW / 2))));
//                        Log.e("PROCESSCAPTURE", "HEIGHT : " + (focusViewY - (focusViewH / 2)) + " < " + ( (btn_Y - btn_H / 2) + btn_H ) + " > " + ((focusViewY + (focusViewH / 2))));
                        //Log.e("PROCESSCAPTURE", ( (focusViewY - btn_H / 2) ) + " > " + ( (btn_X + btn_W / 2) + btn_W )+ " < " + ( (focusViewY + btn_H / 2) ));
//                        Log.e("PROCESSCAPTURE", "IF btn_Y + btn_H >= focusViewY = " + (btn_X + btn_H <= focusViewX));

//                        if((btn_X - btn_W >= focusViewX) && (btn_Y - btn_H >= focusViewY)){
//                            Log.e("PROCESSCAPTURE", "BTN X+-W : OK");
//                        }


                        //checkFaceInCircle(btn_x, progressBar_ring);


                        Bitmap imageInput = null;
                        if (bb != null){
                            imageInput = BitmapEditor.rotateBitmap(bb,-90);
                        }

                        if (isFoundFace(imageInput)){
                            if (captureState){
                                face_crop_bitmap.add(imageInput);
                                round += 1;
                                Log.d("PROCESSCAPTURE", "number image: " + round);
                                TextView txt_new = findViewById(R.id.percent_complete);
                                txt_new.setText((round * 100 / NUM_IMAGE) + "%");

                                ProgressBar progressBar_ring = (ProgressBar) findViewById(R.id.progressBar); // initiate the progress bar
                                progressBar_ring.setProgress(round);
                                progressBar_ring.setMax(NUM_IMAGE);

                                Button btn_x = findViewById(R.id.x_button);

                                float focusViewX = progressBar_ring.getX() + progressBar_ring.getWidth() / 2;
                                float focusViewY = progressBar_ring.getY() + progressBar_ring.getHeight() / 2;
                                float focusViewW = progressBar_ring.getWidth();
                                float focusViewH = progressBar_ring.getHeight();

                                Log.e("PROCESSCAPTURE", "");
                                Log.e("PROCESSCAPTURE", "////////////////////////////////////");
                                Log.e("PROCESSCAPTURE", "focusView X : " + focusViewX);
                                Log.e("PROCESSCAPTURE", "focusView Y : " + focusViewY);
                                Log.e("PROCESSCAPTURE", "focusView W : " + focusViewW);
                                Log.e("PROCESSCAPTURE", "focusView H : " + focusViewH);

                                float btn_X = btn_x.getX() + btn_x.getWidth() / 2;
                                float btn_Y = btn_x.getY() + btn_x.getHeight() / 2;
                                float btn_W = btn_x.getWidth();
                                float btn_H = btn_x.getHeight();
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



//                            checkFaceInCircle

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
//        isCamOn = false;
        finish();
//        detectThread.interrupt();

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

//    public Bitmap cropBitmap(double X, double Y, double width, double height, float xPos, float yPos, Bitmap bm){
//        int width2 = 640;
//        int height2 = 480;
//
//        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
//        int h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
//        int w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
//        int x = (int) Math.round((float) (X * width2));
//        int y = (int) Math.round((float) (Y * height2));
//
//        Bitmap bb = BitmapEditor.getResizedBitmap(bm, width2, height2);
//        Bitmap b = BitmapEditor.crop(bb, x, y, w, h);
//
//        return b;
//    }


    //DETECT FACE FUNCTION
    public void handleResult() {
        if(tempBitmap != null){
            results = detector.recognizeImage(tempBitmap); //ส่งภาพไป คืนคำตอบกลับมาในรูปแบบ List
        }
    }

    public void setBox() {
        if (results != null) {

            //canvasView.setImageBitmap(bitmap);
            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                    //setFocusView(location.left, location.top, location.right, location.bottom, 777, result.getX(), result.getY());
                    Log.e("AAA","setFocusView OK");
                    //}else if (result.getDetectedClass() == 1){
                    //    setFocusView(location.left, location.top, location.right, location.bottom, 777, result.getX(), result.getY(), 1);
                    //}else{
                    //setFocusView(location.left, location.top, location.right, location.bottom, 777, result.getX(), result.getY(), 2);

                }
            }
        }
    }

    public boolean checkFaceInCircle(View focusView, View circleFrame){

        float focusViewX = focusView.getX() + focusView.getWidth() / 2;
        float focusViewY = focusView.getY() + focusView.getHeight() / 2;
        float focusViewW = focusView.getWidth();
        float focusViewH = focusView.getHeight();

        float circleFrameX = focusView.getX() + circleFrame.getWidth() / 2;
        float circleFrameY = focusView.getY() + circleFrame.getHeight() / 2;
        float circleFrameW = circleFrame.getWidth();
        float circleFrameH = circleFrame.getHeight();

        float MAX_WIDTH_circleFrameX = circleFrameX - (circleFrameW / 2);
        float MIN_WIDTH_circleFrameX = circleFrameX + (circleFrameW / 2);

        float MAX_HIGHT_circleFrameY = circleFrameY - (circleFrameH / 2);
        float MIN_HIGHT_circleFrameY = circleFrameY + (circleFrameH / 2);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

//        Log.d("VIEWPOSITION", "X : " + centreX + " | Y : " + centreY);
//        Log.d("VIEWPOSITION", "height : " + height + " | width : " + width);
//
        //X±(W/2) <= W.MAX && X±(W/2) >= W.MIN

        boolean checkWidth_focusView = (focusViewX + (focusViewW/2)) <= (circleFrameX / 2 - circleFrameW);

        return false;
    }

}

