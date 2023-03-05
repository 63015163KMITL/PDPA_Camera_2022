package com.cekmitl.pdpacameracensor.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cekmitl.pdpacameracensor.GridViewAdapter;
import com.cekmitl.pdpacameracensor.Person;
import com.cekmitl.pdpacameracensor.PersonDatabase;
import com.cekmitl.pdpacameracensor.R;
import com.cekmitl.pdpacameracensor.databinding.FragmentHomeBinding;

import java.io.IOException;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    Person[] persons;
    PersonDatabase db;

    GridView androidGridView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.main_color));
        View decorView = getActivity().getWindow().getDecorView(); //set status background black
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        TextView name = root.findViewById(R.id.pdpa);
        name.setText("PDPA");

        try {
            db = new PersonDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        persons = db.persons;

        Log.e("fr", "Hello");


        //Grid View//////////////////////////////////////////////////////////////////////////////////////
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

        GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), gridViewString, gridViewImageId, 0);
        androidGridView = rootView.findViewById(R.id.grid_view);
        androidGridView.setAdapter(adapterViewAndroid);

        return rootView;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}