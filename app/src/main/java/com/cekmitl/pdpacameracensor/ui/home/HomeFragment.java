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
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        TextView name = root.findViewById(R.id.pdpa);
        name.setText("PDPA");

        //makeText(root.getContext(), s, Toast.LENGTH_SHORT).show();

        GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), (String[]) getPersonData().get(0), (Bitmap[]) getPersonData().get(1), 0);
        androidGridView = rootView.findViewById(R.id.grid_view);
        androidGridView.setAdapter(adapterViewAndroid);

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
        Log.e("fr", "getPersonData Hello");

        //Name----------------------------------------------------
        String[] strName = new String[persons.length + 1];
        int i = 0;
        for (Person p : persons) {
            strName[i] = p.getName();
            i++;
        }
        strName[persons.length] = "NEW";

        //Thumnail Image------------------------------------------
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
}