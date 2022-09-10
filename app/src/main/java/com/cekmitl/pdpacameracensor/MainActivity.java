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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, View.OnClickListener, View.OnLongClickListener {


    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;
    private ImageCapture imageCapture, imageCaptureX;
    private VideoCapture videoCapture;
    private ImageAnalysis imageAnalysis;
    private ImageButton btnGallery, bCapture, btnReverse, btnChangResol;
    private boolean statRecord, cam_reverse_state;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    private ImageView imgViewTest;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private RelativeLayout GetSizeImg;

    private TextView tvTimer, txtDebug, txtDebug2;
    private LinearLayout top_center;
    private RelativeLayout relativeLyout, fram_camera, head_layout;
    public int img_width = 0, img_height = 0;

    //Resolution Image
    private String state_serol = "4:3";

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1;
    int state_pdpd = 0;

    //DETECT FACE
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private Classifier detector;
    private Bitmap cropBitmap;
    private ImageView imageView;
    public static final int TF_OD_API_INPUT_SIZE = 640;
    private static final String TF_OD_API_MODEL_FILE = "Sbest-fp16.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().getDecorView().setSystemUiVisibility(  View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.new_preview);

        ChangResolutionImage(4, 3);

        txtDebug = (TextView) findViewById(R.id.text_debug);
        txtDebug2 = (TextView) findViewById(R.id.text_debug2);

        imgViewTest = (ImageView) findViewById(R.id.test_preview);

        //0.461358 0.338458 0.200941 0.198085
        //setFocusView(0.461358,0.338458,0.200941,0.198085);
        //0 0.356173 0.413889 0.0432099 0.0444444
        //0 0.167901, 0.411574, 0.0469136, 0.0490741

        //setFocusView(0.356173, 0.413889, 0.0432099, 0.0444444);
        //setFocusView(0.167901, 0.411574, 0.0469136, 0.0490741);

       // setFocusView(0.4, 0.4, 0.1, 0.1, 999);
       // setFocusView(0.6, 0.4, 0.1, 0.1, 777);

        top_center = (LinearLayout) findViewById(R.id.top_center);


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
        btnChangResol.setBackgroundResource(R.drawable.ic_resol_4_3);

        //VIDEO
        ImageView imgV = findViewById(R.id.icon_recording);                 // สัญลักษณ์ บันทึกวีดิโอ
        //Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        //imgV.startAnimation(startAnimation);
        tvTimer = findViewById(R.id.textViewTimer);
        //END - ส่วนสร้าง Object ต่าง ๆ --------------------------------------------------------------------------------------

        //Canvas ---------------------------------------------------------------------------------------------------------
        relativeLyout = findViewById(R.id.main_relative_layout);
        head_layout = findViewById(R.id.main_relative_layout);
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


        Thread t2 = new Thread(new Runnable() {
            public void run() {
                //final List<Classifier.Recognition> results;
                //cropBitmap = BitmapFactory.decodeResource(MainActivity.this.getResources(),R.drawable.test_img);

                handler.postDelayed(runnable = new Runnable() {
                    final int[] i = {0};

                    public void run() {
                        handler.postDelayed(runnable, delay * 1);

                        i[0]++;
                        imgViewTest.post(new Runnable() {
                            @Override
                            public void run() {
                                int n = 128;

                                long startTime = System.currentTimeMillis();

                                handleResult();
                                long endTime = System.currentTimeMillis();
                                //Toast.makeText(MainActivity.this, "Time = " + (endTime - startTime) + " ms", LENGTH_SHORT).show();

                                txtDebug.setText("COUNTER = " + i[0] + "\nTime = " + (endTime - startTime) + " ms");
                                //final Bitmap bitmap = previewView.getBitmap();
                                //cropBitmap =
                                //image.close();

                                //if(bitmap == null){
                               //     return;
                                //}else {
                                //    txtDebug2.setText(bitmap.toString());
                                    //cropBitmap = bitmap;
                                   // bitmap =
                                   // handleResult(getResizedBitmap(bitmap, 256, 256));


                               // }


                            }
                        });
                    }
                }, delay * 1);

            }
        });

        t2.start();




        //initBox();
        //ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();



    }


    public static void slideView(View view, int currentHeight, int newHeight) {
        ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(500);

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

    public static void slideView2(View view, int currentHeight, int newHeight, int currentWidth, int newWidth) {

        ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(600);
        ValueAnimator slideAnimator2 = ValueAnimator.ofInt(currentWidth, newWidth).setDuration(600);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */
        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
        });

        slideAnimator2.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().width = value.intValue();
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


        //txtDebug.setText("Height = " + ((width / w) * h) + "\nWidht = " + width);
        //Toast.makeText(MainActivity.this, "Height = " + img_height + "\nWidht = " + img_width, Toast.LENGTH_SHORT).show();


        // txtDebug2.setText("Height = " + (width / 9) * 16 + "\nWidht = " + img_width);

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
                if (state_serol.equals("4:3")) {
                    ChangResolutionImage(16, 9);
                    state_serol = "16:9";
                    btnChangResol.setBackgroundResource(R.drawable.ic_resol_16_9);
                } else if (state_serol.equals("16:9")) {
                    ChangResolutionImage(1, 1);
                    btnChangResol.setBackgroundResource(R.drawable.ic_resol_1_1);
                    state_serol = "1:1";

                } else if (state_serol.equals("1:1")) {
                    ChangResolutionImage(4, 3);
                    btnChangResol.setBackgroundResource(R.drawable.ic_resol_4_3);
                    state_serol = "4:3";
                }
                break;
            case R.id.top_center:
                if(state_pdpd == 0){
                    slideView2(top_center, top_center.getLayoutParams().height, 330,top_center.getLayoutParams().width, 1920);
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
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetResolution(new Size(1080, 1440))
                .build();

        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(60)
                .build();

        // Image analysis use case
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        //imageAnalysis.setAnalyzer(getExecutor(), this);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview,imageCapture, videoCapture);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Log.d("TAG", "analyze: got the frame at: " + image.getImageInfo().getTimestamp());

        int n = 128;
        final Bitmap bitmap = previewView.getBitmap();
        Bitmap bm = getResizedBitmap(bitmap, 3*n, 4*n);

        image.close();
        if(bitmap == null){
            return;
        }else {
            imgViewTest.setImageBitmap(bm);
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
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
                            makeText(MainActivity.this, "Photo has been saved successfully.", LENGTH_SHORT).show();

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

    public void setFocusView(double X, double Y, double width, double height, int str , float xPos , float yPos) {
        //removeView();
        int x, y, h, w;


       //RelativeLayout frameFocusLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
        RelativeLayout frameFocusLayout = (RelativeLayout) findViewById(R.id.fram_focus_layout);


        ViewTreeObserver vto = frameFocusLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RelativeLayout mainPreview = (RelativeLayout) findViewById(R.id.main_preview);
                mainPreview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                img_width = mainPreview.getMeasuredWidth();
                img_height = mainPreview.getMeasuredHeight();
                //txtDebug.setText("Height = " + img_height + "\nWidht = " + img_width);
                //Toast.makeText(MainActivity.this, "Height = " + img_height + "\nWidht = " + img_width, Toast.LENGTH_SHORT).show();


                //txt.setText("Height = " + height + "\nWidht = " + width);
            }
        });

        //Toast.makeText(MainActivity.this, "X Height = " + img_height + "\nX Widht = " + img_width, Toast.LENGTH_SHORT).show();

        Display display = getWindowManager().getDefaultDisplay();
        int width2 = display.getWidth();
        int height2 = 0;

        if (state_serol.equals("4:3")) {
            height2 = 1440;
        } else if (state_serol.equals("16:9")) {
            height2 = 1920;
        } else if (state_serol.equals("1:1")) {
            height2 = 1080;
        }


        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = Math.round((float) ((2*(height-xPos)) * height2));
        w = Math.round((float) ((2*(width-yPos)) * width2));
        Log.d("x", String.valueOf(2*(height-yPos)));
        x = Math.round((float) (X * height2));
        y = Math.round((float) (Y * width2));

        //x = Math.round((float) (X * height2 * s)) - (h / 2);
        //y = Math.round((float) (Y * width2 * s)) - (w / 2);

        //txtDebug2.setText("x = " + x + "\ny = " + y);


        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View focus_frame = inflater.inflate(R.layout.focus_frame, null);
        focus_frame.setId(str);

        focus_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                makeText(MainActivity.this, "CLICK = " + focus_frame.getId(), LENGTH_SHORT).show();
            }
        });

        //RelativeLayout focus_frame = (RelativeLayout) findViewById(R.layout.focus_frame);
        //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        //lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(y, x, 0, 0);


        //params1.height = (int) Math.round(height * 1440) ;
        //params1.width = (int) Math.round(width * 1080);
        //params1.setMargins( (int) Math.round((Y * 1440) + params1.height),
        //                    (int) Math.round(X * 1080), 0, 0);

        frameFocusLayout.addView(focus_frame, params1);

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

    // การถ่ายรูป
    private void capturePhoto2() {

        //สร้างตำแหน่งเก็บไฟล์ภาพชั่วคราว
        File file_temp;
        try {
            file_temp = File.createTempFile("geek", ".jpg", null);

            imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(file_temp).build(), getExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            //นำภาพในไฟล์ชั่วคราว ดึงมาใส่ใน Object BitMap
                            Bitmap myBitmap = BitmapFactory.decodeFile(file_temp.getAbsolutePath());
                            imgViewTest.setImageBitmap(myBitmap);

                           // Toast.makeText(MainActivity.this, "A", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                           // Toast.makeText(MainActivity.this, "B", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }







    //DETECT FACE FUNCTION
    // เปิดไฟล์ภาพ
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            assert data != null;
            Uri uri = data.getData();
            try {

                int w = 512, h = 512;

                Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
               // bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap

               // TextView textView = findViewById(R.id.textView);

                cropBitmap = Utils.processBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri), TF_OD_API_INPUT_SIZE);

                String str = "" + Utils.processBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri), TF_OD_API_INPUT_SIZE);;
                //Toast.makeText(this, "cropBitmap : " + str, Toast.LENGTH_SHORT).show();
                txtDebug2.setText("cropBitmap : " + cropBitmap);
                //cropBitmap = bmp;

                imageView.setImageBitmap(cropBitmap);
                handleResult(cropBitmap);

            }catch (IOException e){
                e.printStackTrace();
                finish();
            }
        }
    }
    //จัดการกับ Label คำตอบ
    private void handleResult(Bitmap bitmap) {
//        startTime = System.currentTimeMillis();
        int w = 512, h = 512;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        // Canvas canvas = new Canvas(bmp);

        final Bitmap bm = previewView.getBitmap();
        if (bm != null) {
            // bitmap =
            List<Classifier.Recognition> results = detector.recognizeImage(bitmap); //ส่งภาพไป คืนคำตอบกลับมาในรูปแบบ List
            //handleResult(getResizedBitmap(bitmap, 256, 256));


//        long endtime = System.currentTimeMillis() - startTime;
//        Log.d("time", "time " + String.valueOf(endtime));
            final Canvas canvas = new Canvas(bitmap);
            final Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();

                // ตำแหน่งที่ได้อยู่ในช่วง [0,1] ต้องนำไปคูณกับขนาดของรูปก่อน
                location.left = location.left * 640;
                location.top = location.top * 640;
                location.right = location.right * 640;
                location.bottom = location.bottom * 640;

                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    if (result.getDetectedClass() == 0) {
                        paint.setColor(Color.MAGENTA);
                        canvas.drawRect(location, paint);
                    } else if (result.getDetectedClass() == 1) {
                        paint.setColor(Color.BLUE);
                        canvas.drawRect(location, paint);
                    } else {
                        paint.setColor(Color.RED);
                        canvas.drawRect(location, paint);
                    }
                }
            }
        }
    }
//        imageView.setImageBitmap(bitmap);



        private void handleResult() {
//        startTime = System.currentTimeMillis();
            int w = 512, h = 512;
            long startTime = System.currentTimeMillis();


            long endTime = System.currentTimeMillis();



            //Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap bmp = previewView.getBitmap();//Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap

            //bmp = getResizedBitmap(bmp, 256,256);
            // Canvas canvas = new Canvas(bmp);

                if(bmp == null){
                    makeText(this, "ERROR", LENGTH_SHORT).show();
                }else {
                    List<Classifier.Recognition>results = detector.recognizeImage(getResizedBitmap(bmp,640,640));
                    imgViewTest.setImageBitmap(getResizedBitmap(bmp,256,256));
                    txtDebug2.setText(results + "");

                    clearFocus();
                    for (final Classifier.Recognition result : results) {

                        final RectF location = result.getLocation();

                        // ตำแหน่งที่ได้อยู่ในช่วง [0,1] ต้องนำไปคูณกับขนาดของรูปก่อน
                        //location.left = location.left * 640;
                        //location.top = location.top * 640;
                       // location.right = location.right * 640;
                       // location.bottom = location.bottom * 640;


                        //                           Y - X - Height - Width
                        if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                            if (result.getDetectedClass() == 0){
                                setFocusView(location.left, location.top, location.right , location.bottom, 777,result.getX(),result.getY());
                            }

                        }
                }






            }

                //ส่งภาพไป คืนคำตอบกลับมาในรูปแบบ List
                //handleResult(getResizedBitmap(bitmap, 256, 256));


//        long endtime = System.currentTimeMillis() - startTime;
//        Log.d("time", "time " + String.valueOf(endtime));

            }

            public void clearFocus(){
                RelativeLayout frameFocusLayout = (RelativeLayout) findViewById(R.id.fram_focus_layout);

                if (null != frameFocusLayout && frameFocusLayout.getChildCount() > 0) {
                    try {
                        frameFocusLayout.removeViews (0, frameFocusLayout.getChildCount());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    }
