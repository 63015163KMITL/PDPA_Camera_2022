package com.example.camera_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.4f;
    private Bitmap img;
    private Classifier detector;
    private Bitmap cropBitmap;
    private Button selectButton, detectButton;
    private ImageView imageView;
    public static final int TF_OD_API_INPUT_SIZE = 640;
    private static final String TF_OD_API_MODEL_FILE = "Sbest-fp16.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectButton = findViewById(R.id.cameraButton);
        detectButton = findViewById(R.id.detectButton);
        imageView = findViewById(R.id.imageView);

        try {
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,100);
            }
        });

        detectButton.setOnClickListener(v -> {Handler handler = new Handler();
            new Thread(() -> {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(cropBitmap);
                    }
                });
            }).start();
        });
    }
    // เปิดไฟล์ภาพ
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                cropBitmap = Utils.processBitmap(img, TF_OD_API_INPUT_SIZE);
                imageView.setImageBitmap(this.cropBitmap);
            }catch (IOException e){
                e.printStackTrace();
                finish();
            }
        }
    }

    //จัดการกับ Label คำตอบ
    private void handleResult(Bitmap bitmap) {
        final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap); //ส่งภาพไป คืนคำตอบกลับมาในรูปแบบ List
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                if (result.getDetectedClass() == 0){
                    paint.setColor(Color.MAGENTA);
                    canvas.drawRect(location, paint);
                }else if (result.getDetectedClass() == 1){
                    paint.setColor(Color.BLUE);
                    canvas.drawRect(location, paint);
                }else{
                    paint.setColor(Color.RED);
                    canvas.drawRect(location, paint);
                }
            }
        }
        imageView.setImageBitmap(bitmap);
    }
}
