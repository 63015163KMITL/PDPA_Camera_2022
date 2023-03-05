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
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class FaceRecognitionCamera extends AppCompatActivity implements ImageAnalysis.Analyzer{

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    public PreviewView previewView;
    private ImageCapture imageCapture;

    ArrayList<Bitmap> b = new ArrayList<Bitmap>(5);

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
                SelectFace();
            }
        });
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
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
            face_crop_bitmap.clear();
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

    public void SelectFace(){


//        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.test);

//        icon = BitmapEditor.getResizedBitmap(icon, 50, 50);
        //b.add(icon);
        face_crop_bitmap.add(BitmapEditor.rotateBitmap(bb,-90));
        Intent intent = new Intent(getApplicationContext(), AddNewFaceActivity.class);
        intent.putExtra("key", "1");
        //intent.putExtra("bitmap", b.get(0));
        intent.putExtra("bitmap", face_crop_bitmap);
        startActivity(intent);

    }
}