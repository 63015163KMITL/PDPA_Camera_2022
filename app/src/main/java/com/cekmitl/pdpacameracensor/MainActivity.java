package com.cekmitl.pdpacameracensor;
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
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, View.OnClickListener, View.OnLongClickListener {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private ImageButton btnGallery, bCapture, btnReverse, btnChangResol;
    private boolean statRecord, cam_reverse_state;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private RelativeLayout GetSizeImg;

    private TextView tvTimer, txtDebug, txtDebug2;
    private RelativeLayout relativeLyout, fram_camera;
    public int img_width, img_height;

    //Resolution Image
    private String state_serol = "4:3";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SET Fullscreen
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.new_preview);

        ChangResolutionImage(4, 3);

        txtDebug = (TextView) findViewById(R.id.text_debug);
        txtDebug2 = (TextView) findViewById(R.id.text_debug2);

        //0.461358 0.338458 0.200941 0.198085
        //setFocusView(0.461358,0.338458,0.200941,0.198085);
        //0 0.356173 0.413889 0.0432099 0.0444444
        //0 0.167901, 0.411574, 0.0469136, 0.0490741

        //setFocusView(0.356173, 0.413889, 0.0432099, 0.0444444);
        //setFocusView(0.167901, 0.411574, 0.0469136, 0.0490741);




        //ลบ Action Bar ออก
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();



        //ส่วนสร้าง Object ต่าง ๆ --------------------------------------------------------------------------------------
            previewView = findViewById(R.id.previewView);                       // Preview Camera X
            fram_camera = findViewById(R.id.fram_camera);



        //PHOTO
            bCapture = findViewById(R.id.bCapture);                             // ปุ่มถ่ายรูป Capture Button
            btnGallery = findViewById(R.id.button_gallery);                     // ปุ่มดูรูปในอัลบั้ม Gallery
            btnReverse = findViewById(R.id.reverse_camera_button);              // ปุ่มดสลับกล้องหน้าหลัง Font / BACK
            btnChangResol = findViewById(R.id.change_resolution);

        //VIDEO
            ImageView imgV = findViewById(R.id.icon_recording);                 // สัญลักษณ์ บันทึกวีดิโอ
            //Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
            //imgV.startAnimation(startAnimation);
            tvTimer = findViewById(R.id.textViewTimer);
        //END - ส่วนสร้าง Object ต่าง ๆ --------------------------------------------------------------------------------------

        //Canvas ---------------------------------------------------------------------------------------------------------
            relativeLyout = findViewById(R.id.main_relative_layout);
            //MyView myView = new MyView(this);
            //relativeLyout.addView(myView);
        //END - Canvas ---------------------------------------------------------------------------------------------------

        // Set ค่าเริ่มต้นตัวแปร ------------------------------------------------------------------------------------------------
            statRecord = false;                                                 //bRecord = findViewById(R.id.bRecord);
            cam_reverse_state = true;                                           //true = Back Camera, false = Font Camera
        // END - Set ค่าเริ่มต้นตัวแปร ------------------------------------------------------------------------------------------




        bCapture.setOnClickListener(this);
        bCapture.setOnLongClickListener(this);
        btnGallery.setOnClickListener(this);
        btnReverse.setOnClickListener(this);
        btnChangResol.setOnClickListener(this);



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




        //setContentView(R.layout.new_preview);



    }

    public static void slideView(View view,
                                 int currentHeight,
                                 int newHeight) {

        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(500);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
        });

        /*  We use an animationSet to play the animation  */

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public void ChangResolutionImage(int h, int w){
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();


        img_width = width;
        img_height = (width / w) * h;

        RelativeLayout view = findViewById(R.id.frame_top_camera);
        RelativeLayout view2 = findViewById(R.id.main_relative_layout);

        if (h == 1){
            slideView(view, view.getLayoutParams().height, 1440);
        }else {
            slideView(view, view.getLayoutParams().height, img_height);
        }

        slideView(view2, view2.getLayoutParams().height, img_height);

        /*
        RelativeLayout view = findViewById(R.id.frame_top_camera);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        layoutParams.width = img_width;
        layoutParams.height = img_height;
        view.setLayoutParams(layoutParams);

        RelativeLayout view2 = findViewById(R.id.main_relative_layout);
        ViewGroup.LayoutParams layoutParams2 = view2.getLayoutParams();

        layoutParams2.width = img_width;
        layoutParams2.height = img_height;
        view2.setLayoutParams(layoutParams2);

         */







        //----------------------------------------------//
        RelativeLayout view3 = findViewById(R.id.main_preview);
        ViewGroup.LayoutParams layoutParams3 = view3.getLayoutParams();

        layoutParams3.width = img_width;
        layoutParams3.height = (width / 9) * 16;
        view3.setLayoutParams(layoutParams3);



        //txtDebug.setText("Height = " + img_height + "\nWidht = " + img_width);
        //Toast.makeText(MainActivity.this, "Height = " + img_height + "\nWidht = " + img_width, Toast.LENGTH_SHORT).show();


        //txt.setText("Height = " + height + "\nWidht = " + width);

    }






    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bCapture:
                if (statRecord == false) {
                    capturePhoto();
                } else if (statRecord == true) {
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
                if(state_serol.equals("4:3")){
                    ChangResolutionImage(16, 9);
                    state_serol = "16:9";
                    btnChangResol.setBackgroundResource(R.drawable.ic_resol_16_9);
                }else if(state_serol.equals("16:9")){
                    ChangResolutionImage(1, 1);
                    btnChangResol.setBackgroundResource(R.drawable.ic_resol_1_1);
                    state_serol = "1:1";

                }else if(state_serol.equals("1:1")){
                    ChangResolutionImage(4, 3);
                    btnChangResol.setBackgroundResource(R.drawable.ic_resol_4_3);
                    state_serol = "4:3";
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.bCapture:
                bCapture.setImageResource(R.drawable.ic_recording);
                recordVideo();
                break;
        }
        return statRecord = true;
    }






// ตั้งค่ากล้อง Camera X ---------------------------------------------------------------------------------------------
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }






    //ตรวจสอบการใช้งาน ว่าขณะนี้ใช้กล้องหน้า หรือกล้องหลังอยู่
    private void flipCamera() {
        if (lensFacing == CameraSelector.LENS_FACING_BACK){
            lensFacing = CameraSelector.LENS_FACING_FRONT;
        }
        else if (lensFacing == CameraSelector.LENS_FACING_FRONT){
            lensFacing = CameraSelector.LENS_FACING_BACK;
        }
        startCameraX(cameraProvider);
    }





    // เริ่มต้นการทำงานของ Camera X
    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        // Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), this);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, videoCapture);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Log.d("TAG", "analyze: got the frame at: " + image.getImageInfo().getTimestamp());
        image.close();
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
                                Toast.makeText(MainActivity.this, "Video has been saved successfully.", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                Toast.makeText(MainActivity.this, "Error saving video: " + message, Toast.LENGTH_SHORT).show();
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
            file_temp  = File.createTempFile("geek", ".jpg", null);

            imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(file_temp).build(), getExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(MainActivity.this, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();

                            //นำภาพในไฟล์ชั่วคราว ดึงมาใส่ใน Object BitMap
                            Bitmap myBitmap = BitmapFactory.decodeFile(file_temp.getAbsolutePath());

                            //Preview ใน iCon ของ Gallery
                            ImageButton imgBtn = findViewById(R.id.button_gallery);
                            imgBtn.setImageBitmap(myBitmap);


                            //เปิดหน้า Preview Activity
                            Intent myIntent = new Intent(MainActivity.this, PreviewActivity.class);
                            myIntent.putExtra("key", file_temp.getAbsolutePath()); //Optional parameters
                            myIntent.putExtra("resolution",state_serol);
                            MainActivity.this.startActivity(myIntent);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// END - ตั้งค่ากล้อง Camera X ---------------------------------------------------------------------------------------------

    public void setFocusView(double Y, double X, double height, double width){
        //removeView();
        double s = 1;
        int x, y, h, w;


        RelativeLayout rl = (RelativeLayout) findViewById(R.id.main_relative_layout);
        ViewTreeObserver vto = rl.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RelativeLayout mainPreview = (RelativeLayout) findViewById(R.id.main_preview);
                mainPreview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                img_width  = mainPreview.getMeasuredWidth();
                img_height = mainPreview.getMeasuredHeight();
                txtDebug.setText("Height = " + img_height + "\nWidht = " + img_width);
                //Toast.makeText(MainActivity.this, "Height = " + img_height + "\nWidht = " + img_width, Toast.LENGTH_SHORT).show();


                //txt.setText("Height = " + height + "\nWidht = " + width);
            }
        });

        //Toast.makeText(MainActivity.this, "X Height = " + img_height + "\nX Widht = " + img_width, Toast.LENGTH_SHORT).show();

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = Math.round((float)(height * 1440 * s));
        w = Math.round((float)(width * 1080 * s));

        x = Math.round((float)(X * 1440 * s)) - (h/2);
        y = Math.round((float)(Y * 1080 * s)) - (w/2);

        txtDebug2.setText("x = " + x + "\ny = " + y);


        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View focus_frame = inflater.inflate(R.layout.focus_frame,  null);



        //RelativeLayout focus_frame = (RelativeLayout) findViewById(R.layout.focus_frame);
        //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        //lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams params1 = new
                RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(y,x,0,0);
        rl.addView(focus_frame, params1);

        //rl.setLayoutParams(params1);
       //rl.addView(focus_frame);

       // Button myButton = new Button(this);
        //ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(300, 300);
        //lp.setMargins(left, top, 0, 0);
        //lp.setMargins();
       //l.addView(focus_frame, params1);

       // Toast.makeText(this, previewView.getMeasuredHeight() + ":" + previewView.getMeasuredHeight(), Toast.LENGTH_SHORT).show();

        //int widthX = fram_camera.getWidth();
        //int heightX = fram_camera.getHeight();

       // int widthX = fram_camera.getLayoutParams().height;
        //int heightX = fram_camera.getLayoutParams().width;

        //Toast.makeText(this, "width " + String.valueOf(widthX) + " height " + String.valueOf(heightX), Toast.LENGTH_LONG).show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}