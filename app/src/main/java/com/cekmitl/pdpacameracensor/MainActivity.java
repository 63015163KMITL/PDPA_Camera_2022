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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
    private ImageButton btnGallery, bCapture, btnReverse;
    private boolean statRecord, cam_reverse_state;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private RelativeLayout GetSizeImg;

    private TextView tvTimer, txtDebug, txtDebug2;
    private RelativeLayout relativeLyout, fram_camera;
    public int img_width, img_height;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        //int width = displayMetrics.widthPixels;

        txtDebug = (TextView) findViewById(R.id.text_debug);
        txtDebug2 = (TextView) findViewById(R.id.text_debug2);

        double s = 4.21875;

       //ImageView test_img = findViewById(R.id.test_img);
        //test_img.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
        //ViewGroup.LayoutParams params = test_img.getLayoutParams();
        //params.width = Math.round((float)(256 * s));
        //params.height = Math.round((float)(256 * s));
        //test_img.setLayoutParams(params);

        //0.818359375 0.33203125 0.3359375 0.30859375
        //Toast.makeText(this, "X : " + x + " Y : " + y + " W : " + w + " H : " + h, Toast.LENGTH_SHORT).show();
       // float x = 2140/1280;

        //int n = Integer.parseInt(Math.round(300) + "");
        //Toast.makeText(this, "n = " + n, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "n 2 = " + 300*1.828125, Toast.LENGTH_SHORT).show();

       //setFocusView(0.158203125,0.28125,0.33203125,0.28515625);
       //setFocusView(0.818359375,0.33203125,0.3359375,0.30859375);

        //setFocusView(0.208359375,1.3,0.3359375,0.30859375);


        //0.461358 0.338458 0.200941 0.198085
        //setFocusView(0.461358,0.338458,0.200941,0.198085);
        //0 0.356173 0.413889 0.0432099 0.0444444
        //0 0.167901, 0.411574, 0.0469136, 0.0490741

        setFocusView(0.356173, 0.413889, 0.0432099, 0.0444444);
        setFocusView(0.167901, 0.411574, 0.0469136, 0.0490741);




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
/*
                            int x, y, h, w;
                            double s = 1;

                            double X = 0.461358;
                            double Y = 0.338458;
                            double height = 0.200941;
                            double width = 0.198085;


                            //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
                            h = Math.round((float)(height * img_width * s));
                            w = Math.round((float)(width * img_width * s));

                            x = Math.round((float)(X * img_height * s)) - (w/2);
                            y = Math.round((float)(Y * img_width * s)) - (h/2);

                            Bitmap newBitmap = Bitmap.createBitmap(myBitmap,x,y,w,h);

 */
                            //getImageUri((MainActivity.this), newBitmap);


                            //Preview ใน iCon ของ Gallery
                            ImageButton imgBtn = findViewById(R.id.button_gallery);
                            imgBtn.setImageBitmap(myBitmap);

                            //เปิดหน้า Preview Activity
                            Intent myIntent = new Intent(MainActivity.this, PreviewActivity.class);
                            myIntent.putExtra("key", file_temp.getAbsolutePath()); //Optional parameters
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
                ImageView img = (ImageView) findViewById(R.id.test_img);
                img.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                img_width  = img.getMeasuredWidth();
                img_height = img.getMeasuredHeight();
                txtDebug.setText("Height = " + img_height + "\nWidht = " + img_width);
                Toast.makeText(MainActivity.this, "Height = " + img_height + "\nWidht = " + img_width, Toast.LENGTH_SHORT).show();

                //txt.setText("Height = " + height + "\nWidht = " + width);



            }
        });

        Toast.makeText(MainActivity.this, "X Height = " + img_height + "\nX Widht = " + img_width, Toast.LENGTH_SHORT).show();

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