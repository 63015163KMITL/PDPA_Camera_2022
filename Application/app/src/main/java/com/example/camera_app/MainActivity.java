package com.example.camera_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private Classifier detector;
    private Bitmap cropBitmap;
    private ImageView imageView;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";
    private TextView txt ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button selectButton = findViewById(R.id.cameraButton);
        imageView = findViewById(R.id.imageView);
        txt = findViewById(R.id.textView);
        try {
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        selectButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,100);
        });

    }
    // เปิดไฟล์ภาพ
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            assert data != null;
            Uri uri = data.getData();
            try {
                cropBitmap = Utils.processBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri), TF_OD_API_INPUT_SIZE);
                imageView.setImageBitmap(this.cropBitmap);
                handleResult(cropBitmap);
            }catch (IOException e){
                e.printStackTrace();
                finish();
            }
        }
    }
        long startTime = 0;
    //จัดการกับ Label คำตอบ
    private void handleResult(Bitmap bitmap) {
        startTime = System.currentTimeMillis();
        final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap); //ส่งภาพไป คืนคำตอบกลับมาในรูปแบบ List
        long endtime = System.currentTimeMillis() - startTime;
        txt.setText(String.valueOf(endtime));
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();

            // ตำแหน่งที่ได้อยู่ในช่วง [0,1] ต้องนำไปคูณกับขนาดของรูปก่อน
            location.left = location.left * 320;
            location.top = location.top * 320;
            location.right = location.right * 320;
            location.bottom = location.bottom * 320;

            if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
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

//        imageView.setImageBitmap(bitmap);
    }

}