package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.FACE_NET_MODEL;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.OBJ_DETECT_CONFIDENT;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_INPUT_SIZE;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_LABELS_FILE;
import static com.cekmitl.pdpacameracensor.Process.AIProperties.TF_OD_API_MODEL_FILE;
import static com.cekmitl.pdpacameracensor.Process.Utils.deleteFiles;
import static com.cekmitl.pdpacameracensor.Process.Utils.deleteFolder;
import static com.cekmitl.pdpacameracensor.ui.home.HomeFragment.getPersonData;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.cekmitl.pdpacameracensor.ImageEditor.BitmapEditor;
import com.cekmitl.pdpacameracensor.Process.Classifier;
import com.cekmitl.pdpacameracensor.Process.EuclideanDistance;
import com.cekmitl.pdpacameracensor.Process.FaceRecogitionProcessor;
import com.cekmitl.pdpacameracensor.Process.PersonDatabase;
import com.cekmitl.pdpacameracensor.Process.Score;
import com.cekmitl.pdpacameracensor.Process.YoloV5Classifier;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewPersonListSelectorAdapter;
import com.cekmitl.pdpacameracensor.ViewAdapter.GridViewStickerListSelectorAdapter;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import it.mirko.rangeseekbar.OnRangeSeekBarListener;
import it.mirko.rangeseekbar.RangeSeekBar;

public class FFmpegProcessActivity extends AppCompatActivity implements OnRangeSeekBarListener, View.OnClickListener {
    private LinearLayout button_option_blur;
    private LinearLayout button_option_sticker;
    private LinearLayout button_option_shape;
    int CENSOR_TPYE = 0;
    int CENSOR_SIZE = 0;

    //Dialog Layout
    AlertDialog.Builder dialogBuilder;
    AlertDialog alertDialog;

    private final String[] frame_rate = {"25 FPS (recommend)", "30 FPS", "60 FPS"};
    private final String[] video_resolution = {"480px (recommend)", "640px", "720px", "1080px"};

    private ImageButton button_video_play;
    private int VIDEO_HIGH;
    private int VIDEO_WIDTH;
    int IMAGENUM;
    private final File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    String INPUT_PATH = DOC_PATH + "/out";
    String OUTPUT_PATH = DOC_PATH + "/out2";

    //----------------------------------------------------------------------------------------------
    //Face Select Option
    private String[] strFace_selected;

    //Blur Option val
    private int blur_radius = 0;
    private int blur_size = 0;

    //Sticker Option val
    private Bitmap sticker_option;
    private int sticker_size = 0;

    //Shape Option val (Canvas)
    private Canvas canvas;

    private int shape_color = Color.parseColor("#000000");
    private int shape_size = 0;
    //----------------------------------------------------------------------------------------------

    private final int video_frame_rate = 24;
    public int duration;
    private static int video_height = 720;
    private static int video_width = 720;

    public MediaMetadataRetriever retriever;
    public VideoView videoView;
    public MediaController myMediaController;

    //DETECT FACE
    public Classifier detector;
    public Classifier detector1;
    public Classifier detector2;
    public Classifier detector3;
    public Classifier detector4;


    //Face Recog
    private FaceRecogitionProcessor faceRecognitionProcesser1;
    private Interpreter faceNetInterpreter1;

    private FaceRecogitionProcessor faceRecognitionProcesser2;
    private Interpreter faceNetInterpreter2;

    private FaceRecogitionProcessor faceRecognitionProcesser3;
    private Interpreter faceNetInterpreter3;

    private FaceRecogitionProcessor faceRecognitionProcesser4;
    private Interpreter faceNetInterpreter4;
    //    float CONFIDENT = 0.67f;
    EuclideanDistance distance;
    String path = DOC_PATH + "/";
    String inputVideo = "input.mp4";
    String tempeFrame = "%d.jpg";
    String tempeFramePool = "out/";           //Folder
    String tempeVideo = "out.mp4";     //without audio
    String audio = "out2/audio.mp3";
    String finalVideoResulte = "final_output.mp4";
    String VIDEO_NAME = "";

    //Seekbar
    public SeekBar sk;

    ProgressDialog progressDialogP2;

    boolean isT2Done = false;
    boolean isT3Done = false;
    boolean isT4Done = false;
    //RelativeLayout setFocusView
    RelativeLayout fram_focus_layout;

    //Person Data
    static PersonDatabase db;

    private String[] listPerson_name;
    private Bitmap[] listPerson_image;

    public FFmpegProcessActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_process);

        //ลบ Action Bar ออก
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        //Button
        Button detect_btt = findViewById(R.id.button6);
        Button next_btt = findViewById(R.id.nextBtt);
        button_video_play = findViewById(R.id.button_video_play);

        fram_focus_layout = findViewById(R.id.fram_focus_layout);

        LinearLayout button_option_face = findViewById(R.id.button_option_face);
        button_option_blur = findViewById(R.id.button_option_blur);
        button_option_sticker = findViewById(R.id.button_option_sticker);
        button_option_shape = findViewById(R.id.button_option_shape);
        LinearLayout button_option_cut = findViewById(R.id.button_option_cut);

        detect_btt.setOnClickListener(this);
        next_btt.setOnClickListener(this);
        button_video_play.setOnClickListener(this);

        button_option_face.setOnClickListener(this);
        button_option_blur.setOnClickListener(this);
        button_option_sticker.setOnClickListener(this);
        button_option_shape.setOnClickListener(this);
        button_option_cut.setOnClickListener(this);

        if (db == null) {
            try {
                db = new PersonDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent intent = getIntent();
        String video_name = intent.getStringExtra("video_name"); //if it's a string you stored.
        String path_name = intent.getStringExtra("path");

        Uri uri = Uri.parse(DOC_PATH + "/" + video_name + ".mp4");

        if(path_name != null && video_name != null){
            uri = Uri.parse(path_name);
            inputVideo = path_name;
        }

        inputVideo = video_name + ".mp4";
        tempeFramePool = video_name + "/";
        tempeVideo = video_name +"_temp.mp4";
        audio = video_name + "/audio.mp3";
        finalVideoResulte = video_name + "_output.mp4";
        INPUT_PATH = DOC_PATH + "/" + video_name;
        OUTPUT_PATH = DOC_PATH + "/" + video_name;
        VIDEO_NAME = video_name;


        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(String.valueOf(uri));

        videoView = findViewById(R.id.videoView);
        videoView.setVideoURI(uri);
        videoView.setOnCompletionListener(myVideoViewCompletionListener);
        videoView.setOnPreparedListener(MyVideoViewPreparedListener);
        videoView.setOnErrorListener(myVideoViewErrorListener);
        videoView.requestFocus();

        //Size of frame preview
        video_width = 1080;
        video_height = 607;

        //SeekBar
        sk = findViewById(R.id.seekBar);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    videoView.seekTo(seekBar.getProgress());
                    clearFocus();

                    Bitmap bmFrame = retriever.getFrameAtTime(seekBar.getProgress() * 1000L, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC); //unit in microsecond
                    if(bmFrame == null){
                        makeText(FFmpegProcessActivity.this, "bmFrame == null!", Toast.LENGTH_LONG).show();
                    }else{
                        faceDectecFrame(bmFrame);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RangeSeekBar rangeSeekBar = findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setStartProgress(20); // default is 0
        rangeSeekBar.setEndProgress(80); // default is 50
        rangeSeekBar.setMinDifference(5); // default is 20
        rangeSeekBar.setOnRangeSeekBarListener((OnRangeSeekBarListener) FFmpegProcessActivity.this);

        try {
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector1 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector2 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector3 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector4 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Face Recog
        try {
            faceNetInterpreter1 = new Interpreter(FileUtil.loadMappedFile(this, FACE_NET_MODEL), new Interpreter.Options());
            faceNetInterpreter2 = new Interpreter(FileUtil.loadMappedFile(this, FACE_NET_MODEL), new Interpreter.Options());
            faceNetInterpreter3 = new Interpreter(FileUtil.loadMappedFile(this, FACE_NET_MODEL), new Interpreter.Options());
            faceNetInterpreter4 = new Interpreter(FileUtil.loadMappedFile(this, FACE_NET_MODEL), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }
        faceRecognitionProcesser1 = new FaceRecogitionProcessor(faceNetInterpreter1);
        faceRecognitionProcesser2 = new FaceRecogitionProcessor(faceNetInterpreter2);
        faceRecognitionProcesser3 = new FaceRecogitionProcessor(faceNetInterpreter3);
        faceRecognitionProcesser4 = new FaceRecogitionProcessor(faceNetInterpreter4);

        distance = new EuclideanDistance();

        File f = new File(INPUT_PATH);
        if (!(f.exists())){
            f.mkdirs();
        }

        File f2 = new File(OUTPUT_PATH);
        if (!(f2.exists())){
            f2.mkdirs();
        }

        //Initial GridView list person
        listPerson_name = (String[]) getPersonData().get(0);
        listPerson_image = (Bitmap[]) getPersonData().get(1);
    }


    public void ExtractVideoFrame(String cmd) {
        int rc = FFmpeg.execute(cmd);

        if (rc == RETURN_CODE_SUCCESS) {
            p1 = true;
            Log.i(Config.TAG, "Command execution completed successfully.");
            checkVideoSize();
            processDetect();
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }
    }

    public void ExtractVideoAudio(String cmd){
        //ffmpeg -i input.mp4 -vn -ar 44100 -ac 2 -ab 192k -f mp3 audio.mp3
        int rc = FFmpeg.execute(cmd);
        if (rc == RETURN_CODE_SUCCESS) {
            p4 = true;
            MergeAudioToVideo("-i "+ path + tempeVideo + " -i " + path + audio + " -c:v copy -c:a aac " + path + finalVideoResulte);
            Log.i(Config.TAG, "Command execution completed successfully.");
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }
    }


    public void MergeImageToVideo(String cmd){
        //ffmpeg -f image2 -framerate 23.976 -i temp%05d.jpg -c:v libx264 out.mp4
        int rc = FFmpeg.execute(cmd);
        if (rc == RETURN_CODE_SUCCESS) {
            p3 = true;
            Log.i(Config.TAG, "Command execution completed successfully.");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ExtractVideoAudio("-i "+ path + inputVideo + "  -vn -ar 44100 -ac 2 -ab 192k -f mp3 "+ path + audio);
                }
            }).start();

        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }
    }

    public void MergeAudioToVideo(String cmd){
        //ffmpeg -i video.mp4 -i audio.wav -c:v copy -c:a aac output.mp4
        int rc = FFmpeg.execute(cmd);
        if (rc == RETURN_CODE_SUCCESS) {
            p5 = true;
            Log.i(Config.TAG, "Command execution completed successfully.");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteFolder(INPUT_PATH);
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteFiles(DOC_PATH+"/"+tempeVideo);
                }
            }).start();

        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }
    }

    @Override
    public void onRangeValues(RangeSeekBar rangeSeekBar, int start, int end) {}

    long startTime ;
    int processedIMAGE1 = 0;
    int processedIMAGE2 = 0;
    int processedIMAGE3 = 0;
    int processedIMAGE4 = 0;
    public void executeDetect1(){
        try {
            long h1 = 1;
            long h2 = Math.round(IMAGENUM * 0.25);
            for (long i = h1;i<h2;i++){
                startTime = System.nanoTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String oldPath = INPUT_PATH + "/" + i + ".jpg";
                    FaceDetectionInPicture(oldPath, detector1,faceRecognitionProcesser1);
                    processedIMAGE1++;
                }
            }
            while (true){
                if (isT2Done && isT3Done && isT4Done){
                    p2 = true;
                    mergeVideo();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executeDetect2(){
        try {
            isT2Done = false;
            long h1 = Math.round(IMAGENUM*0.25);
            long h2 = Math.round(IMAGENUM*0.50);
            for (long i = h1;i<=h2;i++){
                startTime = System.nanoTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String oldPath = INPUT_PATH + "/" + i + ".jpg";
                    FaceDetectionInPicture(oldPath, detector2,faceRecognitionProcesser2);
                    processedIMAGE2++;
                }
            }
            isT2Done = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executeDetect3(){
        try {
            isT3Done = false;
            long h1 = Math.round(IMAGENUM*0.50);
            long h2 = Math.round(IMAGENUM*0.75);
            for (long i = h1;i<=h2;i++){
                startTime = System.nanoTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String oldPath = INPUT_PATH + "/" + i + ".jpg";
                    FaceDetectionInPicture(oldPath, detector3,faceRecognitionProcesser3);
                    processedIMAGE3++;
                }
            }
            isT3Done = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executeDetect4(){
        try {
            isT4Done = false;
            long h1 = Math.round(IMAGENUM*0.75);
            long h2 = IMAGENUM;
            for (long i = h1;i<=h2;i++){
                startTime = System.nanoTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String oldPath = INPUT_PATH + "/" + i + ".jpg";
                    FaceDetectionInPicture(oldPath, detector4,faceRecognitionProcesser4);
                    processedIMAGE4++;
                }
            }
            isT4Done = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void FaceDetectionInPicture(String IMG_PATH,Classifier detector,FaceRecogitionProcessor faceRecogitionProcessor) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(IMG_PATH);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        handleResult(bitmap,IMG_PATH,detector,faceRecogitionProcessor);
    }

    private File[] getListFile(){
        return new File(INPUT_PATH).listFiles();
    }

    private void handleResult(Bitmap bm, String newPath, Classifier detector, FaceRecogitionProcessor faceRecognitionProcesser) {
        if (bm == null) {
            makeText(this, "ERROR", LENGTH_SHORT).show();
        } else {
            List<Classifier.Recognition> results = detector.recognizeImage(BitmapEditor.getResizedBitmap(bm, 320, 320));
            if (results.size() <= 0){
                return;
            }

            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                double l = location.left;   //x
                double t = location.top;    //y
                double r = location.right;  //w
                double b = location.bottom; //h
//              float size_video = video_width;
                location.left = location.left * VIDEO_WIDTH; //1920
                location.top = location.top * VIDEO_HIGH;
                location.right = location.right * VIDEO_WIDTH;//1920
                location.bottom = location.bottom * VIDEO_HIGH;

                //X - Y - Width - Height
                if (result.getConfidence() >= OBJ_DETECT_CONFIDENT && result.getDetectedClass() == 0) {
//
                    if (db == null) {
                        try {
                            db = new PersonDatabase();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (selectedFace.size() > 0){
                        Bitmap resize = cropBitmap(l, t, r, b, result.getX(), result.getY(), bm);
                        float[] array1 = faceRecognitionProcesser.recognize(resize);
                        Score score = db.recognize(array1,selectedFace);
                        if (score == null){
                            if (CENSOR_TPYE == 0){
                                bm = BitmapEditor.blurOverlay(bm, resize, location.left, location.top, location.right, location.bottom, CENSOR_SIZE);
                            }else if (CENSOR_TPYE == 1){
                                bm = BitmapEditor.stickerOverlay(bm, selectedSticker[0], location.left, location.top, location.right, location.bottom, CENSOR_SIZE);
                            }else {
                                Canvas cv = new Canvas(bm);
                                cv.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, getPaint());
                            }
                        }
                    }else{
                        Bitmap resize = cropBitmap(l, t, r, b, result.getX(), result.getY(), bm);
                        if (CENSOR_TPYE == 0){
                            bm = BitmapEditor.blurOverlay(bm, resize, location.left, location.top, location.right, location.bottom,0);
                        }else if (CENSOR_TPYE == 1){
                            bm = BitmapEditor.stickerOverlay(bm, selectedSticker[0], location.left, location.top, location.right, location.bottom,0);
                        }else {
                            Canvas cv = new Canvas(bm);
                            cv.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, getPaint());
                        }
                    }

                }
            }

            File output = new File(newPath);
            OutputStream os = null;
            try {
                os = new FileOutputStream(output);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
            Log.e("THREAD1", "save output #1 : " + output.toString());

            try {
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap cropBitmap(double X, double Y, double width, double height, float xPos, float yPos, Bitmap bitmap){
        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        int h = (int) Math.round((float) ((2 * (height - yPos)) * video_height));
        int w = (int) Math.round((float) ((2 * (width - xPos)) * video_width));
        int x = (int) Math.round((float) (X * video_width));
        int y = Math.round((float) (Y * video_height));

        Bitmap bitmapResized = BitmapEditor.getResizedBitmap(bitmap, video_width, video_height);
        Bitmap bitmapCropFace = BitmapEditor.crop(bitmapResized, x, y, w - 1, h - 1);
        return bitmapCropFace;
    }

    MediaPlayer.OnCompletionListener myVideoViewCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer arg0) {
            Toast.makeText(FFmpegProcessActivity.this, "End of Video", Toast.LENGTH_LONG).show();
        }
    };

    MediaPlayer.OnPreparedListener MyVideoViewPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            duration = videoView.getDuration(); //in millisecond
            sk.setMax(videoView.getDuration());
            sk.postDelayed(onEverySecond, 1000);
            TextView text_view_video_time_duration = findViewById(R.id.text_view_video_time_duration);
            text_view_video_time_duration.setText("" + milisecToTimeFormat(videoView.getDuration()));

        }
    };

    private final Runnable onEverySecond = new Runnable() {
        @Override
        public void run() {
            if(sk != null) {
                sk.setProgress(videoView.getCurrentPosition());
                TextView text_view_video_time_current = findViewById(R.id.text_view_video_time_current);
                text_view_video_time_current.setText("" + milisecToTimeFormat(videoView.getCurrentPosition()));

            }
            if(videoView.isPlaying()) {
                sk.postDelayed(onEverySecond, 1000);
            }
        }
    };

    MediaPlayer.OnErrorListener myVideoViewErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(FFmpegProcessActivity.this,"Error!!!",Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public void faceDectecFrame(Bitmap bitmap){
        clearFocus();
        Bitmap resize = BitmapEditor.getResizedBitmap(bitmap, 320, 320);
        List<Classifier.Recognition> results = detector.recognizeImage(resize);

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            double l = location.left;   //x
            double t = location.top;    //y
            double r = location.right;  //w
            double b = location.bottom; //h

            float size_video = 1080;
            location.left = location.left * size_video; //1920
            location.top = location.top * 607;
            location.right = location.right * size_video;//1920
            location.bottom = location.bottom * 607;

            //X - Y - Width - Height
            if (result.getConfidence() >= OBJ_DETECT_CONFIDENT && result.getDetectedClass() == 0) {
                if (db == null) {
                    try {
                        db = new PersonDatabase();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Bitmap bCheck = cropBitmap(l, t, r, b, result.getX(), result.getY(), resize);
                bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                float[] array1 = faceRecognitionProcesser1.recognize(bCheck);
                Score score = db.recognize(array1);

                if (score != null){
                    setFocusView(l, t, r, b,  "", result.getX(), result.getY(), true);
                }else {
                    setFocusView(l, t, r, b,  "", result.getX(), result.getY(), false);
                }
            }
        }
    }

    public void setFocusView(double X, double Y, double width, double height, String id, float xPos, float yPos, boolean faceCheck) {
        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        int h = (int) Math.round((float) ((2 * (height - yPos)) * video_height));
        int w = (int) Math.round((float) ((2 * (width - xPos)) * video_width));
        int x = (int) Math.round((float) (X * video_width));
        int y = (int) Math.round((float) (Y * video_height));

        LayoutInflater inflater = LayoutInflater.from(FFmpegProcessActivity.this);
        @SuppressLint("InflateParams") View focus_frame = inflater.inflate(R.layout.focus_frame_white, null);

        if(faceCheck){
            focus_frame = inflater.inflate(R.layout.focus_frame, null);
        }

        TextView txt = new TextView(this);
        txt.setTextSize(12);
        txt.setTextColor(Color.WHITE);
        txt.setSingleLine(true);
        txt.setPadding(30, 10, 10, 10);
        txt.setGravity(Gravity.CENTER_VERTICAL|Gravity.BOTTOM);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params.height = h;
        params.width = w;
        params.setMargins(x + h, y, 0, 0);

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(x, y, 0, 0);

        LinearLayout layoutTOP = new LinearLayout(FFmpegProcessActivity.this);
        layoutTOP.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout layoutInner;
        layoutInner = new LinearLayout(FFmpegProcessActivity.this);
        layoutTOP.setTag(id);
        layoutInner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layoutInner.addView(focus_frame);
        layoutTOP.addView(layoutInner);

        fram_focus_layout.addView(layoutTOP, params1);
    }

    public void clearFocus() {

        if (null != fram_focus_layout && fram_focus_layout.getChildCount() > 0) {
            fram_focus_layout.removeViews(0, fram_focus_layout.getChildCount());
        }
    }

    ArrayList<String> selectedFace = new ArrayList<>();
    Bitmap[] selectedSticker = new Bitmap[1];
    int[] idSticker = new int[1];

    String strShape = "REG";    //REG - CIR

    @Override
    public void onClick(View view) {
        View layoutView = getLayoutInflater().inflate(R.layout.dialog_option_shap, null);
        switch (view.getId()) {
            case R.id.button_video_play:
                if (videoView.isPlaying()){
                    videoView.pause();
                    button_video_play.setImageResource((android.R.drawable.ic_media_play));
                }else {
                    button_video_play.setImageResource(android.R.drawable.ic_media_pause);
                    videoView.start();
                    clearFocus();
                    sk.postDelayed(onEverySecond, 1000);
                }
                break;
            case R.id.nextBtt:
                makeText(this, String.valueOf(CENSOR_TPYE), LENGTH_SHORT).show();
                dialogBuilder = new AlertDialog.Builder(FFmpegProcessActivity.this);
                View layoutView_Render = getLayoutInflater().inflate(R.layout.dialog_option_render, null);
                dialogBuilder.setView(layoutView_Render);
                alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);

                // Dialog Spinner
                //Spinner
                Spinner spFrameRate = layoutView_Render.findViewById(R.id.spinner_frame_rate_selector);
                Spinner spVdieoResol = layoutView_Render.findViewById(R.id.spinner_video_resol_selector);

                ArrayAdapter spFrameRateVale = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, frame_rate);
                spFrameRateVale.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spFrameRate.setAdapter(spFrameRateVale);

                ArrayAdapter spVideoResol = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, video_resolution);
                spVideoResol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spVdieoResol.setAdapter(spVideoResol);

                String VideoTime = "00:05:00";
                //String path = "Storage/Movies/";

                //Dialog Content
                TextView textview_vide_detail = layoutView_Render.findViewById(R.id.textview_vide_detail);
                textview_vide_detail.setText("TIME : " + VideoTime + "\nPath : " + path);

                EditText edittext_video_name = layoutView_Render.findViewById(R.id.edittext_video_name);
                edittext_video_name.setEnabled(false);

                Button button_render_video = layoutView_Render.findViewById(R.id.button_render_video);
                button_render_video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        progressDialogP2 = new ProgressDialog(FFmpegProcessActivity.this);
                        progressDialogP2.setMessage("Loading..."); // Setting Message
                        progressDialogP2.setTitle("RENDER"); // Setting Title
                        progressDialogP2.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Spinner
                        progressDialogP2.setMax(100);
                        progressDialogP2.show();
                        progressDialogP2.setCancelable(false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ExtractVideoFrame("-i " + path + inputVideo + " -r " + video_frame_rate + " -threads 3 " + path + tempeFramePool + tempeFrame);
                                //progress();
                                while (true) {
                                    if (p1) {
                                        progressDialogP2.incrementProgressBy(20);
                                        break;
                                    }
                                }
                                int savedNum = 0;
                                int updateNum = 0;
                                IMAGENUM = getListFile().length;

                                while (true){
                                    float newNum = ( ((float) processedIMAGE1 + (float)processedIMAGE2 + (float)processedIMAGE3 + (float)processedIMAGE4) / (float)IMAGENUM) * (float)20;
                                    if(newNum > 0){
                                        updateNum = (int)newNum - savedNum;
                                        savedNum = (int)newNum;
                                    }

                                    progressDialogP2.incrementProgressBy(updateNum);
                                    if(p2){
                                        break;
                                    }
                                }

                                while (true){
                                    if(p3){
                                        progressDialogP2.incrementProgressBy(20);
                                        break;
                                    }
                                }

                                while (true){
                                    if(p4){
                                        progressDialogP2.incrementProgressBy(20);
                                        break;
                                    }
                                }

                                while (true){
                                    if(p5){
                                        progressDialogP2.incrementProgressBy(20);
                                        progressDialogP2.dismiss();
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                                        Uri data = Uri.parse(path + finalVideoResulte);
                                        intent.setDataAndType(data, "video/mp4");
                                        startActivity(intent);
                                        finish();
                                        break;
                                    }
                                }
                            }
                        }).start();
                    }
                });

                alertDialog.show();

                break;

            case R.id.button_option_shape:
                dialogBuilder = new AlertDialog.Builder(FFmpegProcessActivity.this);
                View layoutView_dialog_option_shap = getLayoutInflater().inflate(R.layout.dialog_option_shap, null);
                dialogBuilder.setView(layoutView_dialog_option_shap);
                alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);

                //SHAPE
                RadioButton radio_shape_reg = layoutView_dialog_option_shap.findViewById(R.id.radio_shape_reg);
                radio_shape_reg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        strShape = "REG";
                        makeText(FFmpegProcessActivity.this, "REG", LENGTH_SHORT).show();
                    }
                });

                RadioButton radio_shape_cir = layoutView_dialog_option_shap.findViewById(R.id.radio_shape_cir);
                radio_shape_cir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        strShape = "CIR";
                        makeText(FFmpegProcessActivity.this, "CIR", LENGTH_SHORT).show();
                    }
                });

                //SHAPE COLOR
                RadioButton radio_color = layoutView_dialog_option_shap.findViewById(R.id.radio_color_black);
                radio_color.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        shapeColor = Color.parseColor("#000");
                        makeText(FFmpegProcessActivity.this, "COLOR", LENGTH_SHORT).show();
                    }
                });

                TextView text_shape_size = layoutView_dialog_option_shap.findViewById(R.id.text_shape_size);

                //SEEKBAR SHAPE SIZE
                SeekBar seekbar_shape_size = layoutView_dialog_option_shap.findViewById(R.id.seekbar_shape_size);
                seekbar_shape_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        text_shape_size.setText(i + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                TextView button_shape_ok = layoutView_dialog_option_shap.findViewById(R.id.button_shape_ok);
                button_shape_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        renderOptionSelect("SHAPE", seekbar_shape_size.getProgress());
                        disableButton(button_option_shape);
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
                break;
            case R.id.button_option_blur:
                dialogBuilder = new AlertDialog.Builder(FFmpegProcessActivity.this);
                View layoutView_dialog_option_blur = getLayoutInflater().inflate(R.layout.dialog_option_blur, null);
                dialogBuilder.setView(layoutView_dialog_option_blur);
                alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);
                alertDialog.show();

                TextView text_blur_radius = layoutView_dialog_option_blur.findViewById(R.id.text_blur_radius);
                TextView text_blur_size = layoutView_dialog_option_blur.findViewById(R.id.text_blur_size);

                SeekBar sbBlurRadius = layoutView_dialog_option_blur.findViewById(R.id.seekbar_blur_radius);
                sbBlurRadius.setMax(100);
                sbBlurRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        text_blur_radius.setText(i + "");
                       // makeText(FFmpegProcessActivity.this, "sbBlurRadius : " + i, LENGTH_SHORT).show();

                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                SeekBar sbBlurSize = layoutView_dialog_option_blur.findViewById(R.id.seekbar_blur_size);
                sbBlurSize.setMax(100);
                sbBlurSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        text_blur_size.setText(i + "");
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                TextView button_blur_ok = layoutView_dialog_option_blur.findViewById(R.id.button_blur_ok);
                button_blur_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        renderOptionSelect("BLUR", sbBlurSize.getProgress());
                        disableButton(button_option_blur);
                        alertDialog.dismiss();
                    }
                });

                break;

            case R.id.button_option_sticker:
                dialogBuilder = new AlertDialog.Builder(FFmpegProcessActivity.this);
                View layoutView_dialog_option_sticker = getLayoutInflater().inflate(R.layout.dialog_option_sticker, null);
                dialogBuilder.setView(layoutView_dialog_option_sticker);
                alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);

                GridViewStickerListSelectorAdapter dapterViewAndroid = new GridViewStickerListSelectorAdapter(FFmpegProcessActivity.this, selectedSticker,idSticker,null);
                GridView androidGridView = layoutView_dialog_option_sticker.findViewById(R.id.GridView_stricker);
                androidGridView.setAdapter(dapterViewAndroid);
                alertDialog.show();
                TextView text_sticker_size = layoutView_dialog_option_sticker.findViewById(R.id.text_sticker_size);

                SeekBar seekbar_sticker_size = layoutView_dialog_option_sticker.findViewById(R.id.seekbar_sticker_size);
                seekbar_sticker_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        text_sticker_size.setText(i + "");

                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                TextView button_sticker_ok = layoutView_dialog_option_sticker.findViewById(R.id.button_sticker_ok);
                button_sticker_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        renderOptionSelect("STICKER", seekbar_sticker_size.getProgress());
                        disableButton(button_option_sticker);
                        alertDialog.dismiss();
                    }
                });
                break;
            case R.id.button_option_face:

                dialogBuilder = new AlertDialog.Builder(FFmpegProcessActivity.this);
                View layoutView_dialog_option_face = getLayoutInflater().inflate(R.layout.dialog_option_face, null);
                dialogBuilder.setView(layoutView_dialog_option_face);
                alertDialog = dialogBuilder.create();
//                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);
                GridViewPersonListSelectorAdapter dapterViewAndroidFace = new GridViewPersonListSelectorAdapter(FFmpegProcessActivity.this, listPerson_name, listPerson_image, 0, selectedFace);
                androidGridView = layoutView_dialog_option_face.findViewById(R.id.GridView_person_list);
                androidGridView.setAdapter(dapterViewAndroidFace);
                alertDialog.show();
                break;
        }
    }

    void checkVideoSize(){
        Bitmap tBitmap = BitmapFactory.decodeFile(INPUT_PATH+"/1.jpg").copy(Bitmap.Config.ARGB_8888, true);
        VIDEO_HIGH = tBitmap.getHeight();
        VIDEO_WIDTH = tBitmap.getWidth();
    }

    void mergeVideo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                MergeImageToVideo("-f image2 -threads 2 -framerate 24 -i "+ path + VIDEO_NAME+"/%d.jpg " + path + tempeVideo);
            }
        }).start();

    }

    void processDetect(){
        processedIMAGE1 = 0;
        processedIMAGE2 = 0;
        processedIMAGE3 = 0;
        processedIMAGE4 = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                IMAGENUM = getListFile().length;
                executeDetect1();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                IMAGENUM = getListFile().length;
                executeDetect2();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                IMAGENUM = getListFile().length;
                executeDetect3();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                IMAGENUM = getListFile().length;
                executeDetect4();
            }
        }).start();
    }

    public String milisecToTimeFormat(long durationInMillis){
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minute, second);
    }

    boolean p1=false;
    boolean p2=false;
    boolean p3=false;
    boolean p4=false;
    boolean p5=false;

    Paint getPaint(){
        //Paint Default
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#000000"));
        paint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));
        return paint;
    }

    private void renderOptionSelect(String option, int size){
        CENSOR_SIZE = size;

        if("SHAPE".equals(option)){
            makeText(this, "Censor Option : SHAPE", LENGTH_SHORT).show();
            CENSOR_TPYE = 2;

        }else if("STICKER".equals(option)){
            makeText(this, "Censor Option : STICKER", LENGTH_SHORT).show();
            CENSOR_TPYE = 1;

        }else {
            makeText(this, "Censor Option : BLUR", LENGTH_SHORT).show();
            CENSOR_TPYE = 0;
        }
    }

    public void disableButton(View v){

        View vOptionBlur = findViewById(R.id.button_option_blur);
        View vOptionSticker = findViewById(R.id.button_option_sticker);
        View vOptionShape = findViewById(R.id.button_option_shape);

        float alpha = 0.35F;
        switch(v.getId()) {
            case R.id.button_option_blur:
                vOptionBlur.setAlpha(1);
                vOptionSticker.setAlpha(alpha);
                vOptionShape.setAlpha(alpha);
                break;

            case R.id.button_option_sticker:
                vOptionBlur.setAlpha(alpha);
                vOptionSticker.setAlpha(1);
                vOptionShape.setAlpha(alpha);
                break;

            case R.id.button_option_shape:
                vOptionBlur.setAlpha(alpha);
                vOptionSticker.setAlpha(alpha);
                vOptionShape.setAlpha(1);
                break;
        }
    }

}
