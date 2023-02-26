package com.cekmitl.pdpacameracensor.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cekmitl.pdpacameracensor.Person;
import com.cekmitl.pdpacameracensor.PersonDatabase;
import com.cekmitl.pdpacameracensor.R;
import com.cekmitl.pdpacameracensor.databinding.FragmentHomeBinding;

import java.io.IOException;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    Person[] persons;
    PersonDatabase db;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
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

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}