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
import java.util.HashSet;
import java.util.List;

public class GridViewFaceSelectorAdapter extends BaseAdapter {

    private Context mContext;
    private final String[] gridViewString;
    private ArrayList<Bitmap> gridViewImageId = new ArrayList<Bitmap>();
    private ArrayList<Bitmap>  faceSelected = new ArrayList<Bitmap>();

    private static final String[] CLUBS =
            {"open camera", "choose from gallery"};

    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    TextView total;
    ArrayList<Uri> mArrayUri;
    int position = 0;
    List<String> imagesEncodedList;

    public GridViewFaceSelectorAdapter(Context context, String[] gridViewString, ArrayList<Bitmap> gridViewImageId) {
        mContext = context;
        this.gridViewImageId = gridViewImageId;
        this.gridViewString = gridViewString;
    }

    @Override
    public int getCount() {
        //int count = 0;
        //for (int i = 0; i < gridViewImageId.length; i++){
        //    if(gridViewImageId[i] != null){
        //        count++;
        //    }
        //}
        return gridViewImageId.size();
    }

    public ArrayList<Bitmap> getFaceSelected() {
        return this.faceSelected;
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
            gridViewAndroid = inflater.inflate(R.layout.activity_grid_view_face_selector_adapter, null);

            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.face_thumnail);
            //textViewAndroid.setText(gridViewString[i]);
            //if(gridViewImageId[i] != null) {
                gridViewAndroid.setId(i);
                imageViewAndroid.setImageBitmap(gridViewImageId.get(i));
            //}
        } else {
            gridViewAndroid = (View) convertView;
        }



        gridViewAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("GridSelecter","");
                Log.e("GridSelecter","////////////////////////////////////////");
                Log.e("GridSelecter"," gridViewImageId = " + gridViewImageId.toString() + "");
                Log.e("GridSelecter"," v.getId() = " + v.getId());
                Log.e("GridSelecter"," gridViewImageId.get(v.getId())) = " + gridViewImageId.get(v.getId()));

                if(faceSelected.contains(gridViewImageId.get(v.getId()))){
                    Log.e("GridSelecter"," contains = true");
                    v.setBackgroundResource(R.drawable.bg_gridview_image);
                    faceSelected.remove(gridViewImageId.get(v.getId()));
                }else {
                    Log.e("GridSelecter"," contains  = false");
                    v.setBackgroundResource(R.drawable.bg_gridview_image_focus);
                    faceSelected.add(gridViewImageId.get(v.getId()));
                }

                Log.e("GridSelecter"," faceSelected ALL = " + faceSelected.toString());

                //Select face objevt insert to ArrayList(faceSelected)
                /*
                if(gridViewImageId.contains(gridViewImageId.get(v.getId())) ){
                    faceSelected.remove(gridViewImageId.get(v.getId()));
                    //faceSelected.add(gridViewImageId.get(v.getId()));
                    Log.e("GridSelecter"," contains true");
                    Log.e("GridSelecter","Remove");
                    Log.e("GridSelecter","    faceSelected = " + faceSelected.toString());

                }else if(gridViewImageId.contains(gridViewImageId.get(v.getId())) ){
                    faceSelected.add(gridViewImageId.get(v.getId()));
                    v.setBackgroundResource(R.drawable.bg_gridview_image);
                    Log.e("GridSelecter","ADD");
                    Log.e("GridSelecter","    faceSelected = " + faceSelected.toString());
                }

                 */

                Log.e("","");
            }
        });

        return gridViewAndroid;
    }

}
