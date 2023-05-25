package com.cekmitl.pdpacameracensor;

import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.OnLongClickListener;
import static com.cekmitl.pdpacameracensor.MainCameraActivity.slideView2;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.FACE_NET_MODEL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.cekmitl.pdpacameracensor.ImageEditor.BitmapEditor;
import com.cekmitl.pdpacameracensor.ImageEditor.DrawingView;
import com.cekmitl.pdpacameracensor.Process.AIProperties;
import com.cekmitl.pdpacameracensor.Process.Classifier;
import com.cekmitl.pdpacameracensor.Process.FaceRecogitionProcessor;
import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.Process.Score;
import com.cekmitl.pdpacameracensor.Process.YoloV5Classifier;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewStickerListSelectorAdapter;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    Bitmap inputBitmap = null;
    String intenPath = "";

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

    public LinearLayout layout_face_detect, layout_blur_radius, layout_stricker_option, layout_paint_option, button_blur_layout, button_stricker_layout, button_paint_layout;
    public ImageButton button_hide_face_detect;
    public ImageButton button_face_detect;
    public ImageButton button_blur;
    public ImageButton button_stricker;
    public ImageButton button_paint;
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
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = AIProperties.OBJ_DETECT_CONFIDENT;
    private ImageView imgPreView;
    public final int TF_OD_API_INPUT_SIZE = AIProperties.TF_OD_API_INPUT_SIZE;

    //Face Recog
    private FaceRecogitionProcessor faceRecognitionProcesser;
    private Interpreter faceNetInterpreter;

    //PersonDatabase
    PersonDatabase db = null;

    //Display
    public Display display;

    public int old_height_header_layout = 0;
    public int old_width_header_layout = 0;
    public int old_height_bottom_layout = 0;
    public int old_width_bottom_layout = 0;

    public static Bitmap nowPhotoPreview;                  //Now Photo Preview
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
    public ArrayList<Bitmap> bmp_images = new ArrayList<>();

    public ArrayList<String> facePosition = new ArrayList<>();   //LEFT / TOP / RIGHT / BOTTOM / X / Y / ID
    public int[] faceCensorState = new int[20];

    //Blur
    private double blurRadius;
    private int blur_percentage = 50;
    public TextView textview_blur_radius_value;
    public SeekBar seekbar_blur_radius;

    //PAINT  /////////////////////////////////
    private DrawingView drawView;
    private ImageButton currPaint;
    private float smallBrush, mediumBrush, largeBrush;

    //STICKER
    private GridViewStickerListSelectorAdapter adapterViewAndroid;
    Bitmap[] selectedSticker = new Bitmap[1];
    int[] idSticker = new int[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        setContentView(R.layout.activity_preview);

        //Reteive data
        Intent intent = getIntent();

        db = (PersonDatabase) intent.getSerializableExtra("person");
        //Personal Data
        if (db == null){

                try {
                    db = new PersonDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }

        //Face Recog
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, FACE_NET_MODEL), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        faceRecognitionProcesser = new FaceRecogitionProcessor(faceNetInterpreter);

        // END --- Face Recog ---------------------------------------------

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


        ImageView imagePreview = (ImageView) findViewById(R.id.ImagePreview);

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

        imagePreview.setOnClickListener(this);


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

        //STICKER /////////////////////////////////////////////////////////////////////////////////
        adapterViewAndroid = new GridViewStickerListSelectorAdapter(PreviewActivity.this, selectedSticker,idSticker,this);
        GridView androidGridView = findViewById(R.id.GridView_stricker);
        androidGridView.setAdapter(adapterViewAndroid);

        androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

//        androidGridView.setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        }).start();
        //STICKER /////////////////////////////////////////////////////////////////////////////////



        drawView = (DrawingView)findViewById(R.id.drawing);
        drawView.setBrushSize(mediumBrush);

        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));


        ImageButton drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        ImageButton eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        ImageButton newBtn = (ImageButton) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        ImageButton saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        // Get intent, action and MIME type
        String action = intent.getAction();
        String type = intent.getType();



        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                // Handle single image being sent
                String media_path = getRealPathFromURI(this, handleSendImage(intent));
                inputBitmap = BitmapFactory.decodeFile(new File(media_path).getAbsolutePath());
                intenPath = media_path;

                Log.e("reciveintent","path : " + intenPath);
            }
        }else {
            intenPath = intent.getStringExtra("key");
            inputBitmap = BitmapFactory.decodeFile(new File(intenPath).getAbsolutePath());

        }

        //Check image rotate
        ExifInterface ei = null;

        try {
            ei = new ExifInterface(intenPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ei == null){
            finish();
            return;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        //Bitmap rotatedBitmap = null;
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                inputBitmap = BitmapEditor.rotateImage(inputBitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                inputBitmap = BitmapEditor.rotateImage(inputBitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                inputBitmap = BitmapEditor.rotateImage(inputBitmap, 270);
                break;

//            case ExifInterface.ORIENTATION_NORMAL:
//            default:
//                inputBitmap = inputBitmap;
        }



        heightPhoto = inputBitmap.getHeight();
        widthPhoto = inputBitmap.getWidth();

        ImageButton btnSave = (ImageButton) findViewById(R.id.button_save_image);
        btnSave.setOnClickListener(view -> {

            TextView okay_text, cancel_text;
            Dialog dialog = new Dialog(PreviewActivity.this);

            dialog.setContentView(R.layout.dialog_layout);
            dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            okay_text = dialog.findViewById(R.id.button_dialog_ok);
            cancel_text = dialog.findViewById(R.id.button_dialog_cancel);

            okay_text.setOnClickListener(v -> {
                showHide_FocusView(false);
                Bitmap result_photo = BitmapEditor.loadBitmapFromView(FrameImagePreview);
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                Date date = new Date();
                BitmapEditor.saveImage(result_photo, formatter.format(date) + "");

                shareImageandText(result_photo);

                dialog.dismiss();

            });

            cancel_text.setOnClickListener(v -> dialog.dismiss());

            dialog.show();

        });
        ImageButton btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(view -> {
            //file.delete();
            finish();
        });

        try {
            String TF_OD_API_MODEL_FILE = AIProperties.TF_OD_API_MODEL_FILE;
            String TF_OD_API_LABELS_FILE = AIProperties.TF_OD_API_LABELS_FILE;
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        nowPhotoPreview = inputBitmap;
        //nowPhotoPreview = BitmapFactory.decodeResource(this.getResources(),  R.drawable.mmm);

        imgPreView = findViewById(R.id.ImagePreview);
        imgPreView.setImageBitmap(nowPhotoPreview);

        adjustImageDisplay(nowPhotoPreview);


        old_height_header_layout = BitmapEditor.getHeightOfView(HeadLayout);
        old_width_header_layout = 1080;

        old_height_bottom_layout = BitmapEditor.getHeightOfView(bottom_layout);
        old_width_bottom_layout = 1080;

        if(max_fram_focus_layout_height < fram_focus_layout.getLayoutParams().height){
            max_fram_focus_layout_height = fram_focus_layout.getLayoutParams().height;
        }


        xMAX_HEIGHT_PREVIEW = fram_focus_layout.getLayoutParams().height;
        xMAX_WIDTH_PREVIEW = fram_focus_layout.getLayoutParams().width;

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
                blurFaceX(progress);
            }
        });

        handleResult(true);
        blurFaceX(blur_percentage);



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        com.cekmitl.pdpacameracensor.MainCameraActivity.resumeThread();
//        com.cekmitl.pdpacameracensor.MainCameraActivity.isWorking = true;
        finish();
    }

    @Override
    public void onBackPressed() {
        if (false) {
            super.onBackPressed();
        }

    }

    @Override
    public void onClick(View view) {

        if (layout_face_detect_width_MAX < layout_face_detect.getWidth()){
            layout_face_detect_width_MAX = layout_face_detect.getWidth();
        }

        switch (view.getId()) {
            case R.id.GridView_stricker:
                onClickStickerItem();
                break;
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

                    findViewById(R.id.drawing).setVisibility(View.VISIBLE);
                    findViewById(R.id.drawing).setFocusable(true);
                    findViewById(R.id.paint_tools).setVisibility(View.VISIBLE);

                    findViewById(R.id.new_btn).setVisibility(View.VISIBLE);
                    findViewById(R.id.draw_btn).setVisibility(View.VISIBLE);
                    findViewById(R.id.erase_btn).setVisibility(View.VISIBLE);
                    findViewById(R.id.save_btn).setVisibility(View.VISIBLE);

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

                    findViewById(R.id.drawing).setVisibility(View.GONE);
                    findViewById(R.id.drawing).setFocusable(false);
                    findViewById(R.id.paint_tools).setVisibility(View.INVISIBLE);

                    findViewById(R.id.new_btn).setVisibility(View.INVISIBLE);
                    findViewById(R.id.draw_btn).setVisibility(View.INVISIBLE);
                    findViewById(R.id.erase_btn).setVisibility(View.INVISIBLE);
                    findViewById(R.id.save_btn).setVisibility(View.INVISIBLE);

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
            case R.id.button_delete:

                editMode(true);
                editMode(false);

                TextView okay_text, cancel_text;
                Dialog dialog = new Dialog(PreviewActivity.this);

                dialog.setContentView(R.layout.dialog_layout);
                dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                okay_text = dialog.findViewById(R.id.button_dialog_ok);
                cancel_text = dialog.findViewById(R.id.button_dialog_cancel);

                okay_text.setOnClickListener(v -> {
                    showHide_FocusView(false);
                    Bitmap result_photo = BitmapEditor.loadBitmapFromView(FrameImagePreview);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                    Date date = new Date();
                    BitmapEditor.saveImage(result_photo, formatter.format(date) + "");

                    shareImageandText(result_photo);

                    dialog.dismiss();
//                    finish();
//                makeText(PreviewActivity.this, "Save Complete", LENGTH_SHORT).show();
                });

                cancel_text.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
                break;
            case R.id.button_back:
                editMode(false);
                break;
        }

        if(view.getId() == R.id.draw_btn) {
            //draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);

            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(v -> {
                drawView.setBrushSize(smallBrush);
                drawView.setLastBrushSize(smallBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            });

            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(v -> {
                drawView.setBrushSize(mediumBrush);
                drawView.setLastBrushSize(mediumBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            });

            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(v -> {
                drawView.setBrushSize(largeBrush);
                drawView.setLastBrushSize(largeBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            });

            brushDialog.show();
        }else if(view.getId() == R.id.erase_btn) {
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);

            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(v -> {
                drawView.setErase(true);
                drawView.setBrushSize(smallBrush);
                brushDialog.dismiss();
            });

            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(v -> {
                drawView.setErase(true);
                drawView.setBrushSize(mediumBrush);
                brushDialog.dismiss();
            });

            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(v -> {
                drawView.setErase(true);
                drawView.setBrushSize(largeBrush);
                brushDialog.dismiss();
            });

            brushDialog.show();
        }else if(view.getId() == R.id.new_btn)
        {
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("new Drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", (dialog, which) -> {
                drawView.startNew();
                dialog.dismiss();
            });
            newDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            newDialog.show();

        }else if(view.getId() == R.id.save_btn)
        {
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save Drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", (dialog, which) -> {
                //save drawing
                //save drawing
                drawView.setDrawingCacheEnabled(true);
                String imageSaved = MediaStore.Images.Media.insertImage(
                        getContentResolver(), drawView.getDrawingCache(),
                        UUID.randomUUID().toString()+".png", "drawing");
                if(imageSaved != null)
                {
                    Toast savedToast = Toast.makeText(getApplicationContext(),"Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                    savedToast.show();
                }else
                {
                    Toast unsavedToast = Toast.makeText(getApplicationContext(),
                            "Oops! Image could not saved.", Toast.LENGTH_SHORT);
                    unsavedToast.show();
                }
                drawView.destroyDrawingCache();
            });
            saveDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            saveDialog.show();
        }
    }

    public void setFocusView(double X, double Y, double width, double height, String id, float xPos, float yPos, int type, Double blurRadius, boolean first,int detected_class) throws IOException {

        height2 = xMAX_HEIGHT_PREVIEW;
        width2 = xMAX_WIDTH_PREVIEW;

        Log.e("getResizedBitmap","height2X = " + width2 + "x" + height2);

        if (db == null){
            db = new PersonDatabase();
        }

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        x = (int) Math.round((float) (X * width2));
        y = (int) Math.round((float) (Y * height2));

        Bitmap bb = BitmapEditor.getResizedBitmap(nowPhotoPreview, width2, height2);
        Log.e("getResizedBitmap","bb = " + width2 + "x" + height2);
        Bitmap b = BitmapEditor.crop(bb, x, y, w, h);
        Log.e("getResizedBitmap","b = " + b.getWidth() + "x" + b.getHeight());
        bmp_images.add(b);

        float[] array1 = null;
        Log.d("DETECTCLASS", "setFocusView: "+detected_class);
        if (detected_class == 0){
            array1 = faceRecognitionProcesser.recognize(b);
        }

        TextView txt = new TextView(this);
        txt.setTextSize(12);
        txt.setTextColor(Color.WHITE);
        txt.setSingleLine(true);
        txt.setPadding(30, 10, 10, 10);
        txt.setGravity(Gravity.CENTER_VERTICAL|Gravity.BOTTOM);

        RelativeLayout layoutTOP = new RelativeLayout(PreviewActivity.this);
        layoutTOP.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout layoutInner = new LinearLayout(PreviewActivity.this);
        if (first && detected_class != 0){
            faceCensorState[Integer.parseInt(id)] = 1;
        }

        if(faceCensorState[Integer.parseInt(id)] == 0){
            layoutInner.setBackgroundResource(R.drawable.bg_face_frame_focus);
        }else {
            layoutInner.setBackgroundResource(R.drawable.bg_face_frame);
        }

        layoutInner.setTag("focus_frame" + id);
        layoutInner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout censor_layout = new LinearLayout(PreviewActivity.this);
        censor_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        censor_layout.setTag("" + id);

    if(first) {

        if (detected_class == 0 && array1 != null) {
//            makeText(this, "A", LENGTH_SHORT).show();
            Score score = db.recognize(array1);
            if (!(score == null)) {
                txt.setTextColor(Color.parseColor("#fbb040"));
                txt.setText(score.name);

                txt.setTag("name_label" + id);
                layoutInner.setBackgroundResource(R.drawable.bg_face_frame_focus);
                faceCensorState[Integer.parseInt(id)] = 0;
            } else {
                faceCensorState[Integer.parseInt(id)] = 1;
            }
        }
    }else{
        if (detected_class == 0 && array1 != null) {
//            makeText(this, "B", LENGTH_SHORT).show();
            Score score = db.recognize(array1);
            if (!(score == null)) {
                txt.setTextColor(Color.parseColor("#fbb040"));
                txt.setText(score.name);
                txt.setTag("name_label" + id);
//                layoutInner.setBackgroundResource(R.drawable.bg_face_frame_focus);
            }
        }
        if (faceCensorState[Integer.parseInt(id)] == 1){
            layoutInner.setBackgroundResource(R.drawable.bg_face_frame);
        }else{
            layoutInner.setBackgroundResource(R.drawable.bg_face_frame_focus);
        }
    }

        blurFaceX(blur_percentage);
        Log.e("faceCensorState","faceCensorState : " + Arrays.toString(faceCensorState));

        //------------------------------------------------------------------------------------------------------

        //focus_frame.setOnClickListener(view -> frameFocusOnClickListener(id));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params.height = h;
        params.width = w;
        params.setMargins(x + h, y, 0, 0);

        //-------------------------------------------------------------------
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(x, y, 0, 0);

        layoutTOP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(faceCensorState[Integer.parseInt(id)] == 1){
                    faceCensorState[Integer.parseInt(id)] = 0;
                    layoutInner.setBackgroundResource(R.drawable.bg_face_frame_focus);
                    txt.setTextColor(Color.parseColor("#fbb040"));
                    censor_layout.setAlpha(.0f);
                }else {
                    faceCensorState[Integer.parseInt(id)] = 1;
//                    layoutInner.setVisibility(View.VISIBLE);
                    layoutInner.setBackgroundResource(R.drawable.bg_face_frame);
                    txt.setTextColor(Color.parseColor("#FFFFFF"));
                    censor_layout.setAlpha(1f);
                }
            }
        });

        layoutTOP.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    frameFocusOnLongClickListener(id, b);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Reteive data
                Intent intent = getIntent();
                db = (PersonDatabase) intent.getSerializableExtra("person");
                //Personal Data
                if (db == null){
                    new Thread(() -> {
                        try {
                            db = new PersonDatabase();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }


                return false;
            }
        });

        layoutTOP.addView(censor_layout);
        layoutTOP.addView(layoutInner);

        fram_focus_layout.addView(layoutTOP, params1);
        fram_focus_layout.addView(txt, params1);
    }

    public void setFocusViewOther(double X, double Y, double width, double height, String id, float xPos, float yPos, int type, Double blurRadius, boolean first,int detected_class) throws IOException {

        height2 = xMAX_HEIGHT_PREVIEW;
        width2 = xMAX_WIDTH_PREVIEW;

        Log.e("getResizedBitmap","height2X = " + width2 + "x" + height2);

        if (db == null){
            db = new PersonDatabase();
        }

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        x = (int) Math.round((float) (X * width2));
        y = (int) Math.round((float) (Y * height2));

        Bitmap bb = BitmapEditor.getResizedBitmap(nowPhotoPreview, width2, height2);
        Log.e("getResizedBitmap","bb = " + width2 + "x" + height2);
        Bitmap b = BitmapEditor.crop(bb, x, y, w, h);
        Log.e("getResizedBitmap","b = " + b.getWidth() + "x" + b.getHeight());

        RelativeLayout layoutTOP = new RelativeLayout(PreviewActivity.this);
        layoutTOP.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout layoutInner = new LinearLayout(PreviewActivity.this);
        layoutInner.setBackgroundResource(R.drawable.bg_face_frame);

        layoutInner.setTag("focus_frame" + id);
        layoutInner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout censor_layout = new LinearLayout(PreviewActivity.this);
        censor_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        censor_layout.setTag("" + id);

        blurFaceX(blur_percentage);
        Log.e("faceCensorState","faceCensorState : " + Arrays.toString(faceCensorState));

        //------------------------------------------------------------------------------------------------------

        //focus_frame.setOnClickListener(view -> frameFocusOnClickListener(id));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params.height = h;
        params.width = w;
        params.setMargins(x + h, y, 0, 0);

        //-------------------------------------------------------------------
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(x, y, 0, 0);

        layoutTOP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(faceCensorState[Integer.parseInt(id)] == 1){
                    faceCensorState[Integer.parseInt(id)] = 0;
                    layoutInner.setBackgroundResource(R.drawable.bg_face_frame_focus);
                    censor_layout.setAlpha(.0f);
                }else {
                    faceCensorState[Integer.parseInt(id)] = 1;
//                    layoutInner.setVisibility(View.VISIBLE);
                    layoutInner.setBackgroundResource(R.drawable.bg_face_frame);
                    censor_layout.setAlpha(1f);
                }
            }
        });


        layoutTOP.addView(censor_layout);
        layoutTOP.addView(layoutInner);

        fram_focus_layout.addView(layoutTOP, params1);
    }

    public void frameFocusOnLongClickListener(String id, Bitmap bitmap) throws IOException {
        String[] strName = (String[]) db.getPersonData().get(0);

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(PreviewActivity.this);
        builder.setTitle("Choose an person");

        // add a list
        builder.setItems(strName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int num = strName.length;
                String ps = String.valueOf(strName[which]);
                db.add_newPerson_folder(ps);
                float[] arr = faceRecognitionProcesser.recognize(bitmap);

                try {
                    db.save2file(arr,ps);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handleResult(false);
                blurFaceX(blur_percentage);
            }
        });

        // set the neutral button to do some actions
//        builder.setNeutralButton("NEW", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                showAlertDialogButtonClicked(bitmap);
//
//                handleResult(false);
//                blurFaceX(blur_percentage);
//            }
//        });

        // set the positive button to do some actions
        builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showAlertDialogButtonClicked(Bitmap bitmap) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input name");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_save_anme, null);
        builder.setView(customLayout);

        // send data from the AlertDialog to the Activity
        EditText editText = customLayout.findViewById(R.id.editText);


        String ps = editText.getText().toString();
        ps = "TEST";
        if (db == null){
            try {
                db = new PersonDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        db.add_newPerson_folder(ps);
        float[] arr = faceRecognitionProcesser.recognize(bitmap);

        try {
            db.save2file(arr,ps);
        } catch (IOException e) {
            e.printStackTrace();
        }

        handleResult(false);
        blurFaceX(blur_percentage);
    }


    @Override
    protected void onResume() {
        super.onResume();
        com.cekmitl.pdpacameracensor.MainCameraActivity.pauseThread();
    }

    @Override
    public void run() {
        //แสดงใบหน้าที่สามารถตรวจจับได้
        listView = (LinearLayout) findViewById(R.id.listView);
        for (int i = 0; i < bmp_images.size(); i++){
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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

    //จัดการกับ Label คำตอบ
    private void handleResult(boolean initStart) {

        clearFocus();

        bmp_images.removeAll(bmp_images);
        if (null != listView && listView.getChildCount() > 0) {
            listView.removeViews(0, listView.getChildCount());
        }

        if (nowPhotoPreview == null) {
//            makeText(this, "ERROR", LENGTH_SHORT).show();
        } else {
            List<Classifier.Recognition> results = detector.recognizeImage(BitmapEditor.getResizedBitmap(nowPhotoPreview, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE));
            int i = 0;
            facePosition = new ArrayList<String>();

            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                if(result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    //                           X - Y - Width - Height
                    if (result.getDetectedClass() == 0 || result.getDetectedClass() == 1 || result.getDetectedClass() == 2) {
                        try {
                            setFocusView(location.left, location.top, location.right, location.bottom, i + "", result.getX(), result.getY(), 1, 1d, initStart,result.getDetectedClass());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        facePosition.add(location.left + "/" + location.top + "/" + location.right + "/" + location.bottom + "/" + result.getX() + "/" + result.getY() + "/" + i);
                        i++;
                    }

                }
            }
            showHide_FocusView(false);
            run();
        }
    }

    public void adjustImageDisplay(Bitmap bitmap){
        if (bitmap != null){
            Display display = getWindowManager().getDefaultDisplay();
            int display_width = display.getWidth();
            int display_height = display.getHeight();

            if (nowPhotoPreview.getWidth() > nowPhotoPreview.getHeight()){                                      //Landscape
                float f = ((float) display_width / (float)nowPhotoPreview.getWidth());

                int fW = Math.round( f * (float)nowPhotoPreview.getWidth());
                int fH = Math.round( f * (float)nowPhotoPreview.getHeight());

                global_img_width = fW;
                global_img_height = fH;

                fram_focus_layout.getLayoutParams().height = fH;
                fram_focus_layout.getLayoutParams().width = fW;

                imgPreView.setImageBitmap(BitmapEditor.getResizedBitmap(nowPhotoPreview, fW, fH));
            }else {                                                                                             //Portrait

                //if (nowPhotoPreview.getWidth() < nowPhotoPreview.getHeight())
                float f = ((float) display_width / (float)nowPhotoPreview.getWidth());

                int fW = Math.round( f * (float)nowPhotoPreview.getWidth());
                int fH = Math.round( f * (float)nowPhotoPreview.getHeight());


                global_img_width = fW;
                global_img_height = fH;

                Log.e("adj", "global_img_width : " + global_img_width);
                Log.e("adj", "global_img_height : " + global_img_height);

                fram_focus_layout.getLayoutParams().height = fH;
                fram_focus_layout.getLayoutParams().width = fW;

                Log.e("adj", "fram_focus_layout H : " + fram_focus_layout);
                Log.e("adj", "fram_focus_layout W : " + global_img_height);

                imgPreView.setImageBitmap(BitmapEditor.getResizedBitmap(nowPhotoPreview, fW, fH));

            }
        }
    }

    public void editMode(boolean b){
        if (layout_face_detect_width_MAX < layout_face_detect.getWidth()){
            layout_face_detect_width_MAX = layout_face_detect.getWidth();
        }

        if (b){

            menu_bar.setVisibility(View.GONE);
            HeadLayout2.setVisibility(View.GONE);
            Touch_ImagePreview.setVisibility(View.GONE);

            bottom_layout.setVisibility(View.VISIBLE);
            HeadLayout.setVisibility(View.VISIBLE);
            button_bar.setVisibility(View.VISIBLE);
            fram_focus_layout.setVisibility(View.VISIBLE);

            fram_focus_layout.startAnimation(animFadeIn);

            int head_layout_height = BitmapEditor.getHeightOfView(HeadLayout);
            int bottom_layout_height = BitmapEditor.getHeightOfView(bottom_layout);
            int main_layout_height = main_layout.getHeight();

            Display display = getWindowManager().getDefaultDisplay();

            int free_height = main_layout_height - head_layout_height - bottom_layout_height;
            slideView2(FrameImagePreview_TOP, FrameImagePreview_TOP.getHeight(), free_height, FrameImagePreview_TOP.getWidth(), FrameImagePreview_TOP.getWidth());

            if(FrameImagePreview.getHeight() > MAX_HEIGHT_PREVIEW){
                MAX_HEIGHT_PREVIEW = FrameImagePreview.getHeight();
            }

            if(FrameImagePreview.getWidth() > MAX_WIDTH_PREVIEW){
                MAX_WIDTH_PREVIEW = FrameImagePreview.getWidth();
            }

            nowPhoto_Width = nowPhotoPreview.getWidth();
            nowPhoto_Height = nowPhotoPreview.getHeight();

            int match_height = (nowPhotoPreview.getHeight() / nowPhotoPreview.getWidth() ) * display.getWidth();

            if (imgPreView.getHeight() > free_height) {                                                       //Landscape
                int newHeight = free_height;
                int newWidth = free_height * 1080 / imgPreView.getHeight();

                slideView2(FrameImagePreview, FrameImagePreview.getHeight(), newHeight, FrameImagePreview.getWidth(), newWidth);
                slideView2(drawView, FrameImagePreview.getHeight(), newHeight, FrameImagePreview.getWidth(), newWidth);
                nowPhotoPreview = BitmapEditor.getResizedBitmap(nowPhotoPreview, newWidth,newHeight);

                xMAX_HEIGHT_PREVIEW = newHeight;
                xMAX_WIDTH_PREVIEW = newWidth;
            }else if (match_height < free_height){                                                                                 //Portaite
                slideView2(FrameImagePreview, FrameImagePreview.getHeight(), imgPreView.getHeight(), FrameImagePreview.getWidth(), display.getWidth());
                slideView2(drawView, FrameImagePreview.getHeight(), imgPreView.getHeight(), FrameImagePreview.getWidth(), display.getWidth());
                //drawView
                nowPhotoPreview = BitmapEditor.getResizedBitmap(nowPhotoPreview, display.getWidth(),imgPreView.getHeight());

                xMAX_HEIGHT_PREVIEW = imgPreView.getHeight();
                xMAX_WIDTH_PREVIEW = display.getWidth();
            }

            slideView2(bottom_layout, 0, old_height_bottom_layout, old_width_bottom_layout, old_width_bottom_layout);
            slideView2(HeadLayout, 0, old_height_header_layout, old_width_bottom_layout, old_width_header_layout);

            Log.e("adj", "FrameImagePreview.getHeight() : " + FrameImagePreview.getHeight());
            Log.e("adj", "FrameImagePreview.getWidth() : " + FrameImagePreview.getWidth());

            handleResult(false);
            blurFaceX(blur_percentage);

            state_Edite_Mode = false;
            menu_bar.setVisibility(View.GONE);
            state_ImagePreview = true;

            showHide_FocusView(true);

        }else {
            //Exit Edit mode
            Log.e("adj", "global_img_width : " + global_img_width);
            Log.e("adj", "global_img_height : " + global_img_height);

            showHide_FocusView(false);

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
            }

            if(fram_focus_layout.getWidth() > MAX_WIDTH_PREVIEW){
                MAX_WIDTH_PREVIEW = fram_focus_layout.getWidth();
            }

            //1080x2408
            nowPhoto_Width = nowPhotoPreview.getWidth();
            nowPhoto_Height = nowPhotoPreview.getHeight();

            slideView2(FrameImagePreview_TOP, FrameImagePreview_TOP.getHeight(), MAX_HEIGHT_PREVIEW, FrameImagePreview_TOP.getWidth(), MAX_WIDTH_PREVIEW);

            slideView2(FrameImagePreview, FrameImagePreview.getLayoutParams().height, MAX_HEIGHT_PREVIEW, FrameImagePreview.getWidth(), MAX_WIDTH_PREVIEW);

            slideView2(drawView, FrameImagePreview.getLayoutParams().height, MAX_HEIGHT_PREVIEW, FrameImagePreview.getWidth(), MAX_WIDTH_PREVIEW);
            button_bar.setVisibility(View.GONE);

            xMAX_HEIGHT_PREVIEW = max_fram_focus_layout_height;
            xMAX_WIDTH_PREVIEW = display.getWidth();

            handleResult(false);
            blurFaceX(blur_percentage);

            state_Edite_Mode = true;

            nowPhotoPreview = BitmapEditor.getResizedBitmap(nowPhotoPreview, xMAX_WIDTH_PREVIEW, xMAX_HEIGHT_PREVIEW);
        }
    }

    // PAINT ////////////////////
    public void paintClicked(View view) {
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());
        //use chosen color
        if(view!=currPaint) {
            //update color
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            //reflect in the UI
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton)view;
        }
    }

    public void blurFaceX(int progress){
        blurRadius = (100 - progress ) * Math.pow(10,-3);
        blur_percentage = progress;

        for (int i = 0; i < facePosition.size(); i++){

            String[] a = facePosition.get(i).split("/");

            height2 = xMAX_HEIGHT_PREVIEW;
            width2 = xMAX_WIDTH_PREVIEW;

            //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
            int h = (int) Math.round((float) ((2 * (Double.parseDouble(a[3]) - Float.parseFloat(a[5]))) * height2));
            int w = (int) Math.round((float) ((2 * (Double.parseDouble(a[2]) - Float.parseFloat(a[4]))) * width2));
            int x = (int) Math.round((float) (Double.parseDouble(a[0]) * width2));
            int y = (int) Math.round((float) (Double.parseDouble(a[1]) * height2));

            Bitmap bb = BitmapEditor.getResizedBitmap(nowPhotoPreview, width2, height2);
            Bitmap b = BitmapEditor.crop(bb, x, y, w, h);

            LinearLayout censor_layout = fram_focus_layout.findViewWithTag("" + i);

            int intID = Integer.parseInt((String) censor_layout.getTag());

                if (blurRadius == 0.1 || faceCensorState[i] == 0){
                    censor_layout.setBackground(new BitmapDrawable(getResources(), b));
                }else if(faceCensorState[i] == 1){
                    censor_layout.setBackground(new BitmapDrawable(getResources(), BitmapEditor.getMosaicsBitmap(b, blurRadius)));
                }
        }

        Log.e("faceCensorState","faceCensorState : " + Arrays.toString(faceCensorState));
    }

    public void onClickStickerItem(){
        selectedSticker[0] = adapterViewAndroid.getSelectedSticker();
        Drawable sticker = new BitmapDrawable(selectedSticker[0]);

        for (int i = 0; i < facePosition.size(); i++){
            LinearLayout censor_layout = fram_focus_layout.findViewWithTag("" + i);
            censor_layout.setBackgroundDrawable(sticker);
        }

    }

    public void showHide_FocusView(boolean b){
        if(b){
            //Show frame focus
            for (int i = 0; i < facePosition.size(); i++){
                try {
                    LinearLayout layoutinner = fram_focus_layout.findViewWithTag("focus_frame" + i);
                    TextView name_label = fram_focus_layout.findViewWithTag("name_label" + i);

                    layoutinner.setVisibility(View.VISIBLE);
                    name_label.setVisibility(View.VISIBLE);
                }catch (NullPointerException e){

                }
            }
        }else {
            //Hide frame focus
            for (int i = 0; i < facePosition.size(); i++){

                try {
                    LinearLayout layoutinner = fram_focus_layout.findViewWithTag("focus_frame" + i);
                    TextView name_label = fram_focus_layout.findViewWithTag("name_label" + i);

                    layoutinner.setVisibility(View.GONE);
                    name_label.setVisibility(View.GONE);
                }catch (NullPointerException e){

                }

            }
        }
    }

    private void shareImageandText(Bitmap bitmap) {
        Uri uri = getmageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri);
//
//        // adding text to share
//        intent.putExtra(Intent.EXTRA_TEXT, "Sharing Image");
//
//        // Add subject Here
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");

        // setting type to image
        intent.setType("image/png");

        // calling startactivity() to share
        startActivity(Intent.createChooser(intent, "Share Via"));
//        finish();
    }

    private Uri getmageToShare(Bitmap bitmap) {
        File imagefolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagefolder.mkdirs();
            File file = new File(imagefolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, "com.anni.shareimage.fileprovider", file);
        } catch (Exception e) {

        }
        return uri;
    }

    public static Uri handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
        return imageUri;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void saveAndSharePhoto(){

    }



}