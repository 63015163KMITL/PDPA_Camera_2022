package com.cekmitl.pdpacameracensor.ui.home;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cekmitl.pdpacameracensor.FFmpegProcessActivity;
import com.cekmitl.pdpacameracensor.PreviewActivity;
import com.cekmitl.pdpacameracensor.Process.Person;
import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.R;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewAdapter;
import com.cekmitl.pdpacameracensor.databinding.FragmentHomeBinding;

import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    static Person[] persons;
     static PersonDatabase db;
    GridView androidGridView;

    private int count;
    private Bitmap[] thumbnails;
    private boolean[] thumbnailsselection;
    private String[] arrPath;
    private int[] typeMedia;
    private ImageAdapter imageAdapter;

    Cursor imagecursor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.main_color));
        View decorView = getActivity().getWindow().getDecorView(); //set status background black
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        try {
            db = new PersonDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }


        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        TextView name = rootView.findViewById(R.id.pdpa);
        name.setText("PDPA");

        //makeText(root.getContext(), s, Toast.LENGTH_SHORT).show();

        GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), (String[]) getPersonData().get(0), (Bitmap[]) getPersonData().get(1), 0);
        androidGridView = rootView.findViewById(R.id.grid_view);
        androidGridView.setAdapter(adapterViewAndroid);


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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static ArrayList<Object> getPersonData(){
        ArrayList<Object> resulte = new ArrayList<>();
        try {
            if (db == null){
                db = new PersonDatabase();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        persons = db.persons;

        //Name----------------------------------------------------
        String[] strName = new String[persons.length + 1];
        int i = 0;
        for (Person p : persons) {
            strName[i] = p.getName();
            i++;
        }
        strName[persons.length] = "NEW";

        Bitmap[] bitmapProfile = new Bitmap[persons.length + 1];
        int j = 0;
        for (Person p : persons) {
            bitmapProfile[j] = BitmapFactory.decodeFile(p.getImage());
            j++;
        }
        bitmapProfile[persons.length] = null;

        resulte.add(strName);
        resulte.add(bitmapProfile);

        return resulte;
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
            HomeFragment.ViewHolder holder;

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            if (convertView == null) {
                holder = new HomeFragment.ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.list_grid_view_gallery, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.face_thumnail);
                holder.videoICON = (ImageView) convertView.findViewById(R.id.ic_video);
                convertView.setTag(holder);
            }
            else {
                holder = (HomeFragment.ViewHolder) convertView.getTag();
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