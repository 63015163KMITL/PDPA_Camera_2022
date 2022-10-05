package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import android.graphics.RectF;


import java.util.List;



public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, View.OnClickListener, View.OnLongClickListener, Runnable{


    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    public PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private ImageButton bCapture;
    private ImageButton btnChangResol;
    private boolean statRecord;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ProcessCameraProvider cameraProvider;

    private TextView txtDebug;
    private LinearLayout top_center;
    public int img_width = 0, img_height = 0;

    //Resolution Image
    private String state_serol = "4:3";

    int state_pdpd = 0;

    //DETECT FACE
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private Classifier detector;
    public static final int TF_OD_API_INPUT_SIZE = 224;
    private static final String TF_OD_API_MODEL_FILE = "n_224.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";
    public int processTime;
    private Thread detectThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.new_preview);
        ChangResolutionImage(4, 3);

        frameFocusLayout = findViewById(R.id.fram_focus_layout);
        txtDebug = findViewById(R.id.text_debug);

        top_center = findViewById(R.id.top_center);

        //ลบ Action Bar ออก
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        //ส่วนสร้าง Object ต่าง ๆ --------------------------------------------------------------------------------------
        previewView = findViewById(R.id.previewView);                       // Preview Camera X

        //PHOTO
        bCapture = findViewById(R.id.bCapture);                             // ปุ่มถ่ายรูป Capture Button
        ImageButton btnGallery = findViewById(R.id.button_gallery);                     // ปุ่มดูรูปในอัลบั้ม Gallery
        ImageButton btnReverse = findViewById(R.id.reverse_camera_button);              // ปุ่มดสลับกล้องหน้าหลัง Font / BACK
        btnChangResol = findViewById(R.id.change_resolution);
        btnChangResol.setBackgroundResource(R.drawable.ic_resol_4_3);
        statRecord = false;                                                 //bRecord = findViewById(R.id.bRecord);

        bCapture.setOnClickListener(this);
        bCapture.setOnLongClickListener(this);
        btnGallery.setOnClickListener(this);
        btnReverse.setOnClickListener(this);
        btnChangResol.setOnClickListener(this);
        top_center.setOnClickListener(this);

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

        try {
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        detectThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    handleResult();
                    try {
                        detectThread.join(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        detectThread.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        run();
    }

    @Override
    public void run() {

        Log.e("A","RUN OK");
        setBox();
        frameFocusLayout.postDelayed(this,25);

    }


    @SuppressLint({"RestrictedApi", "NonConstantResourceId"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bCapture:
                if (!statRecord) {
                    capturePhoto();
                } else {
                    bCapture.setImageResource(R.drawable.ic_camera);
                    videoCapture.stopRecording();
                    statRecord = false;
                }
                break;
            case R.id.reverse_camera_button:
                flipCamera();
                break;
            case R.id.button_gallery:
                Intent galleryIntent = new Intent(MainActivity.this, GalleryActivity.class);
                MainActivity.this.startActivity(galleryIntent);
                break;
            case R.id.change_resolution:
                switch (state_serol) {
                    case "4:3":
                        ChangResolutionImage(16, 9);
                        state_serol = "16:9";
                        btnChangResol.setBackgroundResource(R.drawable.ic_resol_16_9);
                        break;
                    case "16:9":
                        ChangResolutionImage(1, 1);
                        btnChangResol.setBackgroundResource(R.drawable.ic_resol_1_1);
                        state_serol = "1:1";

                        break;
                    case "1:1":
                        ChangResolutionImage(4, 3);
                        btnChangResol.setBackgroundResource(R.drawable.ic_resol_4_3);
                        state_serol = "4:3";
                        break;
                }
                break;
            case R.id.top_center:
                if(state_pdpd == 0){
                    slideView2(top_center, top_center.getLayoutParams().height, 300,top_center.getLayoutParams().width, 1050);
                    //slideView2(top_center, top_center.getLayoutParams().height, 2500,top_center.getLayoutParams().width, 2500);
                    state_pdpd = 1;
                }else {
                    slideView2(top_center, top_center.getLayoutParams().height, 100,top_center.getLayoutParams().width, 220);
                    state_pdpd = 0;
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.bCapture) {
            bCapture.setImageResource(R.drawable.ic_recording);
            recordVideo();
        }
        return statRecord = true;
    }

    public static void slideView(View view, int currentHeight, int newHeight) {
        ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(500);
        slideAnimator.addUpdateListener(animation1 -> {
            view.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public static void slideView2(View view, int currentHeight, int newHeight, int currentWidth, int newWidth) {
        ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(300);
        ValueAnimator slideAnimator2 = ValueAnimator.ofInt(currentWidth, newWidth).setDuration(300);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */
        slideAnimator.addUpdateListener(animation1 -> {
            view.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });

        slideAnimator2.addUpdateListener(animation1 -> {
            view.getLayoutParams().width = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });
        /*  We use an animationSet to play the animation  */
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
        /*  We use an animationSet to play the animation  */
        AnimatorSet animationSet2 = new AnimatorSet();
        animationSet2.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet2.play(slideAnimator2);
        animationSet2.start();
    }

    public void ChangResolutionImage(int h, int w) {

        if(h == 16 && w == 9){
            state_serol = "16:9";
        }else if(h == 4 && w == 3){
            state_serol = "4:3";
        }else if(h == 1 && w == 1){
            state_serol = "1:1";
        }

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        img_width = width;
        img_height = (width / w) * h;
        RelativeLayout view = findViewById(R.id.frame_top_camera);
        RelativeLayout view2 = findViewById(R.id.main_relative_layout);

        if (h == 1) {
            slideView(view, view.getLayoutParams().height, (width / 3) * 4);
        } else {
            slideView(view, view.getLayoutParams().height, img_height);
        }

        slideView(view2, view2.getLayoutParams().height, img_height);

        RelativeLayout view3 = findViewById(R.id.main_preview);
        ViewGroup.LayoutParams layoutParams3 = view3.getLayoutParams();

        layoutParams3.width = img_width;
        layoutParams3.height = (width / 9) * 16;
        view3.setLayoutParams(layoutParams3);

    }

    // ตั้งค่ากล้อง Camera X ---------------------------------------------------------------------------------------------
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            makeText(this, "landscape", LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            makeText(this, "portrait", LENGTH_SHORT).show();
        }
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    //ตรวจสอบการใช้งาน ว่าขณะนี้ใช้กล้องหน้า หรือกล้องหลังอยู่
    private void flipCamera() {
        if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            lensFacing = CameraSelector.LENS_FACING_FRONT;
        } else if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            lensFacing = CameraSelector.LENS_FACING_BACK;
        }
        startCameraX(cameraProvider);
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


        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetResolution(new Size(1080, 1440))
                .build();
        //imageAnalysis.setAnalyzer(getExecutor(), this);

        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void analyze(@NonNull ImageProxy image) {
        Log.d("TAG", "analyze: ");

        long startTime = System.currentTimeMillis();
        @SuppressLint("UnsafeOptInUsageError") Bitmap bm = Utils.toBitmap(Objects.requireNonNull(image.getImage()));
        image.close();
        bm = Utils.getResizedBitmap(bm, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        tempBitmap = Bitmap.createBitmap(bm, 0, 0, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                matrix, false);

        if(lensFacing == CameraSelector.LENS_FACING_FRONT){
            tempBitmap = flip(tempBitmap);
        }


        long endTime = System.currentTimeMillis();
//        txtDebug.setText("Total Time = " + (endTime - startTime) + " ms" + "\nModule = " + processTime + "ms" + "\nLoop = " + ((endTime - startTime) - processTime) + "ms");
    }

    @SuppressLint("RestrictedApi")
    private void recordVideo() {
        if (videoCapture != null) {
            long timestamp = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                videoCapture.startRecording(
                        new VideoCapture.OutputFileOptions.Builder(
                                getContentResolver(),
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                        ).build(),
                        getExecutor(),
                        new VideoCapture.OnVideoSavedCallback() {
                            @Override
                            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                                makeText(MainActivity.this, "Video has been saved successfully.", LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                makeText(MainActivity.this, "Error saving video: " + message, LENGTH_SHORT).show();
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // การถ่ายรูป
    private void capturePhoto() {
        //สร้างตำแหน่งเก็บไฟล์ภาพชั่วคราว
        File file_temp;
        try {
            file_temp = File.createTempFile("geek", ".jpg", null);

            imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(file_temp).build(), getExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            //makeText(MainActivity.this, "Photo has been saved successfully.", LENGTH_SHORT).show();

                            //นำภาพในไฟล์ชั่วคราว ดึงมาใส่ใน Object BitMap
                            Bitmap myBitmap = BitmapFactory.decodeFile(file_temp.getAbsolutePath());

                            //Preview ใน iCon ของ Gallery
                            ImageButton imgBtn = findViewById(R.id.button_gallery);
                            imgBtn.setImageBitmap(myBitmap);
                            //เปิดหน้า Preview Activity
                            Intent myIntent = new Intent(MainActivity.this, PreviewActivity.class);
                            myIntent.putExtra("key", file_temp.getAbsolutePath()); //Optional parameters
                            myIntent.putExtra("resolution", state_serol);
                            MainActivity.this.startActivity(myIntent);
                        }
                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), LENGTH_SHORT).show();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// END - ตั้งค่ากล้อง Camera X ---------------------------------------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    public void setFocusView(double X, double Y, double width, double height, int str, float xPos, float yPos) {


        //removeView();
        int x, y, h, w;
        Display display = getWindowManager().getDefaultDisplay();
        int width2 = display.getWidth();
        int height2 = 0;

        if(Objects.equals(state_serol, "16:9")){
            height2 = 1920;
        }else if(Objects.equals(state_serol, "4:3")){
            height2 = 1440;
        }else if(Objects.equals(state_serol, "1:1")){
            height2 = 1080;
        }

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = (int) Math.round((float) ((2 * (height-yPos)) * height2));
        w = (int) Math.round((float) ((2 * (width-xPos)) * width2));
        x = (int) Math.round((float) (X * width2));
        y = (int) Math.round((float) (Y * height2));

        //Toast.makeText(this, "w = " + w + "/nh = " + h, LENGTH_SHORT).show();
 //       txtDebug.setText("w = " + w + "\nh = " + h);


        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

        @SuppressLint("InflateParams") View focus_frame = inflater.inflate(R.layout.focus_frame, null);
/*
        if ((h > 70 || w > 70) && ((h < 130 || w < 130))){
            focus_frame = inflater.inflate(R.layout.focus_frame_m, null);
        }else if ((h > 0 || w > 0) && ((h < 70 || w < 70))){
            focus_frame = inflater.inflate(R.layout.focus_frame_s, null);
        }
 */
        //if(type == 1){
        //    focus_frame = inflater.inflate(R.layout.focus_frame, null);
        //}else if(type == 0){
        //    focus_frame = inflater.inflate(R.layout.emoji_layout, null);
        //}



        //
        focus_frame.setId(str);
        int strId = focus_frame.getId();
        focus_frame.setOnClickListener(view -> makeText(MainActivity.this, "CLICK = " + strId, LENGTH_SHORT).show());
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(x, y, 0, 0);

   //     TextView txt = new TextView(this);
 //       txt.setTextSize(6);
   //     txt.setText(h + "x" + w);


        frameFocusLayout.addView(focus_frame, params1);
   //     frameFocusLayout.addView(txt, params1);
    }

    List<Classifier.Recognition> results = null;
    //DETECT FACE FUNCTION
    public void handleResult() {
        if(tempBitmap != null){
            results = detector.recognizeImage(tempBitmap); //ส่งภาพไป คืนคำตอบกลับมาในรูปแบบ List
        }
    }

    public void setBox() {
        if (results != null) {
            Log.e("AAA","results != NULL");
            clearFocus();
            //canvasView.setImageBitmap(bitmap);
            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                    setFocusView(location.left, location.top, location.right, location.bottom, 777, result.getX(), result.getY());
                    Log.e("AAA","setFocusView OK");
                    //}else if (result.getDetectedClass() == 1){
                    //    setFocusView(location.left, location.top, location.right, location.bottom, 777, result.getX(), result.getY(), 1);
                    //}else{
                    //setFocusView(location.left, location.top, location.right, location.bottom, 777, result.getX(), result.getY(), 2);

                }
            }

        }else {
            Log.e("AAA","results = NULL");
        }
    }

    RelativeLayout frameFocusLayout;
    public void clearFocus(){
        if (null != frameFocusLayout && frameFocusLayout.getChildCount() > 0) {
            frameFocusLayout.removeViews (0, frameFocusLayout.getChildCount());
        }
    }
    Bitmap tempBitmap = null;

    public Bitmap flip(Bitmap d)
    {
        Matrix m = new Matrix();
        m.preScale(1, -1);
        Bitmap dst = Bitmap.createBitmap(d, 0, 0, d.getWidth(), d.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }


}