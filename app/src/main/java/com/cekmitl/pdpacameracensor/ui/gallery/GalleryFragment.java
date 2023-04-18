package com.cekmitl.pdpacameracensor.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cekmitl.pdpacameracensor.MainActivity;
import com.cekmitl.pdpacameracensor.R;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewGalleryAdaptor;

public class GalleryFragment extends Fragment {

    MainActivity mainActivity;

    public GalleryFragment(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.white));

        final View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        GridView imagegrid = (GridView) rootView.findViewById(R.id.idRVImages);
        GridViewGalleryAdaptor gAdapter = new GridViewGalleryAdaptor(getContext(), mainActivity.thumbnails, mainActivity.arrPath, mainActivity.typeMedia);
        imagegrid.setAdapter(gAdapter);

        return rootView;
    }
}