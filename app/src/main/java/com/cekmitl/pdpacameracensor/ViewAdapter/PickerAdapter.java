package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.cekmitl.pdpacameracensor.MainCameraActivity;
import com.cekmitl.pdpacameracensor.R;

import org.w3c.dom.Text;

import java.util.List;


public class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.TextVH> {

    private Context context;
    private List<String> dataList;
    private RecyclerView recyclerView;
    private MainCameraActivity mainCameraActivity;

    private TextVH textVH;
    View view;


    public PickerAdapter(Context context, List<String> dataList, RecyclerView recyclerView, MainCameraActivity _mainCamera) {
        this.context = context;
        this.dataList = dataList;
        this.recyclerView = recyclerView;
        mainCameraActivity = _mainCamera;

    }

    @Override
    public TextVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.picker_item_layout, parent, false);
        return new PickerAdapter.TextVH(view);
    }



    @Override
    public void onBindViewHolder(TextVH holder, final int position) {
        textVH = holder;
        textVH.pickerTxt.setText(dataList.get(position));
        recyclerView.smoothScrollToPosition(1);

//        for (int i = 0; i < dataList.size(); i++) {
//            if (String.valueOf(i).equals(textVH.pickerTxt.getTag())){
//                textVH.pickerTxt.setTextColor(Color.parseColor("#FFFFFF"));
//            }else {
//                textVH.pickerTxt.setTextColor(Color.parseColor("#FBB040"));
//            }
//        }
//        Toast.makeText(context,"textVH.pickerTxt.setOnClickListener", Toast.LENGTH_SHORT).show();

        textVH.pickerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,"textVH.pickerTxt.setOnClickListener", Toast.LENGTH_SHORT).show();
                if (recyclerView != null) {
                    recyclerView.smoothScrollToPosition(position);
                }

            }
        });

    }

    private TextView lastView;
    private boolean x = false;

    public void changeColor(int position, TextView view){

        if (x){
            lastView.setTextColor(Color.parseColor("#FFFFFF"));
        }
        x = true;

        for (int i = 0; i < dataList.size(); i++) {
            if (i == position){
                view.setTextColor(Color.parseColor("#FBB040"));
            }else {
                view.setTextColor(Color.parseColor("#FBB040"));
            }
        }

        lastView = view;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void swapData(List<String> newData) {
        dataList = newData;
        notifyDataSetChanged();
    }

    class TextVH extends RecyclerView.ViewHolder {
        TextView pickerTxt;

        public TextVH(View itemView) {
            super(itemView);
            pickerTxt = (TextView) itemView.findViewById(R.id.picker_item);
        }

    }
}