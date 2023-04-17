package com.cekmitl.pdpacameracensor.ui.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cekmitl.pdpacameracensor.FFmpegProcessActivity;
import com.cekmitl.pdpacameracensor.PreviewActivity;
import com.cekmitl.pdpacameracensor.R;

public class GalleryFragment extends Fragment {

    private int count;
    private Bitmap[] thumbnails;
    private boolean[] thumbnailsselection;
    private String[] arrPath;
    private int[] typeMedia;
    private ImageAdapter imageAdapter;

    Cursor imagecursor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.main_gray));


        final View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        String[] columns = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE,
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
        Uri queryUri = MediaStore.Files.getContentUri("external");

        imagecursor = getActivity().managedQuery(queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        int image_column_index = imagecursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        this.count = imagecursor.getCount();
        this.thumbnails = new Bitmap[this.count];
        this.arrPath = new String[this.count];
        this.typeMedia = new int[this.count];
        this.thumbnailsselection = new boolean[this.count];

        for (int i = 0; i < this.count; i++) {
            imagecursor.moveToPosition(i);
            int id = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 4;
            bmOptions.inPurgeable = true;
            int type = imagecursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            int t = imagecursor.getInt(type);

            if(t == 1)
                thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                        getActivity().getContentResolver(), id,
                        MediaStore.Images.Thumbnails.MINI_KIND, bmOptions);
            else if(t == 3)
                thumbnails[i] = MediaStore.Video.Thumbnails.getThumbnail(
                        getActivity().getContentResolver(), id,
                        MediaStore.Video.Thumbnails.MINI_KIND, bmOptions);

            arrPath[i]= imagecursor.getString(dataColumnIndex);
            typeMedia[i] = imagecursor.getInt(type);
        }

        GridView imagegrid = (GridView) rootView.findViewById(R.id.idRVImages);

        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);


        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imagecursor.close();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GalleryViewModel mViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        // TODO: Use the ViewModel
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("NewApi") public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.list_grid_view_gallery, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.face_thumnail);
                holder.videoICON = (ImageView) convertView.findViewById(R.id.ic_video);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            int holder_size = 200;
            holder.imageview.getLayoutParams().height = holder_size;
            holder.imageview.getLayoutParams().width = holder_size;

            holder.imageview.setId(position);

            if(typeMedia[position] == 1)
                holder.videoICON.setVisibility(View.GONE);
            else if(typeMedia[position] == 3)
                holder.videoICON.setVisibility(View.VISIBLE);

            holder.imageview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    try {
                        if(typeMedia[position] == 1){
//                        Toast.makeText(getActivity(), "IMAGE PATH : " + arrPath[position], Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getActivity(), PreviewActivity.class);
                            i.putExtra("key", arrPath[position]);
                            getActivity().startActivity(i);

                        }else if(typeMedia[position] == 3){
                            holder.videoICON.setVisibility(View.VISIBLE);
//                        Toast.makeText(getActivity(), "VIDEO PATH : " + arrPath[position], Toast.LENGTH_SHORT).show();

                            Intent i = new Intent(getActivity(), FFmpegProcessActivity.class);

                            String path = arrPath[position];
                            String filename = path.substring(path.lastIndexOf("/")+1);

                            i.putExtra("video_name", filename);
                            i.putExtra("path", arrPath[position]);

                            getActivity().startActivity(i);
                        }
                    }catch (Exception e){
                        Toast.makeText(getActivity(), "Video not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.imageview.setImageBitmap(thumbnails[position]);
            holder.id = position;
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        ImageView videoICON;
        int id;
    }
}