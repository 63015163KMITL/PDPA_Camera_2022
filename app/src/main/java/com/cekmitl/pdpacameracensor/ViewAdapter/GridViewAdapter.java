package com.cekmitl.pdpacameracensor.ViewAdapter;

import static com.cekmitl.pdpacameracensor.Process.PersonDatabase.delete;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.cekmitl.pdpacameracensor.AddNewFaceActivity;
import com.cekmitl.pdpacameracensor.FaceRecognitionCamera;
import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.R;

import java.io.IOException;

public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    private int fullView = 0;

    public PersonDatabase db = null;

    private static final String[] CLUBS =
            {"open camera", "choose from gallery"};

    LayoutInflater inflater;

    public GridViewAdapter(Context context, int fullView,PersonDatabase db) {
        mContext = context;
        this.fullView = fullView;
        this.db = db;
        this.count = db.persons.length +1;
        this.perImg = new Bitmap[count-1];
        this.perName = new String[count-1];
    }

    int count = 0;
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    String[] perName = null;
    Bitmap[] perImg = null;

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View gridViewAndroid;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            gridViewAndroid = new View(mContext);
//            Log.d("SETPERSON", "getView: "+i);
            if(fullView == 1){
                gridViewAndroid = inflater.inflate(R.layout.face_grid_view_menu_full, null);
            }else {
                gridViewAndroid = inflater.inflate(R.layout.face_grid_view_menu, null);
            }

            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.face_name_label);
            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.face_thumnail);
            Switch switch1 = gridViewAndroid.findViewById(R.id.switch1);

            if (db == null){
                try {
                    db = new PersonDatabase(-1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (i<db.persons.length){

                gridViewAndroid.setId(i);
                perImg[i] = BitmapFactory.decodeFile(db.persons[i].getImage());
                perName[i] = db.persons[i].getName();
                imageViewAndroid.setImageBitmap(perImg[i]);
                textViewAndroid.setText(perName[i]);
                gridViewAndroid.setTag(perName[i]);
                switch1.setChecked(db.persons[i].isOn);
                //db.persons[i].isOn;
//                boolean a = db.changState("Mik",false);

                switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // do something, the isChecked will be
                        // true if the switch is in the On position
                        try {
                            db.changState(perName[i], isChecked);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }else{
                gridViewAndroid.setTag("-1");
                textViewAndroid.setVisibility(View.GONE);
                switch1.setVisibility(View.GONE);
            }



        } else {
            gridViewAndroid = (View) convertView;
        }


        gridViewAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);
                if(v.getTag() == "-1"){

                    builder.setTitle("Add new face");
                    builder.setItems(CLUBS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selected = CLUBS[which];
                            if(which == 0){
                                //open camera
                                mContext.startActivity(new Intent(mContext, FaceRecognitionCamera.class));
                            }else {
                                mContext.startActivity(new Intent(mContext, AddNewFaceActivity.class));
                            }

                        }
                    });
                    builder.setNegativeButton("cancel", null);
                    builder.create();

                    // สุดท้ายอย่าลืม show() ด้วย
                    builder.show();
                }else {
                    // Create an alert builder

                    // set the custom layout
                    final View customLayout = inflater.inflate(R.layout.dialog_face_recog_edit_layout, null);
                    builder.setView(customLayout);

                    ImageView psImageView = customLayout.findViewById(R.id.psImageView);
                    psImageView.setImageBitmap(perImg[v.getId()]);

                    //EditText edt_name = customLayout.findViewWithTag(v.getTag());
                    EditText edt_name = customLayout.findViewById(R.id.edittext_name_profile);
                    //edt_name.setText(v.getTag().toString());
                    edt_name.setText(perName[v.getId()]);
                    edt_name.setSelection(edt_name.getText().length());

                    Button btn_update_face_camera = customLayout.findViewById(R.id.update_face_camera);
                    btn_update_face_camera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(mContext, FaceRecognitionCamera.class);
                            i.putExtra("psName", perName[v.getId()]); //Optional parameters
                            mContext.startActivity(i);
                        }
                    });

                    Button btn_update_face_gallery = customLayout.findViewById(R.id.update_face_gallery);
                    btn_update_face_gallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(mContext, AddNewFaceActivity.class);
                            i.putExtra("psName", perName[v.getId()]); //Optional parameters
                            mContext.startActivity(i);
                        }
                    });

                    Button button_check_face = customLayout.findViewById(R.id.button_check_face);
                    button_check_face.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(mContext, FaceRecognitionCamera.class);
                            i.putExtra("psName", perName[v.getId()]); //Optional parameters
                            i.putExtra("check", true);
                            mContext.startActivity(i);
                        }
                    });

                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    //

                    Button button_save = customLayout.findViewById(R.id.button_save);
                    button_save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    Button button_delete = customLayout.findViewById(R.id.button_delete);
                    button_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            delete(perName[v.getId()]);
                            AlertDialog.Builder sBuilder =
                                    new AlertDialog.Builder(mContext);
                            sBuilder.setTitle("Confirm deletion");
                            sBuilder.setMessage("Do you want to remove " + perName[v.getId()] + "'s data?");

                            sBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    delete(perName[v.getId()]);
                                }
                            });

                            sBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

                            sBuilder.show();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        return gridViewAndroid;
    }

}
