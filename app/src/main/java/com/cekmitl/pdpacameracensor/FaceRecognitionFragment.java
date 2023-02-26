package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.makeText;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cekmitl.pdpacameracensor.databinding.FragmentHomeBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FaceRecognitionFragment extends Fragment {

    private FaceRecognitionViewModel mViewModel;
    Person[] persons;
    PersonDatabase db;
    public static FaceRecognitionFragment newInstance() {
        return new FaceRecognitionFragment();
    }

    GridView androidGridView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_face_recognition, container, false);
        View root = rootView.getRootView();

        try {
            db = new PersonDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        persons = db.persons; //หน้าที่เก็บไว้
        String s = "";
        //Name----------------------------------------------------
        String[] gridViewString = new String[persons.length + 1];
        int i = 0;
        for (Person p : persons) {
            gridViewString[i] = p.getName();
            i++;
        }
        gridViewString[persons.length] = "NEW";

        //Thumnail Image------------------------------------------
        Bitmap[] gridViewImageId = new Bitmap[persons.length + 1];
        int j = 0;
        for (Person p : persons) {
            gridViewImageId[j] = BitmapFactory.decodeFile(p.getImage());
            j++;
        }
        gridViewImageId[persons.length] = null;

        //makeText(root.getContext(), s, Toast.LENGTH_SHORT).show();

        GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), gridViewString, gridViewImageId);
        androidGridView = rootView.findViewById(R.id.grid_view);
        androidGridView.setAdapter(adapterViewAndroid);

        androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                makeText(root.getContext(), "Hello " + i, Toast.LENGTH_SHORT).show();
                Log.e("GridView","GridView Click");
            }
        });

/*
        TextView add_new_face = root.findViewById(R.id.add_new_face_button);
        add_new_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FaceRecognitionCamera.class);
                startActivity(intent);
            }
        }); */
        return rootView;



        //return inflater.inflate(R.layout.fragment_face_recognition, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FaceRecognitionViewModel.class);
        // TODO: Use the ViewModel
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }



}