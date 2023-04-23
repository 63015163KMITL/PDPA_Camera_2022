package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.cekmitl.pdpacameracensor.FFmpegProcessActivity;
import com.cekmitl.pdpacameracensor.PreviewActivity;
import com.cekmitl.pdpacameracensor.R;

public class GridViewGalleryAdaptor extends BaseAdapter {

    private final Context mContext;
//    private final ArrayList<Bitmap> gridViewImageId;
//    private final ArrayList<Bitmap>  faceSelected = new ArrayList<Bitmap>();

    public Bitmap[] thumbnails;
    public String[] arrPath;
    public int[] typeMedia;
    public Cursor imagecursor;

    public GridViewGalleryAdaptor(Context context, Bitmap[] thumbnails, String[] arrPath, int[] typeMedia) {
        mContext = context;
        this.thumbnails = thumbnails;
        this.arrPath = arrPath;
        this.typeMedia = typeMedia;
        this.imagecursor = imagecursor;
    }

    @Override
    public int getCount() {
        return arrPath.length;
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
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_grid_view_gallery, null);
            holder.imageview = (ImageView) convertView.findViewById(R.id.face_thumnail);
            holder.videoICON = (ImageView) convertView.findViewById(R.id.ic_video);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int holder_size = 200;
        holder.imageview.getLayoutParams().height = holder_size;
        holder.imageview.getLayoutParams().width = holder_size;

        holder.imageview.setId(i);

        if (typeMedia[i] == 1)
            holder.videoICON.setVisibility(View.GONE);
        else if (typeMedia[i] == 3)
            holder.videoICON.setVisibility(View.VISIBLE);

        holder.imageview.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    if (typeMedia[i] == 1) {
//                        Toast.makeText(mContext, "IMAGE PATH : " + arrPath[i], Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, PreviewActivity.class);
                        intent.putExtra("key", arrPath[i]);
                        mContext.startActivity(intent);

                    } else if (typeMedia[i] == 3) {
//                        holder.videoICON.setVisibility(View.VISIBLE);
//                        Toast.makeText(mContext, "VIDEO PATH : " + arrPath[i], Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(mContext, FFmpegProcessActivity.class);

                        String path = arrPath[i];
                        String filename = path.substring(path.lastIndexOf("/") + 1);

                        intent.putExtra("video_name", filename);
                        intent.putExtra("path", arrPath[i]);

                        mContext.startActivity(intent);
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "Video not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.imageview.setImageBitmap(thumbnails[i]);
        holder.id = 1;
        return convertView;
    }


    class ViewHolder {
        ImageView imageview;
        ImageView videoICON;
        int id;
    }

}

