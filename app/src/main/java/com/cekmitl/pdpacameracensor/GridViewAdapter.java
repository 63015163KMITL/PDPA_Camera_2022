package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.makeText;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    private final String[] gridViewString;
    private final Bitmap[] gridViewImageId;
    private int fullView = 0;

    private static final String[] CLUBS =
            {"open camera", "choose from gallery"};

    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    TextView total;
    ArrayList<Uri> mArrayUri;
    int position = 0;
    List<String> imagesEncodedList;

    LayoutInflater inflater;

    public GridViewAdapter(Context context, String[] gridViewString, Bitmap[] gridViewImageId, int fullView) {
        mContext = context;
        this.gridViewImageId = gridViewImageId;
        this.gridViewString = gridViewString;
        this.fullView = fullView;
    }

    @Override
    public int getCount() {
        return gridViewString.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View gridViewAndroid;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            gridViewAndroid = new View(mContext);
            if(fullView == 1){
                gridViewAndroid = inflater.inflate(R.layout.face_grid_view_menu_full, null);
            }else {
                gridViewAndroid = inflater.inflate(R.layout.face_grid_view_menu, null);
            }

            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.face_name_label);
            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.face_thumnail);

            if(gridViewImageId[i] != null) {
                gridViewAndroid.setId(i);
                imageViewAndroid.setImageBitmap(gridViewImageId[i]);
                textViewAndroid.setText(gridViewString[i]);
                gridViewAndroid.setTag(gridViewString[i]);
            }else {
                gridViewAndroid.setTag("add_face");
                textViewAndroid.setText("");
            }
        } else {
            gridViewAndroid = (View) convertView;
        }



        gridViewAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("GRID", "onClick: " + v );
                //v.setBackgroundResource(R.drawable.ic_launcher_background);
                //Toast.makeText(mContext, "CLICK " + v.getId(), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);
                if(v.getTag() == "add_face"){

                    builder.setTitle("Select Favorite Team");
                    builder.setItems(CLUBS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selected = CLUBS[which];
                            if(which == 0){
                                //open camera
                                mContext.startActivity(new Intent(mContext, FaceRecognitionCamera.class));
                            }else if(which == 1){
                                Intent intent = new Intent(mContext, AddNewFaceActivity.class);/*
                                //mContext.startActivityForResult(intent, 1001);
                                mContext.startActivityForResult(new Intent(mContext, AddNewFaceActivity.class), 1001);
                                mContext.startActivityForResult(Intent.createChooser(intent,"Selcet Picture"),123);*/
                            }

                        }
                    });
                    builder.setNegativeButton("cancel", null);
                    builder.create();

                    // สุดท้ายอย่าลืม show() ด้วย
                    builder.show();
                }else {
                    // Create an alert builder

                    // set the custom layout
                    final View customLayout = inflater.inflate(R.layout.dialog_face_recog_edit_layout, null);
                    builder.setView(customLayout);

                    //EditText edt_name = customLayout.findViewWithTag(v.getTag());
                    EditText edt_name = customLayout.findViewById(R.id.edittext_name_profile);
                    edt_name.setText(v.getTag().toString());
                    edt_name.setSelection(edt_name.getText().length());

                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    Button button_save = customLayout.findViewById(R.id.button_save);
                    button_save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    Button button_delete = customLayout.findViewById(R.id.button_delete);
                    button_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                }

            }
        });

        return gridViewAndroid;
    }

}
