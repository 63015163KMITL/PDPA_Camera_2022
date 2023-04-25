package com.cekmitl.pdpacameracensor.ui.home;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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
//        HomeFragment.db = db;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.main_color));
        View decorView = getActivity().getWindow().getDecorView(); //set status background black
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (db == null){
            try {
                db = new PersonDatabase(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        TextView name = rootView.findViewById(R.id.pdpa);
        name.setText("PDPA");



//        slideView2(drawView, FrameImagePreview.getHeight(), imgPreView.getHeight(), FrameImagePreview.getWidth(), display.getWidth());

        //makeText(root.getContext(), s, Toast.LENGTH_SHORT).show();
        if (db != null){
            GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), 0, db);
            androidGridView = rootView.findViewById(R.id.grid_view);
            androidGridView.setAdapter(adapterViewAndroid);
        }else{
            try {
                db = new PersonDatabase(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), 0, db);
            androidGridView = rootView.findViewById(R.id.grid_view);
            androidGridView.setAdapter(adapterViewAndroid);
        }


        GridView imagegrid = (GridView) rootView.findViewById(R.id.idRVImages);
        GridViewGalleryAdaptor gAdapter = new GridViewGalleryAdaptor(getContext(), mainActivity.thumbnails, mainActivity.arrPath, mainActivity.typeMedia);
        imagegrid.setAdapter(gAdapter);

        TextView see_more_gallery = rootView.findViewById(R.id.see_more_gallery);
        see_more_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.changFragment();
            }
        });
        try {
            refreshAdapter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootView;

    }

    public void refreshAdapter() throws IOException {
/*
        GridViewAdapter adapterViewAndroid = new GridViewAdapter(getActivity(), (String[]) getPersonData().get(0), (Bitmap[]) getPersonData().get(1), 0,db);
*/

        db = new PersonDatabase(-1);


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
        String[] strName = new String[persons.length - 1];
        int i = 0;
        for (Person p : persons) {
            strName[i] = p.getName();
            i++;
        }
        strName[persons.length] = "NEW";

        Bitmap[] bitmapProfile = new Bitmap[persons.length - 1];
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

    public static void resizeView(View view, int currentHeight, int newHeight, int currentWidth, int newWidth) {
        ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(300);
        ValueAnimator slideAnimator2 = ValueAnimator.ofInt(currentWidth, newWidth).setDuration(300);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */
        slideAnimator.addUpdateListener(animation1 -> {
            view.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });

        slideAnimator2.addUpdateListener(animation1 -> {
            view.getLayoutParams().width = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });
        /*  We use an animationSet to play the animation  */
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
        /*  We use an animationSet to play the animation  */
        AnimatorSet animationSet2 = new AnimatorSet();
        animationSet2.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet2.play(slideAnimator2);
        animationSet2.start();
    }

}