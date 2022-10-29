package com.cekmitl.pdpacameracensor;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    public SharedPreferences sharedPreferences;
    public Switch switch_grid_line, switch_location_tag, switch_mirror_font_camera, switch_preview_after_shutter, switch_volume_kaye_shutter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //ลบ Action Bar ออก
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        setContentView(R.layout.activity_setting2);

        switch_grid_line = findViewById(R.id.switch_grid_line);
        switch_location_tag = findViewById(R.id.switch_location_tag);
        switch_mirror_font_camera = findViewById(R.id.switch_mirroi_font_camera);
        switch_preview_after_shutter = findViewById(R.id.switch_preview_after_shutter);
        switch_volume_kaye_shutter = findViewById(R.id.switch_volume_kaye_shutter);


        SharedPreferences sh = getSharedPreferences("Setting", MODE_PRIVATE);

        switch_grid_line.setChecked(sh.getBoolean("switch_grid_line", true));
        switch_location_tag.setChecked(sh.getBoolean("switch_location_tag", true));
        switch_mirror_font_camera.setChecked(sh.getBoolean("switch_mirror_font_camera", true));
        switch_preview_after_shutter.setChecked(sh.getBoolean("switch_preview_after_shutter", true));
        switch_volume_kaye_shutter.setChecked(sh.getBoolean("switch_volume_kaye_shutter", true));

        switch_grid_line.setOnClickListener(this);
        switch_location_tag.setOnClickListener(this);
        switch_mirror_font_camera.setOnClickListener(this);
        switch_preview_after_shutter.setOnClickListener(this);
        switch_volume_kaye_shutter.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        sharedPreferences = getSharedPreferences("Setting",MODE_PRIVATE);
        SharedPreferences.Editor setting = sharedPreferences.edit();

        setting.putBoolean("switch_grid_line", switch_grid_line.isChecked());
        setting.putBoolean("switch_location_tag", switch_location_tag.isChecked());
        setting.putBoolean("switch_mirror_font_camera", switch_mirror_font_camera.isChecked());
        setting.putBoolean("switch_preview_after_shutter", switch_preview_after_shutter.isChecked());
        setting.putBoolean("switch_volume_kaye_shutter", switch_volume_kaye_shutter.isChecked());
        setting.commit();

    }
}