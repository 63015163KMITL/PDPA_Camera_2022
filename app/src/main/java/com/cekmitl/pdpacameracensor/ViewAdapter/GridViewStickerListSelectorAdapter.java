package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cekmitl.pdpacameracensor.R;

import java.io.IOException;
import java.io.InputStream;

public class GridViewStickerListSelectorAdapter extends BaseAdapter {

    private Context mContext;
    private Bitmap[] sticker;
    private Bitmap[] selectedSticker;
    private int[] id;

    LayoutInflater inflater;

    public GridViewStickerListSelectorAdapter(Context context, Bitmap[] selectedSticker, int [] id) {
        mContext = context;
        this.selectedSticker = selectedSticker;
        sticker = getBitmapFromAsset(mContext);
        this.id = id;
//        Log.e("sticker", "sticker : " + sticker.toString());
//        this.sticker = sticker;


    }

    @Override
    public int getCount() {
        return sticker.length;
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
            gridViewAndroid = inflater.inflate(R.layout.activity_grid_view_sticker_selector_adapter, null);
            ImageView imageViewAndroid = gridViewAndroid.findViewById(R.id.sticker_icon);
            imageViewAndroid.setImageBitmap(sticker[i]);
            gridViewAndroid.setTag(i);

        }else {
            gridViewAndroid = (View) convertView;
        }

        gridViewAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Toast.makeText(mContext, "Hello", Toast.LENGTH_SHORT).show();
                v.setBackgroundResource(R.drawable.bg_round_list_selector);
                selectedSticker[0] = sticker[i];
                id[0] = (int) v.getTag();
//                Toast.makeText(mContext, v.getTag().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        return gridViewAndroid;
    }

    //file:///android_asset/
    public static Bitmap[] getBitmapFromAsset(Context context) {
        String path = "file:///android_asset/sticker/";
        AssetManager assetManager = context.getAssets();

        //context, Uri.parse(path + "stricker" + i + ".png")

        InputStream istr;
        Bitmap[] bitmap = new Bitmap[107];

        for (int i = 0; i < 107; i++) {
//            bitmap[i] = BitmapFactory.decodeStream(assets.open(String.valueOf(Uri.parse(path + "stricker" + (i + 1) + ".png"))));
            // Read a Bitmap from Assets
            try {
                InputStream open = assetManager.open("sticker/stricker" + i+1 + ".png");
                bitmap[i] = BitmapFactory.decodeStream(open);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return bitmap;
    }

}
