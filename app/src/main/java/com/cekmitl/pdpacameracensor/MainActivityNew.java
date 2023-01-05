package com.cekmitl.pdpacameracensor;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivityNew extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout camera_button, setting_button, face_recog_button, gallery_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Remove Action Bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();


        camera_button = findViewById(R.id.camera_button);
        setting_button = findViewById(R.id.setting_button);
        face_recog_button = findViewById(R.id.face_recog_button);
        gallery_button= findViewById(R.id.gallery_button);

        camera_button.setOnClickListener(this);
        setting_button.setOnClickListener(this);
        face_recog_button.setOnClickListener(this);
        gallery_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_button :
                Intent i = new Intent(MainActivityNew.this, MainActivity.class);
                MainActivityNew.this.startActivity(i);
                break;
            case R.id.setting_button :
                Intent intent_setting = new Intent(MainActivityNew.this, SettingActivity.class);
                MainActivityNew.this.startActivity(intent_setting);
                break;
            case R.id.face_recog_button :
                Intent intent_face_recog = new Intent(MainActivityNew.this, SettingActivity.class);
                MainActivityNew.this.startActivity(intent_face_recog);
                break;
            case R.id.gallery_button :
                this.startActivity(new Intent(MainActivityNew.this, GalleryActivity.class));
                this.overridePendingTransition(0, 0);
                break;
        }
    }
}