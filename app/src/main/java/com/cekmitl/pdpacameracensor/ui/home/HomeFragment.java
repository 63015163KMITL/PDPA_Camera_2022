package com.cekmitl.pdpacameracensor.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cekmitl.pdpacameracensor.MainActivity;
import com.cekmitl.pdpacameracensor.Process.Person;
import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.R;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewAdapter;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewGalleryAdaptor;

import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    static Person[] persons;
    static PersonDatabase db;

    GridView androidGridView;
    MainActivity mainActivity;

    public HomeFragment(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.main_color));
        View decorView = getActivity().getWindow().getDecorView(); //set status background black
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        try {
            db = new PersonDatabase(-1);
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

        GridView imagegrid = (GridView) rootView.findViewById(R.id.idRVImages);
        GridViewGalleryAdaptor gAdapter = new GridViewGalleryAdaptor(getContext(), mainActivity.thumbnails, mainActivity.arrPath, mainActivity.typeMedia);
        imagegrid.setAdapter(gAdapter);


        return rootView;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public static ArrayList<Object> getPersonData(){
        ArrayList<Object> resulte = new ArrayList<>();
        try {
            if (db == null){
                db = new PersonDatabase(-1);
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

}