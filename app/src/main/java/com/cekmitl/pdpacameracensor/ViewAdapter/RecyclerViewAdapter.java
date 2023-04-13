package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cekmitl.pdpacameracensor.FFmpegProcessActivity;
import com.cekmitl.pdpacameracensor.PreviewActivity;
import com.cekmitl.pdpacameracensor.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    // creating a variable for our context and array list.
    private final Context context;
    private final ArrayList<String> imagePathArrayList;

    // on below line we have created a constructor.
    public RecyclerViewAdapter(Context context, ArrayList<String> imagePathArrayList) {
        this.context = context;
        this.imagePathArrayList = imagePathArrayList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        File imgFile = new File(imagePathArrayList.get(position));
        if (imgFile.exists()) {
            Picasso.get().load(imgFile).placeholder(R.drawable.ic_launcher_background).into(holder.imageIV);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String[] str = (imagePathArrayList.get(position) + "").split("\\.");

                   // Toast.makeText(context, "str : " + Arrays.toString(str), Toast.LENGTH_SHORT).show();
                    if(str[1].equals("mp4")){
//                        Toast.makeText(context, "MP4\nClick : " + imagePathArrayList.get(position), Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(context, FFmpegProcessActivity.class);
                        i.putExtra("key", imagePathArrayList.get(position));
                        context.startActivity(i);
                    }else {
//                        Toast.makeText(context, "Image\nClick : " + imagePathArrayList.get(position), Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(context, PreviewActivity.class);
                        i.putExtra("key", imagePathArrayList.get(position));
                        context.startActivity(i);
                    }

                    //Toast.makeText(context, "Click : " + imagePathArrayList.get(position), Toast.LENGTH_SHORT).show();

                    // inside on click listener we are creating a new intent

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imagePathArrayList.size();
    }

    // View Holder Class to handle Recycler View.
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageIV;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIV = itemView.findViewById(R.id.idIVImage);
        }
    }
}