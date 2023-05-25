package com.cekmitl.pdpacameracensor;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.ui.facerecognition.FaceRecognitionFragment;
import com.cekmitl.pdpacameracensor.ui.gallery.GalleryFragment;
import com.cekmitl.pdpacameracensor.ui.home.HomeFragment;
import com.cekmitl.pdpacameracensor.ui.setting.SettingFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout home_button;
    private LinearLayout setting_button;
    private LinearLayout face_recog_button;
    private LinearLayout gallery_button;

    PersonDatabase db = null;

    LinearLayout camera_button;

    private ImageView icon_home;
    private ImageView icon_gallery;
    private ImageView icon_face_recog;
    private ImageView icon_setting;
    private TextView title_home, title_gallery, title_face_recog, title_setting;
    private boolean firstTime = false;
    public GalleryFragment galleryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.main_color));
        View decorView = getWindow().getDecorView(); //set status background black
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (db == null){
                    try {
                        db = new PersonDatabase(-1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        finish = false;
        finish2 = false;
        finish3 = false;
        finish4 = false;

        getGallery();
        while (!finish && !finish2 && !finish3 && !finish4){

        }

        //Fragment frame
        FragmentManager fm4 = getSupportFragmentManager();
        FragmentTransaction ft4 = fm4.beginTransaction();
        HomeFragment hFrag4 = new HomeFragment(this);
        ft4.replace(R.id.nav_host_fragment_activity_main, hFrag4);
        ft4.addToBackStack(null);
        ft4.commit();

        camera_button = findViewById(R.id.camera_button);
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
    protected void onResume() {
        super.onResume();
        if (firstTime){
            Toast.makeText(mainActivity, "re", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.camera_button :
                Intent i = new Intent(MainActivity.this, MainCameraActivity.class);
                MainActivity.this.startActivity(i);
                break;
            case R.id.home_button :

                FragmentManager fm4 = getSupportFragmentManager();
                FragmentTransaction ft4 = fm4.beginTransaction();
                HomeFragment hFrag4 = new HomeFragment(this);
                ft4.replace(R.id.nav_host_fragment_activity_main, hFrag4);
                ft4.addToBackStack(null);
                ft4.commit();

                changColorNavebar(R.id.home_button);
                break;
            case R.id.setting_button :
                changColorNavebar(R.id.setting_button);

                FragmentManager fm5 = getSupportFragmentManager();
                FragmentTransaction ft5 = fm5.beginTransaction();
                ft5.replace(R.id.nav_host_fragment_activity_main, new SettingFragment());
                ft5.addToBackStack(null);
                ft5.commit();
                break;
            case R.id.gallery_button :
                FragmentManager fm2 = getSupportFragmentManager();
                FragmentTransaction ft2 = fm2.beginTransaction();
                GalleryFragment gFrag = new GalleryFragment(this);
                ft2.replace(R.id.nav_host_fragment_activity_main, gFrag);
                ft2.addToBackStack(null);
                ft2.commit();

                changColorNavebar(R.id.gallery_button);
                break;
            case R.id.face_recog_button :

                FragmentManager fm3 = getSupportFragmentManager();
                FragmentTransaction ft3 = fm3.beginTransaction();
                ft3.replace(R.id.nav_host_fragment_activity_main, new FaceRecognitionFragment());
                ft3.addToBackStack(null);
                ft3.commit();
                changColorNavebar(R.id.face_recog_button);
                break;
        }
    }

    public void changFragment(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        GalleryFragment gFrag = new GalleryFragment(this);
        ft.replace(R.id.nav_host_fragment_activity_main, gFrag);
        ft.addToBackStack(null);
        ft.commit();
        changColorNavebar(R.id.gallery_button);
    }

    public Bitmap[] thumbnails = null;
    public String[] arrPath= null;
    public int[] typeMedia= null;
    public Cursor imagecursor1,imagecursor2,imagecursor3,imagecursor4;
    public MainActivity mainActivity = this;
    public boolean finish = false;
    public boolean finish2 = false;
    public boolean finish3 = false;
    public boolean finish4 = false;

    int number_saved = 0;

    void getGallery(){
        String[] columns = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE,
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
        Uri queryUri = MediaStore.Files.getContentUri("external");
        imagecursor1 = this.managedQuery(queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        imagecursor2 = this.managedQuery(queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        imagecursor3 = this.managedQuery(queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        imagecursor4 = this.managedQuery(queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        int count = imagecursor1.getCount();
        int image_column_index = imagecursor1.getColumnIndex(MediaStore.Files.FileColumns._ID);
        this.thumbnails = new Bitmap[count];
        this.arrPath = new String[count];
        this.typeMedia = new int[count];
        this.number_saved = 10;
        int p2 = (int) Math.round(count * 0.25);
        int p3 = (int) Math.round(count * 0.5);
        int p4 = (int) Math.round(count * 0.75);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < p2; i++) {
                    imagecursor1.moveToPosition(i);
                    int id = imagecursor1.getInt(image_column_index);
                    int dataColumnIndex = imagecursor1.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 4;
                    bmOptions.inPurgeable = true;
                    int type = imagecursor1.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                    int t = imagecursor1.getInt(type);

                    if(t == 1)
                        thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Images.Thumbnails.MINI_KIND, bmOptions);
                    else if(t == 3)
                        thumbnails[i] = MediaStore.Video.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Video.Thumbnails.MINI_KIND, bmOptions);

                    arrPath[i]= imagecursor1.getString(dataColumnIndex);
                    typeMedia[i] = imagecursor1.getInt(type);
                }

                finish = true;

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = p2; i < p3; i++) {
                    imagecursor2.moveToPosition(i);
                    int id = imagecursor2.getInt(image_column_index);
                    int dataColumnIndex = imagecursor2.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 4;
                    bmOptions.inPurgeable = true;
                    int type = imagecursor2.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                    int t = imagecursor2.getInt(type);

                    if(t == 1)
                        thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Images.Thumbnails.MINI_KIND, bmOptions);
                    else if(t == 3)
                        thumbnails[i] = MediaStore.Video.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Video.Thumbnails.MINI_KIND, bmOptions);

                    arrPath[i]= imagecursor2.getString(dataColumnIndex);
                    typeMedia[i] = imagecursor2.getInt(type);
                }

                finish2 = true;

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = p3; i < p4; i++) {
                    imagecursor3.moveToPosition(i);
                    int id = imagecursor3.getInt(image_column_index);
                    int dataColumnIndex = imagecursor3.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 4;
                    bmOptions.inPurgeable = true;
                    int type = imagecursor3.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                    int t = imagecursor3.getInt(type);

                    if(t == 1)
                        thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Images.Thumbnails.MINI_KIND, bmOptions);
                    else if(t == 3)
                        thumbnails[i] = MediaStore.Video.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Video.Thumbnails.MINI_KIND, bmOptions);

                    arrPath[i]= imagecursor3.getString(dataColumnIndex);
                    typeMedia[i] = imagecursor3.getInt(type);
                }

                finish3 = true;

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = p4; i < count; i++) {
                    imagecursor4.moveToPosition(i);
                    int id = imagecursor4.getInt(image_column_index);
                    int dataColumnIndex = imagecursor4.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 4;
                    bmOptions.inPurgeable = true;
                    int type = imagecursor4.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                    int t = imagecursor4.getInt(type);

                    if(t == 1)
                        thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Images.Thumbnails.MINI_KIND, bmOptions);
                    else if(t == 3)
                        thumbnails[i] = MediaStore.Video.Thumbnails.getThumbnail(
                                mainActivity.getContentResolver(), id,
                                MediaStore.Video.Thumbnails.MINI_KIND, bmOptions);

                    arrPath[i]= imagecursor4.getString(dataColumnIndex);
                    typeMedia[i] = imagecursor4.getInt(type);
                }

                finish4 = true;

            }
        }).start();


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
            transaction_setting.replace(R.id.nav_host_fragment_activity_main, fragment);
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