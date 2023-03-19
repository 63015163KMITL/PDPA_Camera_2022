package com.cekmitl.pdpacameracensor;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import it.mirko.rangeseekbar.OnRangeSeekBarListener;
import it.mirko.rangeseekbar.RangeSeekBar;

public class FFmpegProcessActivity extends AppCompatActivity implements OnRangeSeekBarListener, View.OnClickListener {

    private Canvas canvas1;
    private Bitmap bitmap = null;
    private Paint paint;
    private Paint paint_sensor;

    private Button next_btt, detect_btt;
    private ImageButton button_video_play, button_video_pause;
    private int VIDEO_HIGH;
    private int VIDEO_WIDTH;
    int IMAGENUM;
    private File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    String INPUT_PATH = DOC_PATH + "/out";
    String OUTPUT_PATH = DOC_PATH + "/out2";
    private FFmpeg ffmpeg;

    public String strMessage = "";



    private int video_time;
    private int video_start_time;
    private int video_end_time;
    private int video_frame_rate = 24;
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

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    public static final int TF_OD_API_INPUT_SIZE = 320;

    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    //Face Recog
    private FaceRecogitionProcessor faceRecognitionProcesser;
    private Interpreter faceNetInterpreter;
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
    private Uri uri;

    //Seekbar
    public SeekBar sk;
    private Canvas canvas;

    boolean isT1Done = false;
    boolean isT2Done = false;
    boolean isT3Done = false;
    boolean isT4Done = false;

    public ImageView imgV;

    //RelativeLayout setFocusView
    RelativeLayout fram_focus_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_process);

        //ลบ Action Bar ออก
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        Intent intent = getIntent();
        String video_name = intent.getStringExtra("video_name"); //if it's a string you stored.

        detect_btt = findViewById(R.id.button6);
        next_btt = findViewById(R.id.nextBtt);
        button_video_play = findViewById(R.id.button_video_play);


        detect_btt.setOnClickListener(this);
        next_btt.setOnClickListener(this);
        button_video_play.setOnClickListener(this);


        fram_focus_layout = findViewById(R.id.fram_focus_layout);
        if (video_name == null){
            video_name = "input";
            VIDEO_NAME = "input";
        }else{
            inputVideo = video_name+".mp4";
            tempeFramePool = video_name+"/";
            tempeVideo = video_name+"_temp.mp4";
            audio = video_name+"/audio.mp3";
            finalVideoResulte = video_name + "_output.mp4";
            INPUT_PATH = DOC_PATH + "/" + video_name;
            OUTPUT_PATH = DOC_PATH + "/" +video_name;
            VIDEO_NAME = video_name;
        }
        uri = Uri.parse(DOC_PATH + "/" + video_name + ".mp4");

        videoView = findViewById(R.id.videoView);
        imgV = findViewById(R.id.video_frame_imageview);

        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(String.valueOf(uri));

        videoView.setVideoURI(uri);
//        myMediaController = new MediaController(this);
//        videoView.setMediaController(myMediaController);

        videoView.setOnCompletionListener(myVideoViewCompletionListener);
        videoView.setOnPreparedListener(MyVideoViewPreparedListener);
        videoView.setOnErrorListener(myVideoViewErrorListener);

        videoView.requestFocus();
//
        video_width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        video_height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
//
//        makeText(this, "video_width = " + video_width + "    video_height = " + video_height, LENGTH_SHORT).show();
//        video_width = 1080;
//        video_height = 607;

//        videoView.start();
//        myVideoView.start();

        //SeekBar
        sk = findViewById(R.id.seekBar);
//        sk.setMax(300000);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    videoView.seekTo(seekBar.getProgress());
                    clearFocus();

                    TextView text_view_video_time_current = findViewById(R.id.text_view_video_time_current);
                    text_view_video_time_current.setText("" + milisecToTimeFormat(seekBar.getProgress()));

                    Bitmap bmFrame = retriever.getFrameAtTime(seekBar.getProgress() * 1000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC); //unit in microsecond
                    if(bmFrame == null){
                        makeText(FFmpegProcessActivity.this, "bmFrame == null!", Toast.LENGTH_LONG).show();
                    }else{
                        imgV.setImageBitmap(faceDectecFrame(bmFrame));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //makeText(FFmpegProcessActivity.this, "seekBar : " + seekBar.getProgress() + "\n Current : " + videoView.getCurrentPosition() + "\nDuration() = " + videoView.getDuration(), LENGTH_SHORT).show();
            }
        });

        RangeSeekBar rangeSeekBar = findViewById(R.id.rangeSeekBar);

        rangeSeekBar.setStartProgress(20); // default is 0
        rangeSeekBar.setEndProgress(80); // default is 50
        rangeSeekBar.setMinDifference(5); // default is 20

        rangeSeekBar.setOnRangeSeekBarListener((OnRangeSeekBarListener) FFmpegProcessActivity.this);

        try {
            Log.e("TEST", "detector OK");
            //makeText(this, "detector OK", LENGTH_SHORT).show();
            detector = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector1 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector2 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector3 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            detector4 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            Log.e("TEST", "detector ERROR");
            //makeText(this, "detector ERROR", LENGTH_SHORT).show();
            e.printStackTrace();
        }


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#FFC733"));
        paint.setStrokeWidth(5);

        //paint_sensor
        paint_sensor = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_sensor.setStyle(Paint.Style.FILL);
        paint_sensor.setColor(Color.parseColor("#000000"));
        paint_sensor.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        //Face Recog
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }
        faceRecognitionProcesser = new FaceRecogitionProcessor(faceNetInterpreter);

        distance = new EuclideanDistance();

        File f = new File(INPUT_PATH);
        if (!(f.exists())){
            f.mkdirs();
        }

        File f2 = new File(OUTPUT_PATH);
        if (!(f2.exists())){
            f2.mkdirs();
        }


//        video_frame_rate = 25;
//        video_height = 480;
//        video_width = 480;

//        ExtractVideoFrame("-i " + path + inputVideo + " -vf scale=" + video_height + ":" + video_width + " -r " + video_frame_rate + " " + path + tempeFramePool + tempeFrame);
//        ExtractVideoAudio("-i "+ path + inputVideo + "  -vn -ar 44100 -ac 2 -ab 192k -f mp3 "+ path + audio);
//        MergeImageToVideo("-f image2 -framerate " + video_frame_rate + " -i "+ path + tempeFramePool + tempeFrame + " -c:v libx264 " + path + tempeVideo);
//        MergeAudioToVideo("-i "+ path + tempeVideo + " -i " + path + audio + "-c:v copy -c:a aac " + path + finalVideoResulte);

//        //ExtractVideoFrame
//                cmdFFmpeg("-i " + path + inputVideo + " -vf scale=" + video_height + ":" + video_width + " -r " + video_frame_rate + " " + path + tempeFramePool + tempeFrame);
//        //ExtractVideoAudio
//                cmdFFmpeg("-i "+ path + inputVideo + "  -vn -ar 44100 -ac 2 -ab 192k -f mp3 "+ path + audio);
//        //MergeImageToVideo
//                cmdFFmpeg("-f image2 -framerate " + video_frame_rate + " -i "+ path + tempeFramePool + tempeFrame + " -c:v libx264 " + path + tempeVideo);
//        //MergeAudioToVideo
//                cmdFFmpeg("-i "+ path + tempeVideo + " -i " + path + audio + "-c:v copy -c:a aac " + path + finalVideoResulte);


//        executeDetect();
//        try {
//            execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void cmdFFmpeg(String cmd){
        int rc = FFmpeg.execute(cmd);

        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }

    }

    public void ExtractVideoFrame(String cmd) {
        //FFmpeg ff = FFmpeg.getInstance(getApplicationContext());
        int rc = FFmpeg.execute(cmd);
        p1 = true;

        if (rc == RETURN_CODE_SUCCESS) {

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
            deleteFolder(INPUT_PATH);
            deleteFiles(DOC_PATH+"/"+tempeVideo);

//        myMediaController = new MediaController(this);
//        videoView.setMediaController(myMediaController);


//            makeText(FFmpegProcessActivity.this, "Success", LENGTH_SHORT).show();
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }

    }

    @Override
    public void onRangeValues(RangeSeekBar rangeSeekBar, int start, int end) {

    }

    long startTime ;
    int processedIMAGE1 = 0;
    int processedIMAGE2 = 0;
    int processedIMAGE3 = 0;
    int processedIMAGE4 = 0;
    public void executeDetect1(){
        try {
            boolean isT1Done = false;

            long h1 = 1;
            long h2 = Math.round(IMAGENUM * 0.25);
            for (long i = h1;i<h2;i++){

                startTime = System.nanoTime();
                Log.d("CHECKVERSION", "Thread 1 run: " + i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";


                    FaceDetectionInPicture(oldPath, detector1);
                    processedIMAGE1++;
//                                Files.move(oldPath, newPath);
                }
            }
            isT1Done = true;
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
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }

    public void executeDetect2(){
        try {
            isT2Done = false;

            long h1 = Math.round(IMAGENUM*0.25);
            long h2 = Math.round(IMAGENUM*0.50);
            for (long i = h1;i<=h2;i++){

                startTime = System.nanoTime();
                Log.d("CHECKVERSION", "Thread 2 run: " + i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";


                    FaceDetectionInPicture(oldPath, detector2);
                    processedIMAGE2++;
//                                Files.move(oldPath, newPath);
                }
            }
            isT2Done = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }

    public void executeDetect3(){
        try {

            isT3Done = false;

            long h1 = Math.round(IMAGENUM*0.50);
            long h2 = Math.round(IMAGENUM*0.75);
            for (long i = h1;i<=h2;i++){

                startTime = System.nanoTime();
                Log.d("CHECKVERSION", "Thread 3 run: " + i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";


                    FaceDetectionInPicture(oldPath, detector3);
                    processedIMAGE3++;
//                                Files.move(oldPath, newPath);
                }
            }
            isT3Done = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }

    public void executeDetect4(){
        try {
            isT4Done = false;
            long h1 = Math.round(IMAGENUM*0.75);
            long h2 = IMAGENUM;
            for (long i = h1;i<=h2;i++){

                startTime = System.nanoTime();
                Log.d("CHECKVERSION", "Thread 4 run: " + i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";


                    FaceDetectionInPicture(oldPath, detector4);
                    processedIMAGE4++;
//                                Files.move(oldPath, newPath);
                }
            }
            isT4Done = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }



    public void FaceDetectionInPicture(String IMG_PATH,Classifier detector) throws IOException {

        bitmap = BitmapFactory.decodeFile(IMG_PATH);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        handleResult(bitmap,IMG_PATH,detector);

    }


    private File[] getListFile(){
        return new File(INPUT_PATH).listFiles();
    }

    PersonDatabase db;
    int count = 1;
    private boolean handleResult(Bitmap bm,String newPath,Classifier detector) throws IOException {
        //clearFocus();

        if (bm == null) {
            makeText(this, "ERROR", LENGTH_SHORT).show();
        } else {
//            Bitmap resize = ;
            List<Classifier.Recognition> results = detector.recognizeImage(BitmapEditor.getResizedBitmap(bm, 320, 320));
            if (results.size() <= 0){
                Log.e("FFmpeg", "Results FAILLLLLLLLL");
                return false;
            }
            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();

//                            float size_video = video_width;
                location.left = location.left * VIDEO_WIDTH; //1920
                location.top = location.top * VIDEO_HIGH;
                location.right = location.right * VIDEO_WIDTH;//1920
                location.bottom = location.bottom * VIDEO_HIGH;

                //                           X - Y - Width - Height
                if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                    Canvas cv = new Canvas(bm);
                    cv.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint_sensor);
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
        Log.e("FFmpeg", "Results OK");
        return true;
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

    public Bitmap drawFrameBitmap(Bitmap bitmap) {
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        //Bitmap temeBitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);

        List<Classifier.Recognition> results = detector1.recognizeImage(BitmapEditor.getResizedBitmap(bitmap, 320, 320));
        for (final Classifier.Recognition result : results) {

            final RectF location = result.getLocation();

            double l = location.left;   //x
            double t = location.top;    //y
            double r = location.right;  //w
            double b = location.bottom; //h

            float size_video = 480;
            location.left = location.left * 1080; //1920
            location.top = location.top * 607;
            location.right = location.right * 1080;//1920
            location.bottom = location.bottom * 607;

            //X - Y - Width - Height
            if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
                if (db == null) {
                    try {
                        db = new PersonDatabase();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

//                bCheck = cropBitmap(l, t, r, b, result.getX(), result.getY(), resize);
                Canvas cv = new Canvas(bitmap);
                canvas.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint);
            }
        }
        return bitmap;
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
            //Toast.makeText(FFmpegProcessActivity.this,"Duration: " + duration + " (ms)",Toast.LENGTH_LONG).show();

            TextView text_view_video_time_duration = findViewById(R.id.text_view_video_time_duration);
            text_view_video_time_duration.setText("" + milisecToTimeFormat(videoView.getDuration()));

        }
    };

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run() {
//                    makeText(FFmpegProcessActivity.this, "onEverySecond OK", LENGTH_SHORT).show();
            if(sk != null) {
                sk.setProgress(videoView.getCurrentPosition());
//                        makeText(FFmpegProcessActivity.this, "Runnable OK", LENGTH_SHORT).show();
                TextView text_view_video_time_current = findViewById(R.id.text_view_video_time_current);
                text_view_video_time_current.setText("" + milisecToTimeFormat(videoView.getCurrentPosition()));
            }

            if(videoView.isPlaying()) {
//                        makeText(FFmpegProcessActivity.this, "isPlaying OK", LENGTH_SHORT).show();
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

    public Bitmap faceDectecFrame(Bitmap bitmap){
        clearFocus();
        Bitmap resize = BitmapEditor.getResizedBitmap(bitmap, 320, 320);
        List<Classifier.Recognition> results = detector.recognizeImage(resize);

        for (final Classifier.Recognition result : results) {

            final RectF location = result.getLocation();

            double l = location.left;   //x
            double t = location.top;    //y
            double r = location.right;  //w
            double b = location.bottom; //h
//
            float size_video = 1080;
            location.left = location.left * size_video; //1920
            location.top = location.top * size_video;
            location.right = location.right * size_video;//1920
            location.bottom = location.bottom * size_video;

            //                           X - Y - Width - Height
            if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && result.getDetectedClass() == 0) {
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

                float[] array1 = faceRecognitionProcesser.recognize(bCheck);
                Score score = db.recognize(array1);

                if (!(score == null)){
                    setFocusView(l, t, r, b,  "", result.getX(), result.getY(), true);
//                    canvas.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint);
                }else {
                    setFocusView(l, t, r, b,  "", result.getX(), result.getY(), false);
//                    canvas.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint_sensor);
                }
            }
        }
        return bitmap;
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


        //------------------------------------------------------------------------------------------------------

        //focus_frame.setOnClickListener(view -> frameFocusOnClickListener(id));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params.height = h;
        params.width = w;
        params.setMargins(x + h, y, 0, 0);

        //-------------------------------------------------------------------
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params1.height = h;
        params1.width = w;
        params1.setMargins(x, y, 0, 0);

        LinearLayout layoutTOP = new LinearLayout(FFmpegProcessActivity.this);
        layoutTOP.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //layoutTOP.setOrientation(LinearLayout.HORIZONTAL);
//        layoutTOP.setId(parseInt(id));
//        faceID[layoutTOP.getId()][0] = "T";
//        layoutTOP.setOnClickListener(view -> frameFocusOnClickListener(id));
//        layoutTOP.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                frameFocusOnLongClickListener(id, b);
//                return false;
//            }
//        });

        LinearLayout layoutInner;
        layoutInner = new LinearLayout(FFmpegProcessActivity.this);
        layoutTOP.setTag(id);
        Log.e("TAG", "layoutTOP TAG = " + layoutTOP.getTag());

        layoutInner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        layoutInner.addView(focus_frame);
        layoutTOP.addView(layoutInner);


        fram_focus_layout.addView(layoutTOP, params1);
        //fram_focus_layout.addView(txt, params1);


    }

    public void clearFocus() {

        if (null != fram_focus_layout && fram_focus_layout.getChildCount() > 0) {
            fram_focus_layout.removeViews(0, fram_focus_layout.getChildCount());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_video_play:
                button_video_play.setImageResource((android.R.drawable.ic_media_pause));
                if (videoView.isPlaying()){
                    videoView.pause();
                }else {
                    button_video_play.setImageResource(android.R.drawable.ic_media_play);
                    videoView.start();
                    clearFocus();
                    sk.postDelayed(onEverySecond, 1000);
                }
                break;
            case R.id.nextBtt:


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //                    ExtractVideoFrame("-i " + path + inputVideo + " -r " + video_frame_rate + " -threads 3 " + path + tempeFramePool + tempeFrame);
//                        ExtractVideoAudio("-i "+ path + inputVideo + "  -vn -ar 44100 -ac 2 -ab 192k -f mp3 "+ path + audio);

//                        ExtractVideoAudio("-i "+ path + inputVideo + "  -vn -ar 44100 -ac 2 -ab 192k -f mp3 "+ path + audio);
                        ExtractVideoFrame("-i " + path + inputVideo + " -r " + video_frame_rate + " -threads 3 " + path + tempeFramePool + tempeFrame);
                        progress();

//                        MergeImageToVideo("-f image2 -threads 3 -framerate 24 -i "+ path + "out/%d.jpg " + path + "out.mp4");
                    }
                }).start();

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                }).start();
                break;
//            case R.id.nextBtt:
//                mergeVideo();
//
//                break;
        }
    }

    void checkVideoSize(){
//        File f = new File(INPUT_PATH+"/out/1.jpg");
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
        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

//        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        String time = String.format("%02d:%02d", minute, second);
        return time;
    }



    Thread progressThread;
    boolean p1=false;
    boolean p2=false;
    boolean p3=false;
    boolean p4=false;
    boolean p5=false;


    void progress(){
        progressThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("PROGRESSVIDEO", "Starting ...");
                Log.d("PROGRESSVIDEO", "EXTRACT IMAGE : START..");
                while (true){

                    if (p1){
                        Log.d("PROGRESSVIDEO", "EXTRACT IMAGE : FINISH");
                        break;
                    }
                }
                int num = 0;
                while (true){
                    int pNum = processedIMAGE1 + processedIMAGE2 + processedIMAGE3 + processedIMAGE4;
                    if (pNum>num && pNum <= IMAGENUM){
                        num = pNum;
                        Log.d("PROGRESSVIDEO", "DETECT FACE: "+num + "/"+ IMAGENUM);
                    }
                    if (p2){
                        Log.d("PROGRESSVIDEO", "DETECT FACE : FINISH");
                        break;
                    }

                }
                Log.d("PROGRESSVIDEO", "MERGE VIDEO : START..");
                while (true) {
                    if (p3){
                        Log.d("PROGRESSVIDEO", "MERGE VIDEO : FINISH");
                        break;
                    }
                }
                Log.d("PROGRESSVIDEO", "EXTRACT AUDIO : START..");
                while (true){
                    if (p4){
                        Log.d("PROGRESSVIDEO", "EXTRACT AUDIO : FINISH");
                        break;
                    }
                }
                Log.d("PROGRESSVIDEO", "MERGE AUDIO : START..");
                while (true){
                    if (p4){
                        Log.d("PROGRESSVIDEO", "MERGE AUDIO : FINISH");
                        break;
                    }
                }
                Log.d("PROGRESSVIDEO", "RENDER COMPLETE!!");
            }
        });

        progressThread.start();
    }

    void deleteFolder(String path){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File f = new File(path);
                for (File child : f.listFiles()){
                    child.delete();
                }
                f.delete();
            }
        }).start();

    }



    void deleteFiles(String path){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File f = new File(path);
                f.delete();
            }
        }).start();

    }
}
