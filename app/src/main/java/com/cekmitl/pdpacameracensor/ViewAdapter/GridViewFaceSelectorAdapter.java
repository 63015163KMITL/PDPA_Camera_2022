package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cekmitl.pdpacameracensor.R;

import java.util.ArrayList;

public class GridViewFaceSelectorAdapter extends BaseAdapter {

    private Context mContext;
    private final String[] gridViewString;
    private ArrayList<Bitmap> gridViewImageId = new ArrayList<Bitmap>();
    private ArrayList<Bitmap>  faceSelected = new ArrayList<Bitmap>();

    public GridViewFaceSelectorAdapter(Context context, String[] gridViewString, ArrayList<Bitmap> gridViewImageId) {
        mContext = context;
        this.gridViewImageId = gridViewImageId;
        this.gridViewString = gridViewString;
    }

    @Override
    public int getCount() {
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

                Log.e("","");
            }
        });

        return gridViewAndroid;
    }

}
