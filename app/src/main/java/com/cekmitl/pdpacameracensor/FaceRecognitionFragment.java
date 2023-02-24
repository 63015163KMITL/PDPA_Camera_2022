package com.cekmitl.pdpacameracensor;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cekmitl.pdpacameracensor.databinding.FragmentHomeBinding;

public class FaceRecognitionFragment extends Fragment {

    private FaceRecognitionViewModel mViewModel;

    public static FaceRecognitionFragment newInstance() {
        return new FaceRecognitionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_face_recognition, container, false);
        View root = rootView.getRootView();

        TextView add_new_face = root.findViewById(R.id.add_new_face_button);
        add_new_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FaceRecognitionCamera.class);
                startActivity(intent);
            }
        });



        return root;

        //return inflater.inflate(R.layout.fragment_face_recognition, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FaceRecognitionViewModel.class);
        // TODO: Use the ViewModel
    }

}