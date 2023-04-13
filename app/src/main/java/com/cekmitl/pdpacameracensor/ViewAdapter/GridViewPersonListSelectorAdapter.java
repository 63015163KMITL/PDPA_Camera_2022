package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GridViewPersonListSelectorAdapter extends BaseAdapter {

    private Context mContext;
    private final String[] gridViewString;
    private final Bitmap[] gridViewImageId;
    private int fullView = 0;
    private ArrayList<String> selectedFace;

    public PersonDatabase db = null;



    private static final String[] CLUBS =
            {"open camera", "choose from gallery"};

    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    TextView total;
    ArrayList<Uri> mArrayUri;
    int position = 0;
    List<String> imagesEncodedList;

    LayoutInflater inflater;

    public GridViewPersonListSelectorAdapter(Context context, String[] gridViewString, Bitmap[] gridViewImageId, int fullView,ArrayList<String> selectedF) {
        mContext = context;
        this.gridViewImageId = Arrays.copyOfRange(gridViewImageId, 0, gridViewImageId.length - 1);
        this.gridViewString = Arrays.copyOfRange(gridViewString, 0, gridViewString.length - 1);
        this.fullView = fullView;
        this.selectedFace = selectedF;
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
                gridViewAndroid = inflater.inflate(R.layout.face_grid_view_menu_2, null);

            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.face_name_label);
            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.face_thumnail);

            if(gridViewImageId[i] != null) {
                gridViewAndroid.setId(i);
                imageViewAndroid.setImageBitmap(gridViewImageId[i]);
                textViewAndroid.setText(gridViewString[i]);
                gridViewAndroid.setTag(gridViewString[i]);

                if(selectedFace.contains(gridViewString[i])){
                    gridViewAndroid.setBackgroundResource(R.drawable.bg_round_list_selector);
                }

            }
//            else {
//                gridViewAndroid.setTag(gridViewString[i]);
//                textViewAndroid.setText("");
//            }

        } else {
            gridViewAndroid = (View) convertView;
        }



        gridViewAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String p = v.getTag().toString();

                if (!isSelected(p)){
                    selectedFace.add(p);
                    v.setBackgroundResource(R.drawable.bg_round_list_selector);
                }else{
                    selectedFace.remove(p);
                    v.setBackgroundColor(Color.parseColor("#00000000"));
                }

            }
        });

        return gridViewAndroid;
    }

    boolean isSelected(String select){
        for (String s : selectedFace){
            if (s == select){
                return true;
            }
        }
        return false;
    }

}
