package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.cekmitl.pdpacameracensor.MainActivity.slideView2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    public LinearLayout layout_face_detect, layout_blur_radius, layout_stricker_option, layout_paint_option, button_blur_layout, button_stricker_layout, button_paint_layout;
    public ImageButton button_hide_face_detect, button_face_detect, button_blur, button_stricker, button_paint;
    public RelativeLayout option_layout, frameFocusLayout, FrameImagePreview, button_bar, HeadLayout, HeadLayout2;
    public LinearLayout listView, bottom_layout, menu_bar;

    //RelativLayout Button Menu Bar
    public RelativeLayout button_edit, button_info, button_delete;

    //RelativeLayout Touch ImagePreview
    public RelativeLayout Touch_ImagePreview;

    //RelativeLayout Head Menu Bar
    public ImageButton button_back;

    //DETECT FACE
    private Classifier detector;

    //DETECT FACE
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private Bitmap largeIcon;
    private ImageView imageView, ImagePreview;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    public Bitmap tempBitmap = null;
    public List<Classifier.Recognition> results = null;

    public String state_serol = "16:9";

    public int old_height_header_layout = 0;
    public int old_width_header_layout = 0;
    public int old_height_bottom_layout = 0;
    public int old_width_bottom_layout = 0;


    public int global_img_width, getGlobal_img_height;
    public ArrayList<Bitmap> bmp_images = new ArrayList<Bitmap>();

    ArrayList<Integer> arrlist = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //ลบ Action Bar ออก
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        setContentView(R.layout.activity_preview);

        layout_face_detect = (LinearLayout) findViewById(R.id.layout_face_detect);

        layout_blur_radius = (LinearLayout) findViewById(R.id.layout_blur_radius);
        layout_stricker_option = (LinearLayout) findViewById(R.id.layout_stricker_option);
        layout_paint_option = (LinearLayout) findViewById(R.id.layout_paint_option);
        option_layout = (RelativeLayout) findViewById(R.id.option_layout);

        button_blur_layout = (LinearLayout) findViewById(R.id.button_blur_layout);
        button_stricker_layout = (LinearLayout) findViewById(R.id.button_stricker_layout);
        button_paint_layout = (LinearLayout) findViewById(R.id.button_paint_layout);

        button_blur = (ImageButton) findViewById(R.id.button_blur);
        button_stricker = (ImageButton) findViewById(R.id.button_stricker);
        button_paint = (ImageButton) findViewById(R.id.button_paint);

        button_hide_face_detect = (ImageButton) findViewById(R.id.button_hide_face_detect);
        button_face_detect = (ImageButton) findViewById(R.id.button_face_detect);
        //-----------------------------------------------------------------

        layout_face_detect.setOnClickListener(this);

        button_blur.setOnClickListener(this);
        button_stricker.setOnClickListener(this);
        button_paint.setOnClickListener(this);

        button_blur_layout.setOnClickListener(this);
        button_stricker_layout.setOnClickListener(this);
        button_paint_layout.setOnClickListener(this);

        button_hide_face_detect.setOnClickListener(this);
        button_face_detect.setOnClickListener(this);

        frameFocusLayout = findViewById(R.id.fram_focus_layout);

        ImagePreview = (ImageView) findViewById(R.id.ImagePreview);

        button_bar = (RelativeLayout) findViewById(R.id.button_bar);
        HeadLayout = (RelativeLayout) findViewById(R.id.HeadLayout);
        HeadLayout2 = (RelativeLayout) findViewById(R.id.HeadLayout2);
        bottom_layout = (LinearLayout) findViewById(R.id.bottom_layout);

        menu_bar = (LinearLayout) findViewById(R.id.menu_bar);

        //RelativLayout Button Menu Bar (Edit / Info / Delete)
        button_edit = (RelativeLayout) findViewById(R.id.button_edit);
        button_info = (RelativeLayout) findViewById(R.id.button_info);
        button_delete = (RelativeLayout) findViewById(R.id.button_delete);

        button_edit.setOnClickListener(this);
        button_info.setOnClickListener(this);
        button_delete.setOnClickListener(this);

        //RelativeLayout Touch ImagePreview
        Touch_ImagePreview = (RelativeLayout) findViewById(R.id.Touch_ImagePreview);
        Touch_ImagePreview.setOnClickListener(this);

        //RelativeLayout Head Menu Bar
        button_back = (ImageButton) findViewById(R.id.button_back);
        button_back.setOnClickListener(this);

        ImagePreview.setOnClickListener(this);


        // SET GONE Layout
        bottom_layout.setVisibility(View.GONE);
        HeadLayout.setVisibility(View.GONE);
        button_bar.setVisibility(View.GONE);

        //Animation

/*
        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        String value_resolution = intent.getStringExtra("resolution");

        File file = new File(value);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        //Bitmap rotated = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getHeight(), (myBitmap.getHeight()/16)*9,
        //matrix, true);

        int img_height = 0;
        int img_width = 0;
        int y = 0;
        int x = 0;

        if (value_resolution.equals("4:3")) {
            img_height = myBitmap.getWidth();
            img_width = (myBitmap.getWidth() / 4) * 3;
            y = (myBitmap.getHeight() - img_width) / 2;
            x = (img_width - myBitmap.getHeight()) / 2;

        } else if (value_resolution.equals("16:9")) {
            img_height = myBitmap.getWidth();
            img_width = (myBitmap.getWidth() / 16) * 9;
            y = (myBitmap.getHeight() - img_width) / 2;
            x = 0;//(img_width - myBitmap.getHeight()) / 2;

        } else if (value_resolution.equals("1:1")) {
            img_height = myBitmap.getHeight();
            img_width = myBitmap.getHeight();
            y = (myBitmap.getHeight() - img_width) / 2;
            x = (myBitmap.getWidth() - myBitmap.getHeight()) / 2;
        }

        Toast.makeText(PreviewActivity.this, "img_height = " + img_height + "\nimg_width = " + img_width + "\ny = " + y + "\nx = " + x, Toast.LENGTH_SHORT).show();


        Bitmap rotated = Bitmap.createBitmap(myBitmap, x, y, img_height, img_width, matrix, true);

        //#####################################################################################
        ImageView imgPreView = findViewById(R.id.ImagePreview);
        //imgPreView.setImageBitmap(getResizedBitmap(rotated,1080,1440));
        //imgPreView.setImageResource(R.drawable.test);
        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        String value_resolution = intent.getStringExtra("resolution");
        File file = new File(value);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotated = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getHeight(), (myBitmap.getHeight()/16)*9,
        matrix, true);
     */
        ImageButton btnSave = (ImageButton) findViewById(R.id.button_save_image);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView okay_text, cancel_text;
                Dialog dialog = new Dialog(PreviewActivity.this);

                dialog.setContentView(R.layout.dialog_layout);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                //dialog.setCancelable(false);
                //dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                okay_text = dialog.findViewById(R.id.button_dialog_ok);
                cancel_text = dialog.findViewById(R.id.button_dialog_cancel);

                okay_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(PreviewActivity.this, "okay clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                cancel_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(PreviewActivity.this, "Cancel clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
              //  getImageUri((PreviewActivity.this), myBitmap);
                //String savedImageURL = MediaStore.Images.Media.insertImage((PreviewActivity.this).getContentResolver(), myBitmap, "filename", null);
                //บันทึกรูปลง Grallery
                //Uri savedImageURI = Uri.parse(savedImageURL);
                //Toast.makeText(PreviewActivity.this, "savedImageURL = " + contentValues, Toast.LENGTH_SHORT).show();
             //   file.delete();
             //   finish();
            }
        });
        ImageButton btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  file.delete();
              //  finish();
            }
        });


        try {
            Log.e("TEST", "detector OK");
            makeText(this, "detector OK", LENGTH_SHORT).show();
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            Log.e("TEST", "detector ERROR");
            makeText(this, "detector ERROR", LENGTH_SHORT).show();
            e.printStackTrace();
        }


        //largeIcon = rotated;
        largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.aaa);

        //Bitmap largeIcon = myBitmap;

        Display display = getWindowManager().getDefaultDisplay();
        int display_width = display.getWidth();

        FrameImagePreview = (RelativeLayout) findViewById(R.id.FrameImagePreview);
        //FrameImagePreview.getLayoutParams().height = (int) (display_width / largeIcon.getHeight()) * largeIcon.getHeight();
        //FrameImagePreview.getLayoutParams().width = display_width;

        int a = (int) (display_width / largeIcon.getHeight()) * largeIcon.getHeight();
        //FrameImagePreview.getLayoutParams().width = display_width;

        //FrameImagePreview.getLayoutParams().height = 608;
        //FrameImagePreview.getLayoutParams().width = 1080;

        makeText(this, "display_width = " + display_width, LENGTH_SHORT).show();
        Log.e("IMG","///////////////////////////////////////////");
        Log.e("IMG","display_width = " + display_width);
        Log.e("IMG","W = " + largeIcon.getWidth());
        Log.e("IMG","H = " + largeIcon.getHeight());

        ImageView imgPreView = findViewById(R.id.ImagePreview);

        if (largeIcon.getWidth() > largeIcon.getHeight()){
            float f = ((float) display_width / (float)largeIcon.getWidth());

            int fW = Math.round( f * (float)largeIcon.getWidth());
            int fH = Math.round( f * (float)largeIcon.getHeight());

            global_img_width = fW;
            getGlobal_img_height = fH;

            frameFocusLayout.getLayoutParams().height = fH;
            frameFocusLayout.getLayoutParams().width = fW;

            Log.e("IMG","FW = " + fW);
            Log.e("IMG","FH = " + fH);

            imgPreView.setImageBitmap(getResizedBitmap(largeIcon, fW, fH));
        }else if (largeIcon.getWidth() < largeIcon.getHeight()){
            float f = ((float) display_width / (float)largeIcon.getWidth());

            int fW = Math.round( f * (float)largeIcon.getWidth());
            int fH = Math.round( f * (float)largeIcon.getHeight());

            global_img_width = fW;
            getGlobal_img_height = fH;

            frameFocusLayout.getLayoutParams().height = fH;
            frameFocusLayout.getLayoutParams().width = fW;

            Log.e("IMG","FW = " + fW);
            Log.e("IMG","FH = " + fH);

            imgPreView.setImageBitmap(getResizedBitmap(largeIcon, fW, fH));

            /*
            int height = FrameImagePreview.getHeight();
            float f = (height / (float)largeIcon.getHeight());
            Log.e("IMG","height = " + height);
            Log.e("IMG","f = " + f);
            int fW = Math.round( f * (float)largeIcon.getWidth());
            int fH = Math.round( f * (float)largeIcon.getHeight());
            float x = largeIcon.getHeight() / 1440f;
            int xfW = (int)largeIcon.getWidth() / (int)x;
            Log.e("XFW","XFW = " + xfW);
            Log.e("XFW","Image H = " + largeIcon.getHeight());
            Log.e("XFW","Image W = " + largeIcon.getWidth());
            Log.e("XFW","Image X = " + x);
            Log.e("XFW","Image A = " + Math.round(largeIcon.getWidth() / x));
            RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.main_layout);
            int head_layout_height = getHeightOfView(HeadLayout);
            int bottom_layout_height = getHeightOfView(bottom_layout);
            int main_layout_height = getHeightOfView(frameFocusLayout);
            Log.e("XXX","Image X = " + (main_layout_height - head_layout_height - bottom_layout_height));
            //int fW = Math.round(largeIcon.getWidth() / x);
            //int fH = main_layout_height - head_layout_height - bottom_layout_height;
            global_img_width = fW;
            getGlobal_img_height = fH;
            FrameImagePreview.getLayoutParams().height = fH;
            FrameImagePreview.getLayoutParams().width = fW;
            Log.e("IMG","FW = " + fW);
            Log.e("IMG","FH = " + fH);
            imgPreView.setImageBitmap(getResizedBitmap(largeIcon, fW, fH));
             */
        }

        imgPreView.setImageBitmap(largeIcon);
        ////imgPreView.setBackgroundResource(R.drawable.test);

        tempBitmap = largeIcon;
        bmp_images = new ArrayList<Bitmap>();
        handleResult();

        //int head_layout_height = getHeightOfView(HeadLayout);
        //int bottom_layout_height = getHeightOfView(bottom_layout);


        old_height_header_layout = getHeightOfView(HeadLayout);
        old_width_header_layout = 1080;

        old_height_bottom_layout = getHeightOfView(bottom_layout);
        old_width_bottom_layout = 1080;

        Log.e("OLD","old_height_header_layout = " + old_height_header_layout);
        Log.e("OLD","old_width_header_layout = " + old_width_header_layout);

        Log.e("OLD","old_height_bottom_layout = " + old_height_bottom_layout);
        Log.e("OLD","old_width_bottom_layout = " + old_width_bottom_layout);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    boolean state_face_detect_button = true;

    boolean state_blur_button = true;
    boolean state_stricker_button = true;
    boolean state_paint_button = true;
    boolean state_ImagePreview = true;
    boolean state_Edite_Mode = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.resumeThread();
        MainActivity.isWorking = true;

    }

    @Override
    public void onClick(View view) {

        //Animation
        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeIn.setStartOffset(100);
        Animation animFadeOu = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        //Animation 2 for Menu Bar [Edit - Info - Delete]
        Animation animFadeIn2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in2);
        Animation animFadeOu2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out2);

        switch (view.getId()) {
            case R.id.button_hide_face_detect:
                slideView2(layout_face_detect, layout_face_detect.getLayoutParams().height, layout_face_detect.getLayoutParams().height, 1270, 256);
                break;
            case R.id.button_face_detect:
                if (state_face_detect_button) {
                    slideView2(layout_face_detect, layout_face_detect.getLayoutParams().height, layout_face_detect.getLayoutParams().height, layout_face_detect.getLayoutParams().width, 1270);
                    state_face_detect_button = false;
                } else {
                    slideView2(layout_face_detect, layout_face_detect.getLayoutParams().height, layout_face_detect.getLayoutParams().height, 1000, 256);
                    state_face_detect_button = true;
                }
                break;
            case R.id.button_blur:
                if (state_blur_button) {

                    int button_width = button_blur.getMeasuredWidth();
                    int option_layout_width = option_layout.getMeasuredWidth();
                    int newWidth = (option_layout_width - button_width) - 110;

                    layout_blur_radius.setVisibility(View.VISIBLE);
                    slideView2(layout_blur_radius, layout_blur_radius.getLayoutParams().height, layout_blur_radius.getLayoutParams().height, 0, newWidth);

                    button_stricker_layout.startAnimation(animFadeOu);
                    button_paint_layout.startAnimation(animFadeOu);

                    button_stricker_layout.setVisibility(View.GONE);
                    button_paint_layout.setVisibility(View.GONE);

                    state_blur_button = false;
                } else {
                    slideView2(layout_blur_radius, layout_blur_radius.getLayoutParams().height, layout_blur_radius.getLayoutParams().height, layout_blur_radius.getLayoutParams().width, 0);

                    button_stricker_layout.startAnimation(animFadeIn);
                    button_paint_layout.startAnimation(animFadeIn);

                    button_stricker_layout.setVisibility(View.VISIBLE);
                    button_paint_layout.setVisibility(View.VISIBLE);

                    state_blur_button = true;
                }
                break;
            case R.id.button_stricker:
                if (state_stricker_button) {

                    int button_width = button_stricker.getMeasuredWidth();
                    int option_layout_width = option_layout.getMeasuredWidth();
                    int newWidth = (option_layout_width - button_width) - 110;

                    layout_stricker_option.setVisibility(View.VISIBLE);
                    slideView2(layout_stricker_option, layout_stricker_option.getLayoutParams().height, layout_stricker_option.getLayoutParams().height, 0, newWidth);

                    button_blur_layout.startAnimation(animFadeOu);
                    //button_stricker_layout.startAnimation(animFadeOu);
                    button_paint_layout.startAnimation(animFadeOu);

                    button_blur_layout.setVisibility(View.GONE);
                    button_stricker_layout.setVisibility(View.VISIBLE);
                    button_paint_layout.setVisibility(View.GONE);

                    state_stricker_button = false;
                } else {
                    slideView2(layout_stricker_option, layout_stricker_option.getLayoutParams().height, layout_stricker_option.getLayoutParams().height, layout_stricker_option.getLayoutParams().width, 0);

                    button_blur_layout.startAnimation(animFadeIn);
                    //button_stricker_layout.startAnimation(animFadeOu);
                    button_paint_layout.startAnimation(animFadeIn);

                    button_blur_layout.setVisibility(View.VISIBLE);
                    button_stricker_layout.setVisibility(View.VISIBLE);
                    button_paint_layout.setVisibility(View.VISIBLE);

                    state_stricker_button = true;
                }
                break;
            case R.id.button_paint:
                if (state_paint_button) {

                    int button_width = button_paint.getMeasuredWidth();
                    int option_layout_width = option_layout.getMeasuredWidth();
                    int newWidth = (option_layout_width - button_width) - 110;

                    layout_paint_option.setVisibility(View.VISIBLE);
                    slideView2(layout_paint_option, layout_paint_option.getLayoutParams().height, layout_paint_option.getLayoutParams().height, 0, newWidth);

                    button_blur_layout.startAnimation(animFadeOu);
                    button_stricker_layout.startAnimation(animFadeOu);
                    //button_paint_layout.startAnimation(animFadeOu);

                    button_blur_layout.setVisibility(View.GONE);
                    button_stricker_layout.setVisibility(View.GONE);
                    button_paint_layout.setVisibility(View.VISIBLE);

                    state_paint_button = false;

                } else {
                    slideView2(layout_paint_option, layout_paint_option.getLayoutParams().height, layout_paint_option.getLayoutParams().height, layout_paint_option.getLayoutParams().width, 0);

                    button_blur_layout.startAnimation(animFadeIn);
                    button_stricker_layout.startAnimation(animFadeOu);
                    //button_paint_layout.startAnimation(animFadeIn);

                    button_blur_layout.setVisibility(View.VISIBLE);
                    button_stricker_layout.setVisibility(View.VISIBLE);
                    button_paint_layout.setVisibility(View.VISIBLE);

                    state_paint_button = true;

                    //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)button_paint_layout.getLayoutParams();
                    //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    //params.addRule(RelativeLayout.RIGHT_OF, R.id.button_paint_layout);

                    //button_paint_layout.setLayoutParams(params); //causes layout update
                }
                break;
            case R.id.Touch_ImagePreview:
                if (state_ImagePreview){
                    menu_bar.setVisibility(View.VISIBLE);
                    HeadLayout2.setVisibility(View.VISIBLE);

                    menu_bar.startAnimation(animFadeIn2);
                    HeadLayout2.startAnimation(animFadeIn2);

                    state_ImagePreview = false;
                }else {
                    menu_bar.setVisibility(View.GONE);
                    HeadLayout2.setVisibility(View.GONE);

                    menu_bar.startAnimation(animFadeOu2);
                    HeadLayout2.startAnimation(animFadeOu2);

                    state_ImagePreview = true;
                }

                break;
            case R.id.button_edit:
                state_ImagePreview = true;
                    menu_bar.setVisibility(View.GONE);
                    HeadLayout2.setVisibility(View.GONE);
                    Touch_ImagePreview.setVisibility(View.GONE);

                    bottom_layout.setVisibility(View.VISIBLE);
                    HeadLayout.setVisibility(View.VISIBLE);
                    button_bar.setVisibility(View.VISIBLE);



                    //int head_layout_height = getHeightOfView(HeadLayout);
                    //int bottom_layout_height = getHeightOfView(bottom_layout);

                    //RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.main_layout);

                    //int x = getHeightOfView(main_layout) - (head_layout_height + bottom_layout_height);
                    //FrameImagePreview
                    //slideView2(FrameImagePreview, FrameImagePreview.getLayoutParams().height, x, FrameImagePreview.getLayoutParams().width, FrameImagePreview.getLayoutParams().width);

                    //Log.e("IMG", "head_layout_height = " + head_layout_height);
                    //Log.e("IMG", "bottom_layout_height = " + bottom_layout_height);



                    RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.preview);
                    RelativeLayout FrameImagePreview = (RelativeLayout) findViewById(R.id.FrameImagePreview);

                    int head_layout_height = getHeightOfView(HeadLayout);
                    int bottom_layout_height = getHeightOfView(bottom_layout);
                    int main_layout_height = main_layout.getHeight();

                    int x = main_layout_height - head_layout_height - bottom_layout_height;

                    slideView2(FrameImagePreview, FrameImagePreview.getHeight(), x, FrameImagePreview.getLayoutParams().width, FrameImagePreview.getLayoutParams().width);
                    slideView2(bottom_layout, 0, old_height_bottom_layout, old_width_bottom_layout, old_width_bottom_layout);
                    slideView2(HeadLayout, 0, old_height_header_layout, old_width_bottom_layout, old_width_header_layout);


                    Log.e("LAY","head_layout_height = " + head_layout_height);
                    Log.e("LAY","bottom_layout_height = " + bottom_layout_height);
                    Log.e("LAY","main_layout_height = " + main_layout_height);
                    Log.e("LAY","x = " + x);

                    //slideView2(bottom_layout, 0, old_height_bottom_layout, 0, old_width_bottom_layout);
                    //slideView2(HeadLayout, 0, old_height_header_layout, 0, old_width_header_layout);
                    //button_bar.setVisibility(View.GONE);
                    state_Edite_Mode = false;
                    menu_bar.setVisibility(View.GONE);
                break;
            case R.id.button_info:

                break;
            case R.id.button_delete:

                break;
            case R.id.button_back:
                menu_bar.setVisibility(View.GONE);
                HeadLayout2.setVisibility(View.GONE);
                Touch_ImagePreview.setVisibility(View.VISIBLE);

                slideView2(bottom_layout, bottom_layout.getLayoutParams().height, 0, bottom_layout.getLayoutParams().width, bottom_layout.getLayoutParams().width);
                slideView2(HeadLayout, HeadLayout.getLayoutParams().height, 0, HeadLayout.getLayoutParams().width, HeadLayout.getLayoutParams().width);


                RelativeLayout main_layout2 = (RelativeLayout) findViewById(R.id.preview);
                RelativeLayout FrameImagePreview2 = (RelativeLayout) findViewById(R.id.FrameImagePreview);

                int head_layout_height2 = getHeightOfView(HeadLayout);
                int bottom_layout_height2 = getHeightOfView(bottom_layout);
                int main_layout_height2 = main_layout2.getHeight() ;

                int x2 = main_layout_height2 + head_layout_height2;

                slideView2(FrameImagePreview2, FrameImagePreview2.getLayoutParams().height, x2, FrameImagePreview2.getLayoutParams().width, FrameImagePreview2.getLayoutParams().width);



                //int head_layout_height = getHeightOfView(HeadLayout);
                //int bottom_layout_height = getHeightOfView(bottom_layout);

                //RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.main_layout);

                //int x = getHeightOfView(main_layout) - (head_layout_height + bottom_layout_height);

                //FrameImagePreview
                //slideView2(FrameImagePreview, FrameImagePreview.getLayoutParams().height, x, FrameImagePreview.getLayoutParams().width, FrameImagePreview.getLayoutParams().width);

                //slideView2(bottom_layout, bottom_layout.getLayoutParams().height, 0, bottom_layout.getLayoutParams().width, 0);
                //slideView2(HeadLayout, HeadLayout.getLayoutParams().height, 0, HeadLayout.getLayoutParams().width, 0);

                //bottom_layout.setVisibility(View.GONE);
                //HeadLayout.setVisibility(View.GONE);
                button_bar.setVisibility(View.GONE);
                break;
            //case R.id.button_save_image:
           //     break;
        }
    }

    public void setFocusView(double X, double Y, double width, double height, String str, float xPos, float yPos, int type) {
        //removeView();
        int x = 0, y = 0, h = 0, w = 0;
        Display display = getWindowManager().getDefaultDisplay();

        int width2 = global_img_width;//display.getWidth();
        int height2 = getGlobal_img_height;
/*
        switch (state_serol) {
            case "4:3":
                height2 = 1440;
                break;
            case "16:9":
                height2 = 1920;
                break;
            case "1:1":
                height2 = 1080;
                break;
        }
        if(state_serol == "16:9"){
            height2 = 1920;
        }else if(state_serol == "4:3"){
            height2 = 1440;
        }else if(state_serol == "1:1"){
            height2 = 1080;
        }
*/
        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        x = (int) Math.round((float) (X * width2));
        y = (int) Math.round((float) (Y * height2));

        Bitmap bb = getResizedBitmap(largeIcon, global_img_width, getGlobal_img_height);
        //Bitmap b = crop(bb, x, y, w, h);

        if (w > h){
            Bitmap b = crop(bb, x, y, w + 15, w + 15);
            bmp_images.add(b);
        }else {
            Bitmap b = crop(bb, x, y, h + 15, h + 15);
            bmp_images.add(b);
        }





        //Toast.makeText(this, "w = " + w + "/nh = " + h, LENGTH_SHORT).show();


        LayoutInflater inflater = LayoutInflater.from(PreviewActivity.this);

        @SuppressLint("InflateParams") View focus_frame = inflater.inflate(R.layout.focus_frame, null);
/*
        if ((h > 70 || w > 70) && ((h < 130 || w < 130))) {
            focus_frame = inflater.inflate(R.layout.focus_frame_m, null);
        } else if ((h > 0 || w > 0) && ((h < 70 || w < 70))) {
            focus_frame = inflater.inflate(R.layout.focus_frame_s, null);
        }
 */
        //if(type == 1){
        //    focus_frame = inflater.inflate(R.layout.focus_frame, null);
        //}else if(type == 0){
        //    focus_frame = inflater.inflate(R.layout.emoji_layout, null);
        //}


        //
        focus_frame.setId(Integer.parseInt(str));
        int strId = focus_frame.getId();
        focus_frame.setOnClickListener(view -> makeText(PreviewActivity.this, "CLICK = " + strId, LENGTH_SHORT).show());
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(x, y, 0, 0);

        TextView txt = new TextView(this);
        txt.setTextSize(6);
        txt.setText(h + "x" + w);


        frameFocusLayout.addView(focus_frame, params1);
        //frameFocusLayout.addView(txt, params1);


    }

    @Override
    public void run() {
        // adding the image in ImageView
        clearFocus();
        listView = (LinearLayout) findViewById(R.id.listView);

        for (int i = 0; i < bmp_images.size(); i++){

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params1.height = 100;
            params1.width = 100;
            params1.setMargins(0, 0, 10, 0);

            ImageView imageView = new ImageView(PreviewActivity.this);
            imageView.setImageBitmap(bmp_images.get(i));

            listView.addView(imageView, params1);

        }
    }

    public void clearFocus() {
        if (null != listView && listView.getChildCount() > 0) {
            listView.removeViews(0, listView.getChildCount());
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

    //จัดการกับ Label คำตอบ
    private void handleResult() {
//        startTime = System.currentTimeMillis();
        Bitmap bmp = tempBitmap;

        if (bmp == null) {
            makeText(this, "ERROR", LENGTH_SHORT).show();
        } else {
            List<Classifier.Recognition> results = detector.recognizeImage(getResizedBitmap(bmp, 320, 320));
            int i = 0;
            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                //                           X - Y - Width - Height
                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                    if (true) {
                        setFocusView(location.left, location.top, location.right, location.bottom, i + "", result.getX(), result.getY(), 1);

                        //Bitmap b = crop(bmp, (float)location.left * 640f, (float)location.top * 640f, (float)location.right * 640f, (float)location.bottom * 640f);
                        //bmp_images.add(b);
                    }
                }
                i++;
            }
            run();
        }
    }

    public static Bitmap crop(Bitmap bitmap, float x, float y, float newWidth,
                              float newHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, (int)x, (int)y,
                (int) newWidth, (int) newHeight, null, true);
        return resizedBitmap;
    }

    private int getHeightOfView(View contentview) {
        contentview.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return contentview.getMeasuredHeight();
    }
}
