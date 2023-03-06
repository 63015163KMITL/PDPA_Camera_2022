package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.makeText;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

public class FaceRecognitionFragment extends Fragment {

    private FaceRecognitionViewModel mViewModel;
    Person[] persons;
    PersonDatabase db;

    public static android.app.Fragment newInstance() {
        return new android.app.Fragment();
    }

    GridView androidGridView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.main_gray));

        final View rootView = inflater.inflate(R.layout.fragment_face_recognition, container, false);
//        View root = rootView.getRootView();

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

        GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), gridViewString, gridViewImageId, 1);
        androidGridView = rootView.findViewById(R.id.grid_view);
        androidGridView.setAdapter(adapterViewAndroid);

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

//    public static Bitmap drawableToBitmap (Drawable drawable) {
//        Bitmap bitmap = null;
//
//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            if(bitmapDrawable.getBitmap() != null) {
//                return bitmapDrawable.getBitmap();
//            }
//        }
//
//        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
//            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
//        } else {
//            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        }
//
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bitmap;
//    }

}