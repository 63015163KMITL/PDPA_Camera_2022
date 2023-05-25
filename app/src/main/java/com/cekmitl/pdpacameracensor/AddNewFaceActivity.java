package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.makeText;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_INPUT_SIZE;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_LABELS_FILE;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_MODEL_FILE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.cekmitl.pdpacameracensor.Process.Utils;
import com.cekmitl.pdpacameracensor.Process.YoloV5Classifier;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewFaceSelectorAdapter;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddNewFaceActivity extends AppCompatActivity implements View.OnClickListener{
    public LinearLayout ll;

    //DETECT FACE
    private Classifier detector;

    //DETECT FACE
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;

    //Intent
    Intent intent;

    public ArrayList<Bitmap> faceSelect = new ArrayList<Bitmap>();
    public ArrayList<Bitmap> insertFaceSelect = new ArrayList<Bitmap>();

    GridView androidGridView;

    //Intent
    public String psName = null;


    PersonDatabase db;

    //Intent
    public Boolean check;

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

        intent = getIntent();
        String str = intent.getStringExtra("key");
        psName = intent.getStringExtra("psName");
        //check
        check = intent.getBooleanExtra("check", false);

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
                db = new PersonDatabase();
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

                Boolean check = intent.getBooleanExtra("check", false);
                String psName = intent.getStringExtra("psName");

                if(psName != null){

                    db.add_newPerson_folder(psName);
                    makeText(getApplicationContext(), String.valueOf(psName) , Toast.LENGTH_SHORT).show();
                    int num = adapterViewAndroid.getCount();

                    float[][] personList = new float[num][192];
                    faceSelect = adapterViewAndroid.getFaceSelected();
                    num = faceSelect.size();
                    Bitmap image_toSave = null;
                    float n_detect_face_true = 0f;
                    for (int i = 0; i < num; i++) {

                        float[] arr = faceRecognitionProcesser.recognize(faceSelect.get(i));

                        if (check){
                            if(db.test(arr,psName)){
                                n_detect_face_true++;
                            }
                        }else {

                            if (i == 0){
                                image_toSave = faceSelect.get(i);
                            }

                            try {
                                db.save2file(arr,psName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            finish();
                            db.save_image(image_toSave,psName);

                        }
                    }

                    if (check){
                        // set the custom layout
                        AlertDialog.Builder cBuilder = new AlertDialog.Builder(AddNewFaceActivity.this);
                        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_face_check_accuracy, null);
                        cBuilder.setView(customLayout);

                        ImageView psImageView = customLayout.findViewById(R.id.psImageView);

                        psImageView.setImageBitmap(PersonDatabase.getDisplay(psName));

                        EditText edt_name = customLayout.findViewById(R.id.edittext_name_profile);
                        edt_name.setText(psName);
                        edt_name.setSelection(edt_name.getText().length());

                        TextView textview_input_image = customLayout.findViewById(R.id.textview_input_image);
                        textview_input_image.setText("Input image : " + faceSelect.size());

                        TextView textview_correct_prediction = customLayout.findViewById(R.id.textview_correct_prediction);
                        textview_correct_prediction.setText("Correct prediction : " + Math.round(n_detect_face_true));

                        TextView textview_accuracy = customLayout.findViewById(R.id.textview_accuracy);
                        textview_accuracy.setText("Accuracy : " + Math.round((n_detect_face_true / 20) * 100)+ "%");


                        // create and show the alert dialog
                        AlertDialog dialog = cBuilder.create();
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();

                        Button button_ok = customLayout.findViewById(R.id.button_ok);
                        button_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                    }


                }else {
                    try {
                        showAlertDialogButtonClicked();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void showAlertDialogButtonClicked() throws IOException {
        String[] strName = (String[]) db.getPersonData().get(0);

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input name");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_save_anme, null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton("OK", (dialog, which) -> {

            Boolean check = intent.getBooleanExtra("check", false);
            String psName = intent.getStringExtra("psName");

            // send data from the AlertDialog to the Activity
            EditText editText = customLayout.findViewById(R.id.editText);
            String inputName = editText.getText().toString();

            if (Arrays.asList(strName).contains(inputName)) {
                AlertDialog.Builder sBuilder =
                        new AlertDialog.Builder(AddNewFaceActivity.this);
                sBuilder.setTitle("Warning");
                sBuilder.setMessage("The name entered is a duplicate of an existing name. Do you need to re-record?");

                sBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.add_newPerson_folder(editText.getText().toString());
                        makeText(getApplicationContext(), String.valueOf(editText.getText()) , Toast.LENGTH_SHORT).show();
                        int num = adapterViewAndroid.getCount();

                        String ps = "";

                        if (check){
                            editText.setText(psName);
                            ps = psName;
                        }else {
                            ps = editText.getText().toString();
                        }

                        faceSelect = adapterViewAndroid.getFaceSelected();
                        num = faceSelect.size();
                        Bitmap image_toSave = null;
                        for (int j = 0; j < num; j++) {
                            float[] arr = faceRecognitionProcesser.recognize(faceSelect.get(j));

                            if (j == 0){
                                image_toSave = faceSelect.get(j);
                            }

                            try {
                                db.save2file(arr,ps);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        db.save_image(image_toSave,ps);
                        makeText(getApplicationContext(), "Save Complete! " + num + " Images", Toast.LENGTH_SHORT).show();
                        //getFragmentManager().beginTransaction().replace(R.id.navigation_home,FaceRecognitionFragment.newInstance()).commit();
                        finish();
                    }
                });

                sBuilder.setNegativeButton("Rename", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            showAlertDialogButtonClicked();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                sBuilder.show();

            }else {
                db.add_newPerson_folder(editText.getText().toString());
                    makeText(getApplicationContext(), String.valueOf(editText.getText()) , Toast.LENGTH_SHORT).show();
                    int num = adapterViewAndroid.getCount();

                    String ps = "";

                    if (check){
                        editText.setText(psName);
                        ps = psName;
                    }else {
                        ps = editText.getText().toString();
                    }

                    float[][] personList = new float[num][192];

                    faceSelect = adapterViewAndroid.getFaceSelected();
                    num = faceSelect.size();
                    Bitmap image_toSave = null;
                    float n_detect_face_true = 0f;
                    for (int i = 0; i < num; i++) {
                        float[] arr = faceRecognitionProcesser.recognize(faceSelect.get(j));

                        if (check){
                            if(db.test(arr,psName)){
                                n_detect_face_true++;
                            }
                        }

                        if (i == 0){
                            image_toSave = faceSelect.get(i);
                        }

                        try {
                            db.save2file(arr,ps);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    db.save_image(image_toSave,ps);
                    finish();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Utils.deleteFolder(AIProperties.DOC_PATH+"/temp");
                }
            }).start();
        });

//        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.deleteFolder(AIProperties.DOC_PATH+"/temp");
            }
        }).start();

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
                            //X - Y - Width - Height
                            if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                                Log.d("LOCATION", String.valueOf(location.left)+" " +String.valueOf(location.top)+" " +String.valueOf(location.right)+" " +String.valueOf(location.bottom));

                                    Bitmap m = cropBitmap(location.left, location.top, location.right, location.bottom, result.getX(), result.getY(), bitmap1, bitmap, 100);
                                    if (m != null) {
                                        ImageView img = new ImageView(getApplication());
                                        img.setImageBitmap(m);
                                        ll.addView(img);
                                        insertFaceSelect.add(m);
                                        Log.e("GRIDVIEW", "faceSelect[" + j + "] = " + m.toString());
                                        ++j;
                                }
                        }
                    }
                }

            }else if(data.getData() != null){
                String imgurl = data.getData().getPath();
            }

            adapterViewAndroid = new GridViewFaceSelectorAdapter(AddNewFaceActivity.this, insertFaceSelect, true);
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
            int process_image = 0;
            for (final Classifier.Recognition result : results) {
                if (process_image == 0){
                    final RectF location = result.getLocation();
                    //                           X - Y - Width - Height
                    if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                        Log.d("LOCATION", String.valueOf(location.left)+" " + String.valueOf(location.top) + " " + String.valueOf(location.right) + " " + String.valueOf(location.bottom));

                        Bitmap m = cropBitmap(location.left, location.top, location.right, location.bottom,  result.getX(), result.getY(), bitmap1,bitmap,100);
                        if (m!= null){
                            ImageView img = new ImageView(getApplication());
                            img.setImageBitmap(m);
                            ll.addView(img);
                            insertFaceSelect.add(m);
                            Log.e("GRIDVIEW","faceSelect[" + j + "] = " + m.toString());
                            ++j;
                            process_image += 1;
                        }
                    }
                }
            }

            adapterViewAndroid = new GridViewFaceSelectorAdapter(AddNewFaceActivity.this, insertFaceSelect, true);
            androidGridView = findViewById(R.id.grid_view);
            androidGridView.setAdapter(adapterViewAndroid);
        }

    }


    public static Bitmap cropBitmap(double X, double Y, double width, double height, float xPos, float yPos, Bitmap bm, Bitmap realBitmap, int persen){
        int width2 = realBitmap.getWidth();
        int height2 = realBitmap.getHeight();

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