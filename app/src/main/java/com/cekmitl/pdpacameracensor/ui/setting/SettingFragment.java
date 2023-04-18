package com.cekmitl.pdpacameracensor.ui.setting;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cekmitl.pdpacameracensor.R;

public class SettingFragment extends Fragment implements View.OnClickListener{

    private SettingViewModel mViewModel;
    public SharedPreferences sharedPreferences;
    public Switch switch_grid_line, switch_location_tag, switch_mirror_font_camera, switch_preview_after_shutter, switch_volume_kaye_shutter;


    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        switch_grid_line = rootView.findViewById(R.id.switch_grid_line);
        switch_location_tag = rootView.findViewById(R.id.switch_location_tag);
        switch_mirror_font_camera = rootView.findViewById(R.id.switch_mirroi_font_camera);
        switch_preview_after_shutter = rootView.findViewById(R.id.switch_preview_after_shutter);
        switch_volume_kaye_shutter = rootView.findViewById(R.id.switch_volume_kaye_shutter);

        SharedPreferences sh = this.getActivity().getSharedPreferences("Setting",MODE_PRIVATE);

        switch_grid_line.setChecked(sh.getBoolean("switch_grid_line", true));
        switch_location_tag.setChecked(sh.getBoolean("switch_location_tag", true));
        switch_mirror_font_camera.setChecked(sh.getBoolean("switch_mirror_font_camera", true));
        switch_preview_after_shutter.setChecked(sh.getBoolean("switch_preview_after_shutter", true));
        switch_volume_kaye_shutter.setChecked(sh.getBoolean("switch_volume_kaye_shutter", true));
//        Toast.makeText(this, sh.getStringSet("Person_Selected",null).toArray().toString(), Toast.LENGTH_SHORT).show();

        switch_grid_line.setOnClickListener((View.OnClickListener) this);
        switch_location_tag.setOnClickListener((View.OnClickListener) this);
        switch_mirror_font_camera.setOnClickListener((View.OnClickListener) this);
        switch_preview_after_shutter.setOnClickListener((View.OnClickListener) this);
        switch_volume_kaye_shutter.setOnClickListener((View.OnClickListener) this);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        // TODO: Use the ViewModel
    }


    @Override
    public void onClick(View view) {
        sharedPreferences = this.getActivity().getSharedPreferences("Setting",MODE_PRIVATE);
        SharedPreferences.Editor setting = sharedPreferences.edit();
        setting.putBoolean("switch_grid_line", switch_grid_line.isChecked());
        setting.putBoolean("switch_location_tag", switch_location_tag.isChecked());
        setting.putBoolean("switch_mirror_font_camera", switch_mirror_font_camera.isChecked());
        setting.putBoolean("switch_preview_after_shutter", switch_preview_after_shutter.isChecked());
        setting.putBoolean("switch_volume_kaye_shutter", switch_volume_kaye_shutter.isChecked());
        setting.apply();
    }
}