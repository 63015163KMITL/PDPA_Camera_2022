package com.cekmitl.pdpacameracensor;

import androidx.appcompat.app.AppCompatActivity;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        Toast.makeText(this, "value = " + value, Toast.LENGTH_SHORT).show();

        File file = new File(value);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap rotated = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(),
                matrix, true);


        //#####################################################################################

        //#####################################################################################

        ImageView imgPreView = findViewById(R.id.ImagePreview);
        imgPreView.setImageBitmap(rotated);

        Button btnSave = findViewById(R.id.button_save_image);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageUri((PreviewActivity.this), myBitmap);
                //String savedImageURL = MediaStore.Images.Media.insertImage((PreviewActivity.this).getContentResolver(), myBitmap, "filename", null);
                //บันทึกรูปลง Grallery
                //Uri savedImageURI = Uri.parse(savedImageURL);
                //Toast.makeText(PreviewActivity.this, "savedImageURL = " + contentValues, Toast.LENGTH_SHORT).show();

                file.delete();
                finish();
            }
        });

        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                file.delete();
                finish();
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}