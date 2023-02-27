package com.cekmitl.pdpacameracensor;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    private final String[] gridViewString;
    private final Bitmap[] gridViewImageId;

    private static final String[] CLUBS =
            {"open camera", "choose from gallery"};

    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    TextView total;
    ArrayList<Uri> mArrayUri;
    int position = 0;
    List<String> imagesEncodedList;

    public GridViewAdapter(Context context, String[] gridViewString, Bitmap[] gridViewImageId) {
        mContext = context;
        this.gridViewImageId = gridViewImageId;
        this.gridViewString = gridViewString;
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
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {


            gridViewAndroid = new View(mContext);
            gridViewAndroid = inflater.inflate(R.layout.face_grid_view_menu, null);


            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.face_name_label);
            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.face_thumnail);
            textViewAndroid.setText(gridViewString[i]);
            if(gridViewImageId[i] != null) {
                gridViewAndroid.setId(i);
                imageViewAndroid.setImageBitmap(gridViewImageId[i]);
            }else {
                gridViewAndroid.setTag("add_face");
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
                if(v.getTag() == "add_face"){

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(mContext);
                    builder.setTitle("Select Favorite Team");
                    builder.setItems(CLUBS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selected = CLUBS[which];
                            if(which == 0){
                                //open camera
                                mContext.startActivity(new Intent(mContext, FaceRecognitionCamera.class));
                            }else if(which == 1){
                                mContext.startActivity(new Intent(mContext, AddNewFaceActivity.class));
                            }

                        }
                    });
                    builder.setNegativeButton("cancel", null);
                    builder.create();

                    // สุดท้ายอย่าลืม show() ด้วย
                    builder.show();
                }

            }
        });

        return gridViewAndroid;
    }

}
