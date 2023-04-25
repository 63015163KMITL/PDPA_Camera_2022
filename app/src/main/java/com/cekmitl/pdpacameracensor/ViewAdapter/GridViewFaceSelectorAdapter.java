package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cekmitl.pdpacameracensor.R;

import java.util.ArrayList;

public class GridViewFaceSelectorAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<Bitmap> gridViewImageId;
    private final ArrayList<Bitmap>  faceSelected = new ArrayList<Bitmap>();
    private  boolean selectAll;

    public GridViewFaceSelectorAdapter(Context context, ArrayList<Bitmap> gridViewImageId, Boolean selectAll) {
        mContext = context;
        this.selectAll = selectAll;
        this.gridViewImageId = gridViewImageId;
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
                gridViewAndroid.setId(i);
                imageViewAndroid.setImageBitmap(gridViewImageId.get(i));

                if (selectAll){
                    gridViewAndroid.setBackgroundResource(R.drawable.bg_gridview_image_focus);
                    faceSelected.add(gridViewImageId.get(gridViewAndroid.getId()));
                }

        } else {
            gridViewAndroid = (View) convertView;
        }

        gridViewAndroid.setOnClickListener(v -> {
            if(faceSelected.contains(gridViewImageId.get(v.getId()))){
                v.setBackgroundResource(R.drawable.bg_gridview_image);
                faceSelected.remove(gridViewImageId.get(v.getId()));
            }else {
                v.setBackgroundResource(R.drawable.bg_gridview_image_focus);
                faceSelected.add(gridViewImageId.get(v.getId()));
            }
        });
        return gridViewAndroid;
    }

}
