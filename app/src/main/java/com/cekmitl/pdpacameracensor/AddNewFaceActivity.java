package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.makeText;

import static java.lang.Integer.parseInt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

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
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private Bitmap largeIcon;
    private ImageView imageView, ImagePreview, imgPreView;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    //Grid View
    public String[] str = new String[20];
    public Bitmap[] faceSelect = new Bitmap[20];

    GridView androidGridView;
    public Bitmap[] bitmapArray = new Bitmap[20];


    //component
    private Button saveBT;
    private EditText nameTxt;

    PersonDatabase db;

    private FaceRecogitionProcessor faceRecognitionProcesser;
    private Interpreter faceNetInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_face);
        ll = findViewById(R.id.ll);
        saveBT = findViewById(R.id.button_save);
        nameTxt = findViewById(R.id.edit_user_name);


        try {
            Log.e("TEST", "detector OK");
            //makeText(this, "detector OK", LENGTH_SHORT).show();
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            Log.e("TEST", "detector ERROR");
            //makeText(this, "detector ERROR", LENGTH_SHORT).show();
            e.printStackTrace();
        }

        openGalley();
        try {
            db = new PersonDatabase();
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }
        faceRecognitionProcesser = new FaceRecogitionProcessor(faceNetInterpreter);

        saveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) , Toast.LENGTH_SHORT).show();
                int num = countBitmap();
                String ps = String.valueOf(nameTxt.getText());
                db.add_newPerson_folder(ps);
                float[][] personList = new float[num][192];

                for (int i = 0; i < num; i++) {
                    float[] arr = faceRecognitionProcesser.recognize(faceSelect[i]);

                    try {
                        db.save2file(arr,ps);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                db.save_image(faceSelect[0],ps);
                makeText(getApplicationContext(), "Save Complete! " + num + " Images", Toast.LENGTH_SHORT).show();
            }
        });



/*
        TextView add_new_face = root.findViewById(R.id.add_new_face_button);
        add_new_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FaceRecognitionCamera.class);
                startActivity(intent);
            }
        });
        return rootView; */
    }


    int countBitmap(){
        int count = 0;
        for (int i = 0;i<faceSelect.length;i++){
            if (faceSelect[i] != null){
                count++;
            }
        }
        return count;
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
        if(requestCode==123 && resultCode==RESULT_OK){
            if(data.getClipData()!=null){

                int x=data.getClipData().getItemCount();
                for(int i=0;i<x;i++){
                    //list.add(data.getClipData().getItemAt(i).getUri());
                    Uri imgUri = Uri.parse(String.valueOf(data.getClipData().getItemAt(i).getUri()));

                    //img.setImageURI(imgUri);


                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver() , Uri.parse(String.valueOf(imgUri)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Bitmap bitmap1 = BitmapEditor.getResizedBitmap(bitmap, 320, 320);
                    List<Classifier.Recognition> results = detector.recognizeImage(bitmap1);


                    for (final Classifier.Recognition result : results) {

                        final RectF location = result.getLocation();
                        //                           X - Y - Width - Height
                        if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                            Log.d("LOCATION", String.valueOf(location.left)+" " +String.valueOf(location.top)+" " +String.valueOf(location.right)+" " +String.valueOf(location.bottom));


                            Bitmap m = cropBitmap(location.left, location.top, location.right, location.bottom,  result.getX(), result.getY(), bitmap1);
                            ImageView img = new ImageView(getApplication());
                            img.setImageBitmap(m);
                            ll.addView(img);
                            faceSelect[j] = m;

                            ++j;
                        }
                        }


                }
                //adaptor.notifyDataSetChanged();
                //textView.setText("Image("+list.size()+")");

                Log.e("bitmap","" + faceSelect[0]);
                Log.e("bitmap","" + faceSelect[10]);




            }else if(data.getData()!=null){
                String imgurl = data.getData().getPath();
                //list.add(Uri.parse(imgurl));
            }
        }

    }

    public Bitmap cropFace(double X, double Y, double width, double height, Bitmap b) throws IOException {
        Log.d("CROPFACE", "width: "+String.valueOf(width) + " H :" + String.valueOf(height) + " X" + X + "  Y" + Y);
        if (!(b == null)){
            double newSize = 320;
            int newX = (int)Math.round(X * newSize);
            int newY = (int)Math.round(X * newSize);
            int newH = (int)Math.round(X * newSize);
            int newW = (int)Math.round(X * newSize);

            Log.d("IMAGESIZE", " cropFace ////////////////////////////////////");
            Log.d("IMAGESIZE", "   newSize = " + newSize);
            Log.d("IMAGESIZE", "   x = " + newX);
            Log.d("IMAGESIZE", "   y = " + newY);
            Log.d("IMAGESIZE", "   h = " + newH);
            Log.d("IMAGESIZE", "   w = " + newW);

            Bitmap bb = Bitmap.createScaledBitmap(b, newW, newH, false);

            //Bitmap bb = BitmapEditor.getResizedBitmapBig(b, (float) newW, (float) newH);

            Log.d("IMAGESIZE", "   bitmap h = " + bb.getHeight());
            Log.d("IMAGESIZE", "   bitmap w = " + bb.getWidth());

            //Bitmap bb = BitmapEditor.getResizedBitmapBig(b, (float) width, (float) height);
            Log.d("IMAGESIZE", "BB h "+ bb.getHeight() + " w "+bb.getWidth());
            //return bb;
            return BitmapEditor.crop2(bb, (float) X, (float) Y, (float) width, (float) height);
        }
        return null;


    }

    public Bitmap cropBitmap(double X, double Y, double width, double height, float xPos, float yPos, Bitmap bm){
        int width2 = 640;
        int height2 = 480;

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        int h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        int w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        int x = (int) Math.round((float) (X * width2));
        int y = (int) Math.round((float) (Y * height2));

        Bitmap bb = BitmapEditor.getResizedBitmap(bm, width2, height2);
        Bitmap b = BitmapEditor.crop(bb, x, y, w, h);

        return b;
    }


}