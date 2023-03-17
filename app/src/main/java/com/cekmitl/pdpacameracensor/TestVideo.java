package com.cekmitl.pdpacameracensor;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class TestVideo extends AppCompatActivity {

    Button btnCapture;
    Spinner spOption;
    VideoView myVideoView;

    private File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    String videoSource =  "https://sites.google.com/site/androidexample9/download/RunningClock.mp4";

    Uri uriVideoSource;
    Uri uri = Uri.parse(DOC_PATH + "/input.mp4");

    MediaController myMediaController;
    MediaMetadataRetriever myMediaMetadataRetriever;

    String[] stringOpts = {
            "none",
            "OPTION_CLOSEST",
            "OPTION_CLOSEST_SYNC",
            "OPTION_NEXT_SYNC",
            "OPTION_PREVIOUS_SYNC"};
    int[] valOptions ={
            0,  //will not be used
            MediaMetadataRetriever.OPTION_CLOSEST,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            MediaMetadataRetriever.OPTION_NEXT_SYNC,
            MediaMetadataRetriever.OPTION_PREVIOUS_SYNC};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video);

        myVideoView = (VideoView)findViewById(R.id.vview);

        prepareVideo();

        spOption = (Spinner)findViewById(R.id.option);
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(TestVideo.this,
                android.R.layout.simple_list_item_1, stringOpts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOption.setAdapter(adapter);

        btnCapture = (Button)findViewById(R.id.capture);
        btnCapture.setOnClickListener(btnCaptureOnClickListener);
    }

    View.OnClickListener btnCaptureOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            int currentPosition = myVideoView.getCurrentPosition(); //in millisecond
            Toast.makeText(TestVideo.this,
                    "Current Position: " + currentPosition + " (ms)",
                    Toast.LENGTH_LONG).show();

            Bitmap bmFrame;
            int pos = currentPosition * 1000;   //unit in microsecond
            int opt = spOption.getSelectedItemPosition();
            if(opt == 0){
                bmFrame = myMediaMetadataRetriever
                        .getFrameAtTime(pos);
            }else{
                bmFrame = myMediaMetadataRetriever
                        .getFrameAtTime(pos,
                                valOptions[opt]);
            }

            if(bmFrame == null){
                Toast.makeText(TestVideo.this,
                        "bmFrame == null!",
                        Toast.LENGTH_LONG).show();
            }else {
                AlertDialog.Builder myCaptureDialog =
                        new AlertDialog.Builder(TestVideo.this);
                ImageView capturedImageView = new ImageView(TestVideo.this);
                capturedImageView.setImageBitmap(bmFrame);
                LinearLayout.LayoutParams capturedImageViewLayoutParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                capturedImageView.setLayoutParams(capturedImageViewLayoutParams);

                myCaptureDialog.setView(capturedImageView);
                myCaptureDialog.show();
            }

        }
    };

    private void prepareVideo(){

        myMediaMetadataRetriever = new MediaMetadataRetriever();
        myMediaMetadataRetriever.setDataSource(String.valueOf(uri));

        myMediaController = new MediaController(TestVideo.this);
        myVideoView.setMediaController(myMediaController);

        //Toast.makeText((Context) TestVideo.this, (CharSequence) uri, Toast.LENGTH_LONG).show();

        uriVideoSource = Uri.parse(String.valueOf(uri));

        myVideoView.setVideoURI(uriVideoSource);

        myVideoView.setOnCompletionListener(myVideoViewCompletionListener);
        myVideoView.setOnPreparedListener(MyVideoViewPreparedListener);
        myVideoView.setOnErrorListener(myVideoViewErrorListener);

        myVideoView.requestFocus();
        myVideoView.start();

    }

    MediaPlayer.OnCompletionListener myVideoViewCompletionListener =
            new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    Toast.makeText(TestVideo.this, "End of Video",
                            Toast.LENGTH_LONG).show();
                }
            };

    MediaPlayer.OnPreparedListener MyVideoViewPreparedListener =
            new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    long duration = myVideoView.getDuration(); //in millisecond
                    Toast.makeText(TestVideo.this,
                            "Duration: " + duration + " (ms)",
                            Toast.LENGTH_LONG).show();

                }
            };

    MediaPlayer.OnErrorListener myVideoViewErrorListener =
            new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    String errWhat = "";
                    switch (what){
                        case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                            errWhat = "MEDIA_ERROR_UNKNOWN";
                            break;
                        case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                            errWhat = "MEDIA_ERROR_SERVER_DIED";
                            break;
                        default: errWhat = "unknown what";
                    }

                    String errExtra = "";
                    switch (extra){
                        case MediaPlayer.MEDIA_ERROR_IO:
                            errExtra = "MEDIA_ERROR_IO";
                            break;
                        case MediaPlayer.MEDIA_ERROR_MALFORMED:
                            errExtra = "MEDIA_ERROR_MALFORMED";
                            break;
                        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                            errExtra = "MEDIA_ERROR_UNSUPPORTED";
                            break;
                        case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                            errExtra = "MEDIA_ERROR_TIMED_OUT";
                            break;
                        default:
                            errExtra = "...others";

                    }

                    Toast.makeText(TestVideo.this,
                            "Error!!!\n" +
                                    "what: " + errWhat + "\n" +
                                    "extra: " + errExtra,
                            Toast.LENGTH_LONG).show();
                    return true;
                }
            };
}