package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.makeText;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_INPUT_SIZE;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_LABELS_FILE;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_MODEL_FILE;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.cekmitl.pdpacameracensor.ImageEditor.BitmapEditor;
import com.cekmitl.pdpacameracensor.Process.AIProperties;
import com.cekmitl.pdpacameracensor.Process.Classifier;
import com.cekmitl.pdpacameracensor.Process.FaceRecogitionProcessor;
import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.Process.YoloV5Classifier;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewFaceSelectorAdapter;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddNewFaceActivity extends AppCompatActivity implements View.OnClickListener{
    public LinearLayout ll;

    public ArrayList<Bitmap> bmp_images = new ArrayList<Bitmap>();
    public ArrayList<String> facePosition = new ArrayList<String>();

    //DETECT FACE
    private Classifier detector;

    //DETECT FACE
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;


    //Grid View
    public String[] str = new String[20];
    public ArrayList<Bitmap> faceSelect = new ArrayList<Bitmap>();
    public ArrayList<Bitmap> insertFaceSelect = new ArrayList<Bitmap>();

    GridView androidGridView;
    public Bitmap[] bitmapArray = new Bitmap[20];

    //Intent
    public String psName = null;


    PersonDatabase db;

    private FaceRecogitionProcessor faceRecognitionProcesser;
    private Interpreter faceNetInterpreter;

    //Grid View
    GridViewFaceSelectorAdapter adapterViewAndroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_face);

        //ลบ Action Bar ออก
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        ll = findViewById(R.id.ll);
        //component

        try {
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        String str = intent.getStringExtra("key");
        psName = intent.getStringExtra("psName");

        if (str != null){
            makeText(this, "Intent STR != NULL", Toast.LENGTH_SHORT).show();
            int num_pic = Integer.parseInt(intent.getStringExtra("temp_num"));
            ArrayList<Bitmap> recive_bitmap = new ArrayList<Bitmap>();
            File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            String path = DOC_PATH + "/temp/";
            for(int i = 0; i < num_pic; i++){
                File imgFile = new  File(path + i + ".png");
                if(imgFile.exists()){
                    cropFaceProcess(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                }
            }

        }else {
            openGalley();
        }

        try {
            if (db == null){
                db = new PersonDatabase(1);
            }
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, AIProperties.FACE_NET_MODEL), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }
        faceRecognitionProcesser = new FaceRecogitionProcessor(faceNetInterpreter);

        TextView save_button = findViewById(R.id.button_ok);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(psName != null){

                    String ps = psName;
                    db.add_newPerson_folder(ps);
                    faceSelect = adapterViewAndroid.getFaceSelected();

                    int num = faceSelect.size();
                    for (int i = 0; i < num; i++) {
                        float[] arr = faceRecognitionProcesser.recognize(faceSelect.get(i));

                        try {
                            db.save2file(arr,ps);
                            Log.e("SAVINGIMAGE","SAVE NUM = " + i);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    finish();
                }else {
                    showAlertDialogButtonClicked();
                }
            }
        });

    }

    public void showAlertDialogButtonClicked() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input name");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_save_anme, null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton("OK", (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            EditText editText = customLayout.findViewById(R.id.editText);
            db.add_newPerson_folder(editText.getText().toString());
            makeText(getApplicationContext(), String.valueOf(editText.getText()) , Toast.LENGTH_SHORT).show();
            int num = adapterViewAndroid.getCount();

            Log.d("NUMIMAGESELECT", "showAlertDialogButtonClicked: " + num);

            String ps = editText.getText().toString();

            float[][] personList = new float[num][192];

            faceSelect = adapterViewAndroid.getFaceSelected();
            num = faceSelect.size();

            Log.e("GridSelecter","");
            Log.e("GridSelecter","faceSelect = adapterViewAndroid.getFaceSelected() ");
            Log.e("SAVINGIMAGE","faceSelect = " + faceSelect.size());

            Log.e("SAVINGIMAGE","TO SAVE = " + num);
            Bitmap image_toSave = null;
            for (int i = 0; i < num; i++) {
                float[] arr = faceRecognitionProcesser.recognize(faceSelect.get(i));
                if (i == 0){
                    image_toSave = faceSelect.get(i);
                }
                Log.e("GridSelecter","faceSelect.get(i) = " + faceSelect.get(i).toString());


                try {
                    db.save2file(arr,ps);
                    Log.e("SAVINGIMAGE","SAVE NUM = " + i);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


            db.save_image(image_toSave,ps);
            makeText(getApplicationContext(), "Save Complete! " + num + " Images", Toast.LENGTH_SHORT).show();

            //getFragmentManager().beginTransaction().replace(R.id.navigation_home,FaceRecognitionFragment.newInstance()).commit();

            finish();
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Thread detectThread;
    private static final Object mPauseLock = new Object();
    private static boolean mPaused = false;




    int countBitmap(){
        return faceSelect.size();
    }

    private void openGalley() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Selcet Picture"),123);
    }

    @Override
    public void onClick(View view) {

    }
    int j = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 123 && resultCode == RESULT_OK){
            if(data.getClipData()!=null){
                int x = data.getClipData().getItemCount();
                for(int i = 0; i < x; i++){

                    Uri imgUri = Uri.parse(String.valueOf(data.getClipData().getItemAt(i).getUri()));

                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver() , Uri.parse(String.valueOf(imgUri)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Bitmap bitmap1 = BitmapEditor.getResizedBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE);
                    List<Classifier.Recognition> results = detector.recognizeImage(bitmap1);

                    for (final Classifier.Recognition result : results) {

                        final RectF location = result.getLocation();
                        //                           X - Y - Width - Height
                        if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                            Log.d("LOCATION", String.valueOf(location.left)+" " +String.valueOf(location.top)+" " +String.valueOf(location.right)+" " +String.valueOf(location.bottom));

                            Bitmap m = cropBitmap(location.left, location.top, location.right, location.bottom,  result.getX(), result.getY(), bitmap1,bitmap,100);
                            if (m!=null){
                                ImageView img = new ImageView(getApplication());
                                img.setImageBitmap(m);
                                ll.addView(img);
                                //faceSelect.add(j, m);
                                insertFaceSelect.add(m);
                                Log.e("GRIDVIEW","faceSelect[" + j + "] = " + m.toString());
                                ++j;
                            }

                        }
                    }
                }

            }else if(data.getData()!=null){
                String imgurl = data.getData().getPath();
                //list.add(Uri.parse(imgurl));
            }

            adapterViewAndroid = new GridViewFaceSelectorAdapter(AddNewFaceActivity.this, insertFaceSelect);
            androidGridView = findViewById(R.id.grid_view);
            androidGridView.setAdapter(adapterViewAndroid);

        }else {
            finish();
        }
    }


    public void cropFaceProcess(Bitmap bitmap){
        if (bitmap != null){
            Bitmap bitmap1 = BitmapEditor.getResizedBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE);
            List<Classifier.Recognition> results = detector.recognizeImage(bitmap1);

            for (final Classifier.Recognition result : results) {

                final RectF location = result.getLocation();
                //                           X - Y - Width - Height
                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                    Log.d("LOCATION", String.valueOf(location.left)+" " +String.valueOf(location.top)+" " +String.valueOf(location.right)+" " +String.valueOf(location.bottom));

                    Bitmap m = cropBitmap(location.left, location.top, location.right, location.bottom,  result.getX(), result.getY(), bitmap1,bitmap,100);
                    if (m!= null){
                        ImageView img = new ImageView(getApplication());
                        img.setImageBitmap(m);
                        ll.addView(img);
                        insertFaceSelect.add(m);
                        Log.e("GRIDVIEW","faceSelect[" + j + "] = " + m.toString());
                        ++j;
                    }

                }
            }

            adapterViewAndroid = new GridViewFaceSelectorAdapter(AddNewFaceActivity.this, insertFaceSelect);
            androidGridView = findViewById(R.id.grid_view);
            androidGridView.setAdapter(adapterViewAndroid);
        }

    }


    public static Bitmap cropBitmap(double X, double Y, double width, double height, float xPos, float yPos, Bitmap bm, Bitmap realBitmap, int persen){
        int width2 = realBitmap.getWidth();
        int height2 = realBitmap.getHeight();

//        int width2 = 1080;
//        int height2 = 1080;
        persen = 0;
        //persen = Math.round(persen / 2);

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        int h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        int w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        int x = (int) Math.round((float) (X * width2));
        int y = (int) Math.round((float) (Y * height2));

        Bitmap bb = BitmapEditor.getResizedBitmap(bm, width2, height2);
        //Bitmap b = BitmapEditor.crop(bb, x, y, w + persen, h + persen);

        Bitmap b = BitmapEditor.crop(bb, x, y, w, h);

        return b;
    }

}