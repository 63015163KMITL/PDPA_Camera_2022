package com.cekmitl.pdpacameracensor;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

    public class GridViewFaceSelectorAdapter extends BaseAdapter {

        private Context mContext;
        private final Bitmap[] gridViewImageId;

        public GridViewFaceSelectorAdapter(Context context, Bitmap[] gridViewImageId) {
            mContext = context;
            this.gridViewImageId = gridViewImageId;
        }

        @Override
        public int getCount() {
            return 0;
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

                ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.image_gridview);
                //if(gridViewImageId[i] != null) {
                    gridViewAndroid.setId(i);
                    //imageViewAndroid.setImageBitmap(gridViewImageId[i]);
                    imageViewAndroid.setBackgroundResource(R.drawable.ic_launcher_background);
                //}
            } else {
                gridViewAndroid = (View) convertView;
            }


            gridViewAndroid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("GRID", "onClick: " + v );
                    //v.setBackgroundResource(R.drawable.ic_launcher_background);

                }
            });

            return gridViewAndroid;
        }

    }
