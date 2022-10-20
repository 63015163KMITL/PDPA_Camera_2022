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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    public LinearLayout layout_face_detect, layout_blur_radius, layout_stricker_option, layout_paint_option, button_blur_layout, button_stricker_layout, button_paint_layout;
    public ImageButton button_hide_face_detect, button_face_detect, button_blur, button_stricker, button_paint;
    public RelativeLayout option_layout, fram_focus_layout, FrameImagePreview, FrameImagePreview_TOP, button_bar, HeadLayout, HeadLayout2, main_layout;
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
    private ImageView imageView, ImagePreview, imgPreView;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    public Bitmap tempBitmap = null;
    public List<Classifier.Recognition> results = null;

    //public String state_serol = "16:9";

    //Dispaly
    public Display display;

    public int old_height_header_layout = 0;
    public int old_width_header_layout = 0;
    public int old_height_bottom_layout = 0;
    public int old_width_bottom_layout = 0;

    //Global Value /////////////////////////////////////////////////////////////////////////////////
    public int global_preview_height = 0;           //ขนาดของพื้นที่แสดงภาพตัวอย่าง ณ ปัจจุบัน
    public int global_preview_width = 0;
    public int global_screen_width = 0;             //ขนาดความกว้างสูงสุดของหน้าจอ

    public Bitmap nowPhotoPreview;                  //Now Photo Preview
    public int nowPhoto_Height, nowPhoto_Width;     //Now size of photo
    public int heightPhoto, widthPhoto;             //Real size of photo
    public int max_fram_focus_layout_height;        //ความกว้างสูงสุดที่สามารถแสดงตัวอย่างได้

    //SetFocus Var
    int x = 0, y = 0, h = 0, w = 0;
    int height2 = 0;
    int width2 = 0;

    //Animation
    public Animation animFadeIn2, animFadeOu2, animFadeIn, animFadeOu;

    public int global_img_width, global_img_height;
    public ArrayList<Bitmap> bmp_images = new ArrayList<Bitmap>();
    public ArrayList<String> facePosition = new ArrayList<String>();

    public TextView textview_blur_radius_value;
    public SeekBar seekbar_blur_radius;

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

        display = getWindowManager().getDefaultDisplay();

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

        fram_focus_layout = findViewById(R.id.fram_focus_layout);


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
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeIn.setStartOffset(100);
        animFadeOu = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        //Animation 2 for Menu Bar [Edit - Info - Delete]
        animFadeIn2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in2);
        animFadeOu2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out2);

        main_layout = (RelativeLayout) findViewById(R.id.preview);
        FrameImagePreview = (RelativeLayout) findViewById(R.id.FrameImagePreview);
        FrameImagePreview_TOP = (RelativeLayout) findViewById(R.id.FrameImagePreview_TOP);
        fram_focus_layout = (RelativeLayout) findViewById(R.id.fram_focus_layout);

        textview_blur_radius_value = findViewById(R.id.textview_blur_radius_value);
        seekbar_blur_radius = (SeekBar) findViewById(R.id.seekbar_blur_radius);

        // Get intent form MainActivity
        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        String value_resolution = intent.getStringExtra("resolution");
        File file = new File(value);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        //set real size of photo
        heightPhoto = myBitmap.getHeight();
        widthPhoto = myBitmap.getWidth();

        //set photo rotate
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

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

        //Toast.makeText(PreviewActivity.this, "img_height = " + img_height + "\nimg_width = " + img_width + "\ny = " + y + "\nx = " + x, Toast.LENGTH_SHORT).show();


        //ภาพถ่ายที่ผ่านการหมุนตามเข้มนาฬิกาแล้ว = Rotate
        Bitmap rotated = Bitmap.createBitmap(myBitmap, x, y, img_height, img_width, matrix, true);

        ImageButton btnSave = (ImageButton) findViewById(R.id.button_save_image);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView okay_text, cancel_text;
                Dialog dialog = new Dialog(PreviewActivity.this);

                dialog.setContentView(R.layout.dialog_layout);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
            //makeText(this, "detector OK", LENGTH_SHORT).show();
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            Log.e("TEST", "detector ERROR");
            //makeText(this, "detector ERROR", LENGTH_SHORT).show();
            e.printStackTrace();
        }


        //largeIcon = rotated;
        //largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.aaa);
        nowPhotoPreview = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        //nowPhotoPreview = rotated;

        imgPreView = findViewById(R.id.ImagePreview);
        imgPreView.setImageBitmap(nowPhotoPreview);
        ////imgPreView.setBackgroundResource(R.drawable.test);

        //Bitmap largeIcon = myBitmap;

        adjustImageDisplay(nowPhotoPreview);

        //tempBitmap = nowPhotoPreview;
        //handleResult();

        //int head_layout_height = getHeightOfView(HeadLayout);
        //int bottom_layout_height = getHeightOfView(bottom_layout);

        old_height_header_layout = getHeightOfView(HeadLayout);
        old_width_header_layout = 1080;

        old_height_bottom_layout = getHeightOfView(bottom_layout);
        old_width_bottom_layout = 1080;


        Log.e("IMG","OnCreate Image  Width ///////////////////////////////////////////////////////");

        Log.e("IMG","   Real Photo Height = " + heightPhoto);
        Log.e("IMG","   Real Photo Width = " + widthPhoto);

        if(max_fram_focus_layout_height < fram_focus_layout.getLayoutParams().height){
            max_fram_focus_layout_height = fram_focus_layout.getLayoutParams().height;
            Log.e("IMG","   max_fram_focus_layout_height = " + max_fram_focus_layout_height);
        }


        resetSizeOfPhotoPreview();

        xMAX_HEIGHT_PREVIEW = fram_focus_layout.getLayoutParams().height;
        xMAX_WIDTH_PREVIEW = fram_focus_layout.getLayoutParams().width;

        //editMode(true);
        //editMode(false);
        seekbar_blur_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                textview_blur_radius_value.setText(progress + "");
                //progress * Math.pow(10,-3)

                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_LONG).show();
                double n;// = (100 - progress ) * Math.pow(10,-3);
                n = (1.0 - (progress / 100.0));
                Log.e("IMG"," N = " + n);

                Log.e("IMG","LOG FACE //////////////////////////////////////////////////////////");
                for (int i = 0; i < facePosition.size(); i++){
                    //Log.e("IMG","   - " + facePosition.get(i));
                    String[] a = facePosition.get(i).split("/");
                    //Log.e("IMG","   a = " + Arrays.toString(a));
                    //if((n + "").equals("0.1")){
                        //setFocusView(Double.parseDouble(a[0]), Double.parseDouble(a[1]), Double.parseDouble(a[2]), Double.parseDouble(a[3]), Double.parseDouble(a[4]), Double.parseDouble(a[5]), 0.9);
                        //Log.e("IMG","   Radius = " + 0.9);
                    //}
                    setFocusView(Double.parseDouble(a[0]), Double.parseDouble(a[1]), Double.parseDouble(a[2]), Double.parseDouble(a[3]), Double.parseDouble(a[4]), Double.parseDouble(a[5]), n);
                    //Log.e("IMG","   Radius = " + n);
                }

                //Log.e("IMG","   SEEK = " + progress);
                //Log.e("IMG","   0.1d = " + 0.1d);

                //fram_focus_layout.addView(imgBlur, params1);

            }
        });
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    //สถานะการเลือกเมนู หรือการกดปุ่ม
    boolean state_face_detect_button = true;
    boolean state_blur_button = true;
    boolean state_stricker_button = true;
    boolean state_paint_button = true;
    boolean state_ImagePreview = true;
    boolean state_Edite_Mode = true;
    int layout_face_detect_width_MAX = 0;

    int MAX_HEIGHT_PREVIEW = 0;
    int MAX_WIDTH_PREVIEW = 0;

    int xMAX_HEIGHT_PREVIEW = 0;
    int xMAX_WIDTH_PREVIEW = 0;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.resumeThread();
        MainActivity.isWorking = true;
    }

    @Override
    public void onClick(View view) {

        if (layout_face_detect_width_MAX < layout_face_detect.getWidth()){
            layout_face_detect_width_MAX = layout_face_detect.getWidth();
        }

        switch (view.getId()) {
            case R.id.button_hide_face_detect:
                slideView2(layout_face_detect, layout_face_detect.getLayoutParams().height, layout_face_detect.getLayoutParams().height,  layout_face_detect.getWidth(), layout_face_detect.getHeight() + 50);
                state_face_detect_button = true;
                break;
            case R.id.button_face_detect:
                if (state_face_detect_button) {
                    slideView2(layout_face_detect, layout_face_detect.getLayoutParams().height, layout_face_detect.getLayoutParams().height, layout_face_detect.getWidth(), layout_face_detect_width_MAX);
                    state_face_detect_button = false;
                } else {
                    slideView2(layout_face_detect, layout_face_detect.getLayoutParams().height, layout_face_detect.getLayoutParams().height, layout_face_detect_width_MAX, layout_face_detect.getHeight() + 50);
                    state_face_detect_button = true;
                }
                break;
            case R.id.button_blur:
                if (state_blur_button) {

                    seekbar_blur_radius.setProgress(50);
                    int button_width = button_blur.getMeasuredWidth();
                    int option_layout_width = option_layout.getMeasuredWidth();
                    int newWidth = (option_layout_width - button_width) - 110;

                    layout_blur_radius.setVisibility(View.VISIBLE);

                    slideView2(layout_blur_radius, layout_blur_radius.getLayoutParams().height, layout_blur_radius.getLayoutParams().height, 0, newWidth);

                    //Animation Fade Out (Stricker - Paint)
                    button_stricker_layout.startAnimation(animFadeOu);
                    button_paint_layout.startAnimation(animFadeOu);

                    //Visibility Layout/View (Stricker - Paint)
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
                }
                break;
            case R.id.Touch_ImagePreview:
                if (state_ImagePreview){
                    //Set VISIBLE
                    menu_bar.setVisibility(View.VISIBLE);
                    HeadLayout2.setVisibility(View.VISIBLE);
                    //Set Animation Fade In
                    menu_bar.startAnimation(animFadeIn2);
                    HeadLayout2.startAnimation(animFadeIn2);
                    //Set State state_ImagePreview
                    state_ImagePreview = false;
                }else {
                    //Set VISIBLE GONE
                    menu_bar.setVisibility(View.GONE);
                    HeadLayout2.setVisibility(View.GONE);
                    //Set Animation Fade Out
                    menu_bar.startAnimation(animFadeOu2);
                    HeadLayout2.startAnimation(animFadeOu2);
                    //Set State state_ImagePreview
                    state_ImagePreview = true;
                }

                break;
            case R.id.button_edit:
                editMode(true);
                break;
            case R.id.button_info:

                break;
            case R.id.button_delete:

                break;
            case R.id.button_back:
                editMode(false);
                break;
        }
    }

    public void setFocusView(double X, double Y, double width, double height, String str, float xPos, float yPos, int type) {

        height2 = xMAX_HEIGHT_PREVIEW;
        width2 = xMAX_WIDTH_PREVIEW;

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        x = (int) Math.round((float) (X * width2));
        y = (int) Math.round((float) (Y * height2));

        Bitmap bb = getResizedBitmap(nowPhotoPreview, width2, height2);
        Bitmap b = crop(bb, x, y, w, h);

        bmp_images.add(b);
        /*
        if (w > h){
            Bitmap b = crop(bb, x, y, w + 15, w + 15);
            bmp_images.add(b);
        }else {
            Bitmap b = crop(bb, x, y, h + 15, h + 15);
            bmp_images.add(b);
        }

         */

        LayoutInflater inflater = LayoutInflater.from(PreviewActivity.this);
        @SuppressLint("InflateParams") View focus_frame = inflater.inflate(R.layout.focus_frame, null);

        if ((h > 70 || w > 70) && ((h < 130 || w < 130))) {
            focus_frame = inflater.inflate(R.layout.focus_frame_m, null);
        } else if ((h > 0 || w > 0) && ((h < 70 || w < 70))) {
            focus_frame = inflater.inflate(R.layout.focus_frame_s, null);
        }

        if(type == 1){
            focus_frame = inflater.inflate(R.layout.focus_frame, null);
        }else if(type == 0){
            focus_frame = inflater.inflate(R.layout.emoji_layout, null);
        }

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

        fram_focus_layout.addView(focus_frame, params1);

        Animation animFadeIn2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in2);
        focus_frame.startAnimation(animFadeIn2);
        //frameFocusLayout.addView(txt, params1);
    }

    public void setFocusView(double X, double Y, double width, double height, double xPos, double yPos, double blurRadius) {

        height2 = xMAX_HEIGHT_PREVIEW;
        width2 = xMAX_WIDTH_PREVIEW;

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        x = (int) Math.round((float) (X * width2));
        y = (int) Math.round((float) (Y * height2));

        Bitmap bb = getResizedBitmap(nowPhotoPreview, width2, height2);
        Bitmap b = crop(bb, x, y, w, h);

        //bmp_images.add(getCroppedBitmap(b));

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(x, y, 0, 0);

        ImageView imgBlur = new ImageView(this);

        imgBlur.setImageBitmap(getMosaicsBitmap(b, blurRadius));
        //makeText(this, "SEEK = " + seekbar_blur_radius.getProgress(), LENGTH_SHORT).show();
        //imgBlur.setImageBitmap(getMosaicsBitmap(getCroppedBitmap(b), 1));

        fram_focus_layout.addView(imgBlur, params1);
    }

    @Override
    public void run() {
        //แสดงใบหน้าที่สามารถตรวจจับได้
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

    //ลบ Focus Frame View ออกทั้งหมด
    //Remove All setFocusView()
    public void clearFocus() {

        if (null != fram_focus_layout && fram_focus_layout.getChildCount() > 0) {
            fram_focus_layout.removeViews(0, fram_focus_layout.getChildCount());
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
        clearFocus();
        bmp_images.removeAll(bmp_images);
        if (null != listView && listView.getChildCount() > 0) {
            listView.removeViews(0, listView.getChildCount());
        }

        if (nowPhotoPreview == null) {
            makeText(this, "ERROR", LENGTH_SHORT).show();
        } else {
            List<Classifier.Recognition> results = detector.recognizeImage(getResizedBitmap(nowPhotoPreview, 320, 320));
            int i = 0;

            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                //                           X - Y - Width - Height
                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                    if (true) {
                        setFocusView(location.left, location.top, location.right, location.bottom, i + "", result.getX(), result.getY(), 1);
                        facePosition.add(location.left + "/" + location.top + "/" + location.right + "/" + location.bottom + "/" + result.getX() + "/" + result.getY());
                    }
                }
                i++;
            }

            Log.e("IMG","   setFocusView ////////////////////////////////////////////////////////////////////////////");
            Log.e("IMG","      - Number of faces detected = " + (bmp_images.size()));
            Log.e("IMG","      - xMAX_HEIGHT_PREVIEW = " + xMAX_HEIGHT_PREVIEW);
            Log.e("IMG","      - xMAX_WIDTH_PREVIEW = " + xMAX_WIDTH_PREVIEW);

            run();
        }
    }

    public static Bitmap crop(Bitmap bitmap, float x, float y, float newWidth, float newHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, (int)x, (int)y, (int) newWidth, (int) newHeight, null, true);
        return resizedBitmap;
    }

    private int getHeightOfView(View contentview) {
        contentview.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return contentview.getMeasuredHeight();
    }

    public void resetSizeOfPhotoPreview(){
        //nowPhoto_Width = nowPhotoPreview.getWidth();
        //nowPhoto_Height = nowPhotoPreview.getHeight();

        Log.e("IMG","   Reset Size Of PhotoPreview /////////////////////////////////////////////////");

        //Log.e("IMG","      - global_screen_width = " + display.getWidth() + "px");
        Log.e("IMG","      - max_fram_focus_layout_height = " + max_fram_focus_layout_height + "px");
        //Log.e("IMG","      - global_preview_height = " + global_preview_height + "px");
        //Log.e("IMG","      - global_preview_width = " + global_preview_width + "px");

        Log.e("IMG","      - nowPhoto_Height = " + nowPhoto_Height + "px");
        Log.e("IMG","      - nowPhoto_Width = " + nowPhoto_Width + "px");


        /*
        Log.e("IMG","   Now Photo Height = " + nowPhoto_Height + "px");
        Log.e("IMG","   Now Photo Width = " + nowPhoto_Width + "px");

        Log.e("IMG","   MAX_WIDTH_PREVIEW = " + MAX_WIDTH_PREVIEW + "px");
        Log.e("IMG","   MAX_HEIGHT_PREVIEW = " + MAX_HEIGHT_PREVIEW + "px");

        Log.e("IMG","   NOW FRAME FOCUS HEIGHT = " + fram_focus_layout.getHeight() + "px");
        Log.e("IMG","   max_fram_focus_layout_height = " + max_fram_focus_layout_height);

        //Global Value /////////////////////////////////////////////////////////////////////////////////
    public int global_preview_height = 0;           //ขนาดของพื้นที่แสดงภาพตัวอย่าง ณ ปัจจุบัน
    public int global_preview_width = 0;
    public int global_screen_width = 0;             //ขนาดความกว้างสูงสุดของหน้าจอ

    public Bitmap nowPhotoPreview;                  //Now Photo Preview
    public int nowPhoto_Height, nowPhoto_Width;     //Now size of photo
    public int heightPhoto, widthPhoto;             //Real size of photo
    public int max_fram_focus_layout_height;        //ความกว้างสูงสุดที่สามารถแสดงตัวอย่างได้

         */
    }

    public void adjustImageDisplay(Bitmap bitmap){
        if (bitmap != null){
            Display display = getWindowManager().getDefaultDisplay();
            int display_width = display.getWidth();

            if (nowPhotoPreview.getWidth() > nowPhotoPreview.getHeight()){                                      //Landscape
                float f = ((float) display_width / (float)nowPhotoPreview.getWidth());

                int fW = Math.round( f * (float)nowPhotoPreview.getWidth());
                int fH = Math.round( f * (float)nowPhotoPreview.getHeight());

                global_img_width = fW;
                global_img_height = fH;

                fram_focus_layout.getLayoutParams().height = fH;
                fram_focus_layout.getLayoutParams().width = fW;

                imgPreView.setImageBitmap(getResizedBitmap(nowPhotoPreview, fW, fH));
            }else if (nowPhotoPreview.getWidth() < nowPhotoPreview.getHeight()){                                //Portrait
                float f = ((float) display_width / (float)nowPhotoPreview.getWidth());

                int fW = Math.round( f * (float)nowPhotoPreview.getWidth());
                int fH = Math.round( f * (float)nowPhotoPreview.getHeight());

                global_img_width = fW;
                global_img_height = fH;

                fram_focus_layout.getLayoutParams().height = fH;
                fram_focus_layout.getLayoutParams().width = fW;

                imgPreView.setImageBitmap(getResizedBitmap(nowPhotoPreview, fW, fH));

            }
        }
    }

    public void editMode(boolean b){
        if (layout_face_detect_width_MAX < layout_face_detect.getWidth()){
            layout_face_detect_width_MAX = layout_face_detect.getWidth();
        }
        if (b){
            Log.e("IMG","> Enter Edit Mode /////////////////////////////////////////////////////////////////////");

            menu_bar.setVisibility(View.GONE);
            HeadLayout2.setVisibility(View.GONE);
            Touch_ImagePreview.setVisibility(View.GONE);

            bottom_layout.setVisibility(View.VISIBLE);
            HeadLayout.setVisibility(View.VISIBLE);
            button_bar.setVisibility(View.VISIBLE);
            fram_focus_layout.setVisibility(View.VISIBLE);

            fram_focus_layout.startAnimation(animFadeIn);

            ///////////////////////////////////////////////////////////////////////////////////////


            int head_layout_height = getHeightOfView(HeadLayout);
            int bottom_layout_height = getHeightOfView(bottom_layout);
            int main_layout_height = main_layout.getHeight();

            Display display = getWindowManager().getDefaultDisplay();

            int free_height = main_layout_height - head_layout_height - bottom_layout_height;
            slideView2(FrameImagePreview_TOP, FrameImagePreview_TOP.getHeight(), free_height, FrameImagePreview_TOP.getWidth(), FrameImagePreview_TOP.getWidth());
            //int newWidthFocusFrame = (free_height * display.getWidth() / ImagePreview.getHeight());

            Log.e("IMG","   CALCULATE________________________________________");
            //Log.e("IMG","   - head_layout_height = " + head_layout_height);
            //Log.e("IMG","   - bottom_layout_height = " + bottom_layout_height);
            //Log.e("IMG","   - main_layout_height = " + main_layout_height);
            Log.e("IMG","   - imgPreView.getHeight() = " + imgPreView.getHeight());
            Log.e("IMG","   - free_height = " + free_height);
            Log.e("IMG","   - free_width = " +  display.getWidth());

            //slideView2(fram_focus_layout, fram_focus_layout.getHeight(), x, fram_focus_layout.getWidth(), newWidthFocusFrame);

            Log.e("IMG","   - ImagePreview.getHeight() = " + imgPreView.getHeight());
            Log.e("IMG", "   - fram_focus_layout.getHeight() = " + fram_focus_layout.getHeight());

            if(FrameImagePreview.getHeight() > MAX_HEIGHT_PREVIEW){
                MAX_HEIGHT_PREVIEW = FrameImagePreview.getHeight();
                Log.e("IMG", "   - MAX_HEIGHT_PREVIEW = " + MAX_HEIGHT_PREVIEW);
            }

            if(FrameImagePreview.getWidth() > MAX_WIDTH_PREVIEW){
                MAX_WIDTH_PREVIEW = FrameImagePreview.getWidth();
                Log.e("IMG", "   - MAX_WIDTH_PREVIEW = " + MAX_WIDTH_PREVIEW);
            }

            nowPhoto_Width = nowPhotoPreview.getWidth();
            nowPhoto_Height = nowPhotoPreview.getHeight();

            Log.e("IMG", "   CHECK Orientation ______________________________________________");
            Log.e("IMG", "      - nowPhotoPreview.getHeight() = " + nowPhotoPreview.getHeight());
            Log.e("IMG", "      - nowPhotoPreview.getWidth() = " + nowPhotoPreview.getWidth());

            int match_width = display.getWidth();
            int match_height = (nowPhotoPreview.getHeight() / nowPhotoPreview.getWidth() ) * display.getWidth();

            if (imgPreView.getHeight() > free_height) {                                                       //Landscape
                Log.e("IMG", "      imgPreView.getHeight > free_height________________________________________________________________");
                int newHeight = free_height;
                int newWidth = free_height * 1080 / imgPreView.getHeight();

                slideView2(FrameImagePreview, FrameImagePreview.getHeight(), newHeight, FrameImagePreview.getWidth(), newWidth);

                nowPhotoPreview = getResizedBitmap(nowPhotoPreview, newWidth,newHeight);

                xMAX_HEIGHT_PREVIEW = newHeight;
                xMAX_WIDTH_PREVIEW = newWidth;

                Log.e("IMG", "      - FrameImagePreview Height = " + FrameImagePreview.getHeight());
                Log.e("IMG", "      - FrameImagePreview Width = " + FrameImagePreview.getWidth());
            }else if (match_height < free_height){                                                                                 //Portaite
                Log.e("IMG", "      imgPreView.getHeight < free_height________________________________________________________________");

                slideView2(FrameImagePreview, FrameImagePreview.getHeight(), imgPreView.getHeight(), FrameImagePreview.getWidth(), display.getWidth());
                nowPhotoPreview = getResizedBitmap(nowPhotoPreview, display.getWidth(),imgPreView.getHeight());

                xMAX_HEIGHT_PREVIEW = imgPreView.getHeight();
                xMAX_WIDTH_PREVIEW = display.getWidth();

                Log.e("IMG", "      - FrameImagePreview Height = " + FrameImagePreview.getHeight());
                Log.e("IMG", "      - FrameImagePreview Width = " + FrameImagePreview.getWidth());
            }

            Log.e("IMG", "      - match_height = " + match_height);
            Log.e("IMG", "      - match_width = " + match_width);

            slideView2(bottom_layout, 0, old_height_bottom_layout, old_width_bottom_layout, old_width_bottom_layout);
            slideView2(HeadLayout, 0, old_height_header_layout, old_width_bottom_layout, old_width_header_layout);

            resetSizeOfPhotoPreview();
            handleResult();

            state_Edite_Mode = false;
            menu_bar.setVisibility(View.GONE);
            state_ImagePreview = true;

            //nowPhotoPreview = getResizedBitmap(nowPhotoPreview, xMAX_WIDTH_PREVIEW,xMAX_HEIGHT_PREVIEW);
            //nowPhotoPreview = getResizedBitmap(nowPhotoPreview, old_width_header_layout,x);
            Log.e("IMG", "   NOW PHOTO PREVIEW /////////////////////////////////////////////////////////////////////////////");
            Log.e("IMG", "   - Height = " + nowPhotoPreview.getHeight());
            Log.e("IMG", "   - Width = " + nowPhotoPreview.getWidth());

        }else {
            Log.e("IMG","< Exit Edit Mode /////////////////////////////////////////////////////////////////////");

            Log.e("IMG","   ImagePreview.getHeight() = " + imgPreView.getHeight());
            Log.e("IMG", "   fram_focus_layout.getHeight() = " + fram_focus_layout.getHeight());

            Log.e("IMG", "   CHECK Orientation ______________________________________________");
            Log.e("IMG", "      - nowPhotoPreview.getWidth() = " + nowPhotoPreview.getWidth());
            Log.e("IMG", "      - nowPhotoPreview.getHeight() = " + nowPhotoPreview.getHeight());

            //Recall MenuBar + Header + FliterPreview
            menu_bar.setVisibility(View.GONE);
            HeadLayout2.setVisibility(View.GONE);
            Touch_ImagePreview.setVisibility(View.VISIBLE);
            //fram_focus_layout.setVisibility(View.INVISIBLE);

            //Resize MenuBar + Header
            slideView2(bottom_layout, bottom_layout.getLayoutParams().height, 0, bottom_layout.getLayoutParams().width, bottom_layout.getLayoutParams().width);
            slideView2(HeadLayout, HeadLayout.getLayoutParams().height, 0, HeadLayout.getLayoutParams().width, HeadLayout.getLayoutParams().width);

            ////////////////////////////////////////////////////////////////////////////////////////
            //int x2 = FrameImagePreview2.getLayoutParams().height;
            Display display2 = getWindowManager().getDefaultDisplay();
            nowPhoto_Width = display2.getWidth();

            if(fram_focus_layout.getHeight() > MAX_HEIGHT_PREVIEW){
                MAX_HEIGHT_PREVIEW = fram_focus_layout.getHeight();
                Log.e("IMG", "   MAX_HEIGHT_PREVIEW = " + MAX_HEIGHT_PREVIEW);
            }

            if(fram_focus_layout.getWidth() > MAX_WIDTH_PREVIEW){
                MAX_WIDTH_PREVIEW = fram_focus_layout.getWidth();
                Log.e("IMG", "   MAX_WIDTH_PREVIEW = " + MAX_WIDTH_PREVIEW);
            }

            nowPhoto_Width = nowPhotoPreview.getWidth();
            nowPhoto_Height = nowPhotoPreview.getHeight();

            //nowPhoto_Width = display2.getWidth();
            //nowPhoto_Height = max_fram_focus_layout_height;
            //nowPhoto_Height = FrameImagePreview.getLayoutParams().height;

            slideView2(FrameImagePreview_TOP, FrameImagePreview_TOP.getHeight(), MAX_HEIGHT_PREVIEW, FrameImagePreview_TOP.getWidth(), MAX_WIDTH_PREVIEW);

            slideView2(FrameImagePreview, FrameImagePreview.getLayoutParams().height, MAX_HEIGHT_PREVIEW, FrameImagePreview.getWidth(), MAX_WIDTH_PREVIEW);
            //slideView2(fram_focus_layout, FrameImagePreview.getLayoutParams().height, max_fram_focus_layout_height, FrameImagePreview.getLayoutParams().width, display2.getWidth());

            button_bar.setVisibility(View.GONE);

            xMAX_HEIGHT_PREVIEW = max_fram_focus_layout_height;
            xMAX_WIDTH_PREVIEW = display.getWidth();

            resetSizeOfPhotoPreview();

            handleResult();

            state_Edite_Mode = true;

            nowPhotoPreview = getResizedBitmap(nowPhotoPreview, xMAX_WIDTH_PREVIEW, xMAX_HEIGHT_PREVIEW);
            //nowPhotoPreview = getResizedBitmap(nowPhotoPreview,display.getWidth(),FrameImagePreview.getLayoutParams().height);
            Log.e("IMG", "   NOW PHOTO PREVIEW /////////////////////////////////////////////////////////////////////////////");
            Log.e("IMG", "   - Height = " + nowPhotoPreview.getHeight());
            Log.e("IMG", "   - Width = " + nowPhotoPreview.getWidth());

            //adjustImageDisplay(nowPhotoPreview);
            //ImagePreview.setImageBitmap(getResizedBitmap(getMosaicsBitmap(nowPhotoPreview, 0.05), nowPhotoPreview.getWidth(), nowPhotoPreview.getHeight()));
        }

    }

    public void setBlur(){

    }

    public static Bitmap getMosaicsBitmap(Bitmap bmp, double precent) {
        long start = System.currentTimeMillis();
        int bmpW = bmp.getWidth();
        int bmpH = bmp.getHeight();
        Bitmap resultBmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBmp);
        Paint paint = new Paint();
        double unit;
        if (precent == 0) {
            unit = bmpW;
        } else {
            unit = 1 / precent;
        }
        double resultBmpW = bmpW / unit;
        double resultBmpH = bmpH / unit;
        for (int i = 0; i < resultBmpH; i++) {
            for (int j = 0; j < resultBmpW; j++) {
                int pickPointX = (int) (unit * (j + 0.5));
                int pickPointY = (int) (unit * (i + 0.5));
                int color;
                if (pickPointX >= bmpW || pickPointY >= bmpH) {
                    color = bmp.getPixel(bmpW / 2, bmpH / 2);
                } else {
                    color = bmp.getPixel(pickPointX, pickPointY);
                }
                paint.setColor(color);
                canvas.drawRect((int) (unit * j), (int) (unit * i), (int) (unit * (j + 1)), (int) (unit * (i + 1)), paint);
            }
        }
        canvas.setBitmap(null);
        long end = System.currentTimeMillis();
        Log.v("IMG", "DrawTime:" + (end - start));
        return resultBmp;
    }

    /**
     * 和上面的函数同样的功能，效率远高于上面
     *
     * @param bmp
     * @param precent
     * @return
     */
    public static Bitmap getMosaicsBitmaps(Bitmap bmp, double precent) {
        long start = System.currentTimeMillis();
        int bmpW = bmp.getWidth();
        int bmpH = bmp.getHeight();
        int[] pixels = new int[bmpH * bmpW];
        bmp.getPixels(pixels, 0, bmpW, 0, 0, bmpW, bmpH);
        int raw = (int) (bmpW * precent);
        int unit;
        if (raw == 0) {
            unit = bmpW;
        } else {
            unit = bmpW / raw; //原来的unit*unit像素点合成一个，使用原左上角的值
        }
        if (unit >= bmpW || unit >= bmpH) {
            return getMosaicsBitmap(bmp, precent);
        }
        for (int i = 0; i < bmpH; ) {
            for (int j = 0; j < bmpW; ) {
                int leftTopPoint = i * bmpW + j;
                for (int k = 0; k < unit; k++) {
                    for (int m = 0; m < unit; m++) {
                        int point = (i + k) * bmpW + (j + m);
                        if (point < pixels.length) {
                            pixels[point] = pixels[leftTopPoint];
                        }
                    }
                }
                j += unit;
            }
            i += unit;
        }
        long end = System.currentTimeMillis();
        Log.v("IMG", "DrawTime:" + (end - start));
        return Bitmap.createBitmap(pixels, bmpW, bmpH, Bitmap.Config.ARGB_8888);
    }
}

/*

            menu_bar.setVisibility(View.GONE);
            HeadLayout2.setVisibility(View.GONE);
            Touch_ImagePreview.setVisibility(View.GONE);

            bottom_layout.setVisibility(View.VISIBLE);
            HeadLayout.setVisibility(View.VISIBLE);
            button_bar.setVisibility(View.VISIBLE);
            //fram_focus_layout.setVisibility(View.VISIBLE);

            fram_focus_layout.setVisibility(View.VISIBLE);
            fram_focus_layout.startAnimation(animFadeIn);

            ///////////////////////////////////////////////////////////////////////////////////////
            int head_layout_height = getHeightOfView(HeadLayout);
            int bottom_layout_height = getHeightOfView(bottom_layout);
            int main_layout_height = main_layout.getHeight();

            Display display = getWindowManager().getDefaultDisplay();

            int x = main_layout_height - head_layout_height - bottom_layout_height;
            int newWidthFocusFrame = (x * display.getWidth() / imgPreView.getHeight());

            Log.e("IMG","   ImagePreview.getHeight() = " + imgPreView.getHeight());

            global_preview_height = x;
            global_preview_width = newWidthFocusFrame;

            slideView2(FrameImagePreview, FrameImagePreview.getHeight(), x, FrameImagePreview.getWidth(), newWidthFocusFrame);
            //slideView2(fram_focus_layout, fram_focus_layout.getHeight(), x, fram_focus_layout.getWidth(), newWidthFocusFrame);
            Log.e("IMG", "   fram_focus_layout.getHeight() = " + fram_focus_layout.getHeight());

            if(FrameImagePreview.getHeight() > MAX_HEIGHT_PREVIEW){
                MAX_HEIGHT_PREVIEW = FrameImagePreview.getHeight();
                Log.e("IMG", "   MAX_HEIGHT_PREVIEW = " + MAX_HEIGHT_PREVIEW);
            }

            if(FrameImagePreview.getWidth() > MAX_WIDTH_PREVIEW){
                MAX_WIDTH_PREVIEW = FrameImagePreview.getWidth();
                Log.e("IMG", "   MAX_WIDTH_PREVIEW = " + MAX_WIDTH_PREVIEW);
            }

            //nowPhoto_Width = newWidthFocusFrame;
            //nowPhoto_Height = ImagePreview.getHeight();

            nowPhoto_Width = global_preview_width;
            nowPhoto_Height = global_preview_height;

            Log.e("IMG", "   CHECK Orientation ______________________________________________");
            Log.e("IMG", "      - nowPhotoPreview.getWidth() = " + nowPhotoPreview.getWidth());
            Log.e("IMG", "      - nowPhotoPreview.getHeight() = " + nowPhotoPreview.getHeight());


            if (nowPhotoPreview.getWidth() > nowPhotoPreview.getHeight()){                              //Landscape
                Log.e("IMG", "   Photo Orientation = Landscape");
                xMAX_HEIGHT_PREVIEW = max_fram_focus_layout_height;
                xMAX_WIDTH_PREVIEW = display.getWidth();
            }else if(nowPhotoPreview.getWidth() < nowPhotoPreview.getHeight()){
                Log.e("IMG", "   Photo Orientation = Portaite");
                xMAX_HEIGHT_PREVIEW = global_preview_height;
                xMAX_WIDTH_PREVIEW = global_preview_width;
            }

            //xMAX_HEIGHT_PREVIEW = global_preview_height;
            //xMAX_WIDTH_PREVIEW = global_preview_width;

            ///////////////////////////////////////////////////////////////////////////////////////
            slideView2(bottom_layout, 0, old_height_bottom_layout, old_width_bottom_layout, old_width_bottom_layout);
            slideView2(HeadLayout, 0, old_height_header_layout, old_width_bottom_layout, old_width_header_layout);

            resetSizeOfPhotoPreview();
            handleResult();

            state_Edite_Mode = false;
            menu_bar.setVisibility(View.GONE);
            state_ImagePreview = true;

            nowPhotoPreview = getResizedBitmap(nowPhotoPreview, old_width_header_layout,x);
            Log.e("IMG", "   NOW PHOTO PREVIEW /////////////////////////////////////////////////////////////////////////////");
            Log.e("IMG", "   - Height = " + x);
            Log.e("IMG", "   - Width = " + old_width_header_layout);

 */


/*
if (nowPhotoPreview.getWidth() > nowPhotoPreview.getHeight()){                              //Landscape
                Log.e("IMG", "      - Photo Orientation = Landscape");
                xMAX_HEIGHT_PREVIEW = max_fram_focus_layout_height;
                xMAX_WIDTH_PREVIEW = display.getWidth();

                int newHeightFocusFrame = main_layout_height - head_layout_height - bottom_layout_height;
                int newWidthFocusFrame = (newHeightFocusFrame * display.getWidth() / imgPreView.getHeight());

                Log.e("IMG", "      - xMAX_HEIGHT_PREVIEW = " + xMAX_HEIGHT_PREVIEW);
                Log.e("IMG", "      - xMAX_WIDTH_PREVIEW = " + xMAX_WIDTH_PREVIEW);

                Log.e("IMG","   - newHeightFocusFrame = " + newHeightFocusFrame);
                Log.e("IMG","   - newWidthFocusFrame = " + newWidthFocusFrame);

                //slideView2(FrameImagePreview, FrameImagePreview.getHeight(), newHeightFocusFrame, FrameImagePreview.getWidth(), newWidthFocusFrame);
            }else if(nowPhotoPreview.getWidth() < nowPhotoPreview.getHeight()){                         //Portaite
                Log.e("IMG", "      - Photo Orientation = Portaite");

                //newHeightFocusFrame = main_layout_height - head_layout_height - bottom_layout_height;
                //newWidthFocusFrame = (newHeightFocusFrame * display.getWidth() / imgPreView.getHeight());

                int newHeightFocusFrame = (nowPhotoPreview.getHeight() / nowPhotoPreview.getWidth()) * display.getWidth();
                int newWidthFocusFrame = display.getWidth();
                int free_height = main_layout_height - head_layout_height - bottom_layout_height;

                xMAX_HEIGHT_PREVIEW = newHeightFocusFrame;
                xMAX_WIDTH_PREVIEW = newWidthFocusFrame;

                Log.e("IMG", "      - newHeightFocusFrame = " + newHeightFocusFrame);
                Log.e("IMG", "      - newWidthFocusFrame = " + newWidthFocusFrame);

                Log.e("IMG", "      - FREE SPACE Height = " + free_height);
                Log.e("IMG", "      - FREE SPACE Width = " + xMAX_HEIGHT_PREVIEW);

                int match_width = display.getWidth();
                int match_height = (nowPhoto_Height / nowPhoto_Width ) * display.getWidth();

                if (match_height > free_height) {
                    match_width = display.getWidth();
                    match_height = free_height;

                    Log.e("IMG", "      - match_width = " + match_width);
                    Log.e("IMG", "      - match_height = " + match_height);

                    slideView2(FrameImagePreview, FrameImagePreview.getHeight(), match_width, FrameImagePreview.getWidth(), match_height);
                }else {
                    Log.e("IMG", "      - match_width = " + match_width);
                    Log.e("IMG", "      - match_height = " + match_height);
                    slideView2(FrameImagePreview, FrameImagePreview.getHeight(), match_height, FrameImagePreview.getWidth(), match_width);
                }

                Log.e("IMG", "      - xMAX_HEIGHT_PREVIEW = " + xMAX_HEIGHT_PREVIEW);
                Log.e("IMG", "      - xMAX_WIDTH_PREVIEW = " + xMAX_WIDTH_PREVIEW);

                Log.e("IMG","   - newHeightFocusFrame = " + newHeightFocusFrame);
                Log.e("IMG","   - newWidthFocusFrame = " + newWidthFocusFrame);

                //slideView2(FrameImagePreview, FrameImagePreview.getHeight(), newHeightFocusFrame, FrameImagePreview.getWidth(), newWidthFocusFrame);
            }
 */
