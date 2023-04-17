package com.cekmitl.pdpacameracensor;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cekmitl.pdpacameracensor.databinding.ActivityMain2Binding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout home_button;
    private LinearLayout setting_button;
    private LinearLayout face_recog_button;
    private LinearLayout gallery_button;

    private ImageView icon_home;
    private ImageView icon_gallery;
    private ImageView icon_face_recog;
    private ImageView icon_setting;
    private TextView title_home, title_gallery, title_face_recog, title_setting;

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.main_color));
        View decorView = getWindow().getDecorView(); //set status background black
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        com.cekmitl.pdpacameracensor.databinding.ActivityMain2Binding binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_facerecognition).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main2);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        LinearLayout camera_button = findViewById(R.id.camera_button);
        home_button = findViewById(R.id.home_button);
        setting_button = findViewById(R.id.setting_button);
        face_recog_button = findViewById(R.id.face_recog_button);
        gallery_button= findViewById(R.id.gallery_button);

        icon_home = findViewById(R.id.icon_home);
        icon_gallery = findViewById(R.id.icon_gallery);
        icon_face_recog = findViewById(R.id.icon_face_recog);
        icon_setting = findViewById(R.id.icon_setting);

        title_home = findViewById(R.id.title_home);
        title_gallery = findViewById(R.id.title_gallery);
        title_face_recog = findViewById(R.id.title_face_recog);
        title_setting = findViewById(R.id.title_setting);

        camera_button.setOnClickListener(this);
        home_button.setOnClickListener(this);
        gallery_button.setOnClickListener(this);
        face_recog_button.setOnClickListener(this);
        setting_button.setOnClickListener(this);
        face_recog_button.setOnClickListener(this);


        ImageView splash = findViewById(R.id.splash);
        slideView2(splash, 2000, camera_button.getLayoutParams().height, 2000, camera_button.getLayoutParams().height);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.camera_button :
                Intent i = new Intent(MainActivity.this, MainCameraActivity.class);
                MainActivity.this.startActivity(i);
                break;
            case R.id.home_button :
                //replaceFragment(new HomeFragment());
                navController.navigate(R.id.navigation_home);
                changColorNavebar(R.id.home_button);
                break;
            case R.id.setting_button :

                Intent myIntent = new Intent(MainActivity.this, SettingActivity.class);
                MainActivity.this.startActivity(myIntent);
                //replaceFragment(new NotificationsFragment());
//                navController.navigate(R.id.navigation_setting);
//                changColorNavebar(R.id.setting_button);
                break;
            case R.id.face_recog_button :
                //replaceFragment(new DashboardFragment());
                navController.navigate(R.id.navigation_facerecognition);
                changColorNavebar(R.id.face_recog_button);
                break;
            case R.id.gallery_button :

//                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//                pickIntent.setType("image/* video/*");
//                startActivityForResult(pickIntent, 1);

//                Intent _myIntent = new Intent(MainActivity.this, GalleryActivity.class);
//                MainActivity.this.startActivity(_myIntent);
                //replaceFragment(new HomeFragment());
                navController.navigate(R.id.navigation_gallery);
                changColorNavebar(R.id.gallery_button);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri.toString().contains("image")) {
                //handle image
                Toast.makeText(this, "IMAGE", Toast.LENGTH_SHORT).show();
            } else if (selectedMediaUri.toString().contains("video")) {
                //handle video
                Toast.makeText(this, "VIDEO", Toast.LENGTH_SHORT).show();
            }
        }
    }

        public void replaceFragment (Fragment fragment){
            FragmentTransaction transaction_setting = getSupportFragmentManager().beginTransaction();
            //transaction_setting.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_left);
            transaction_setting.replace(R.id.nav_host_fragment_activity_main2, fragment);
            transaction_setting.commit();
        }

        public void changColorNavebar ( int id){
            icon_home.setImageResource(R.drawable.ic_home);
            icon_gallery.setImageResource(R.drawable.ic_gallery_gray);
            icon_face_recog.setImageResource(R.drawable.ic_facerecog_gray);
            icon_setting.setImageResource(R.drawable.ic_setting);

            title_home.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.font_color_gray));
            title_gallery.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.font_color_gray));
            title_face_recog.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.font_color_gray));
            title_setting.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.font_color_gray));

            home_button.setClickable(true);
            gallery_button.setClickable(true);
            face_recog_button.setClickable(true);
            setting_button.setClickable(true);
            face_recog_button.setClickable(true);

            switch (id) {
                case R.id.home_button:
                    icon_home.setImageResource(R.drawable.ic_home_focus);
                    title_home.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                    home_button.setClickable(false);
                    break;
                case R.id.gallery_button:
                    //         Intent myIntent = new Intent(MainActivity.this, TestVideo.class);
//                    Intent myIntent = new Intent(MainActivity.this, GalleryActivity.class);
//                MainActivity.this.startActivity(myIntent);
                    icon_gallery.setImageResource(R.drawable.ic_gallery_focus);
                    title_gallery.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                    gallery_button.setClickable(false);
                    break;
                case R.id.face_recog_button:
                    icon_face_recog.setImageResource(R.drawable.ic_facerecog_focus);
                    title_face_recog.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                    face_recog_button.setClickable(false);
                    break;
                case R.id.setting_button:
                    icon_setting.setImageResource(R.drawable.ic_setting_focus);
                    title_setting.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                    setting_button.setClickable(false);
                    break;
            }
        }

        public static void slideView2 (View view,int currentHeight, int newHeight, int currentWidth,
        int newWidth){
            ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(0);
            ValueAnimator slideAnimator2 = ValueAnimator.ofInt(currentWidth, newWidth).setDuration(0);

            slideAnimator.addUpdateListener(animation1 -> {
                view.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
                view.requestLayout();
            });

            slideAnimator2.addUpdateListener(animation1 -> {
                view.getLayoutParams().width = (Integer) animation1.getAnimatedValue();
                view.requestLayout();
            });
            AnimatorSet animationSet = new AnimatorSet();
            animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animationSet.play(slideAnimator);
            animationSet.start();

            AnimatorSet animationSet2 = new AnimatorSet();
            animationSet2.setInterpolator(new AccelerateDecelerateInterpolator());
            animationSet2.play(slideAnimator2);
            animationSet2.start();
        }

        void getSetting () {
            SharedPreferences setting = getSharedPreferences("Setting", MODE_PRIVATE);
            SharedPreferences.Editor edit = setting.edit();

            edit.putFloat("OBJ_DETECT_CONFIDENT", 0.3f);
            edit.apply();
        }

    }