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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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

public class FFmpegProcessActivity extends AppCompatActivity implements OnRangeSeekBarListener{

    private Canvas canvas1;
    private Bitmap bitmap = null;
    private Paint paint;
    private Paint paint_sensor;

    private Button extract_btt,detect_btt;
    int IMAGENUM;


    private FFmpeg ffmpeg;
    private File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    public String strMessage = "";

    private int video_time;
    private int video_start_time;
    private int video_end_time;
    private int video_frame_rate = 24;
    public int duration;
    private static int video_height = 720;
    private static int video_width = 720;

    public MediaMetadataRetriever mediaMetadataRetriever;
    public VideoView myVideoView;
    public MediaController myMediaController;

    //DETECT FACE
    public Classifier detector1;
    public Classifier detector2;
    public Classifier detector3;
    public Classifier detector4;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static int VIDEO_SIZE = 480;
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
    String tempeVideo = "tempeVideo.mp4";     //without audio
    String audio = "audio.mp3";
    String finalVideoResulte = "final_output.mp4";

    //Seekbar
    public SeekBar sk;
    private Canvas canvas;

    public ImageView imgV;

    //RelativeLayout setFocusView
    RelativeLayout fram_focus_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_process);

        Intent intent = getIntent();

        String video_name = intent.getStringExtra("video_name"); //if it's a string you stored.


        extract_btt = findViewById(R.id.button3);
        detect_btt = findViewById(R.id.button6);



        fram_focus_layout = findViewById(R.id.fram_focus_layout);
        if (video_name == null){
            video_name = "input";
        }
        Uri uri = Uri.parse(DOC_PATH + "/" + video_name + ".mp4");

        myVideoView = findViewById(R.id.videoView);
        imgV = findViewById(R.id.video_frame_imageview);

        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(String.valueOf(uri));

        myVideoView.setVideoURI(uri);
        myMediaController = new MediaController(this);
        myVideoView.setMediaController(myMediaController);

        myVideoView.setOnCompletionListener(myVideoViewCompletionListener);
        myVideoView.setOnPreparedListener(MyVideoViewPreparedListener);
        myVideoView.setOnErrorListener(myVideoViewErrorListener);

        myVideoView.requestFocus();
//        myVideoView.start();

        ImageView button_video_pause = findViewById(R.id.button_video_pause);
        button_video_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int currentPosition = myVideoView.getCurrentPosition();
                Bitmap bmFrame = mediaMetadataRetriever
                        .getFrameAtTime(currentPosition * 1000); //unit in microsecond

                if(bmFrame == null){
                    Toast.makeText(FFmpegProcessActivity.this, "bmFrame == null!", Toast.LENGTH_LONG).show();
                }else{
                    imgV.setImageBitmap(bmFrame);
                }

            }});


        extract_btt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ExtractVideoFrame("-i " + path + inputVideo + " -vf scale=" + video_height + ":" + video_width + " -r " + video_frame_rate + " -threads 4 " + path + tempeFramePool + tempeFrame);

                    }
                }).start();
            }
        });


        detect_btt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processedIMAGE = 0;
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
        });
        myVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myVideoView.isPlaying()){
                    myVideoView.pause();
                }else {
                    myVideoView.start();
                }
            }
        });

        //SeekBar
        sk = findViewById(R.id.seekBar);
//        sk.setMax(300000);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                myVideoView.seekTo(seekBar.getProgress());
                clearFocus();

                Bitmap bmFrame = mediaMetadataRetriever
                        .getFrameAtTime(seekBar.getProgress() * 1000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC); //unit in microsecond

                if(bmFrame == null){
                    Toast.makeText(FFmpegProcessActivity.this,
                            "bmFrame == null!",
                            Toast.LENGTH_LONG).show();
                }else{
                    imgV.setImageBitmap(faceDectecFrame(bmFrame));
                }

            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                makeText(FFmpegProcessActivity.this, "seekBar : " + seekBar.getProgress() + "\n Current : " + myVideoView.getCurrentPosition() + "\nDuration() = " + myVideoView.getDuration(), LENGTH_SHORT).show();


                    //imgV.setImageBitmap(screenshot(myVideoView));
                int currentPosition = myVideoView.getCurrentPosition(); //in millisecond
//                Toast.makeText(FFmpegProcessActivity.this,
//                        "Current Position: " + currentPosition + " (ms)",
//                        Toast.LENGTH_LONG).show();



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
                detector1 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
                detector2 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
                detector3 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
                detector4 = YoloV5Classifier.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            } catch (IOException e) {
                Log.e("TEST", "detector ERROR");
                //makeText(this, "detector ERROR", LENGTH_SHORT).show();
                e.printStackTrace();
            }

        bitmap = BitmapFactory.decodeFile("/storage/emulated/0/Download/tempfile.jpg");
        //bitmap = BitmapEditor.getResizedBitmap(bitmap, 1080, 607);
        //bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

        bitmap = Bitmap.createBitmap(bitmap);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);


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

                if (rc == RETURN_CODE_SUCCESS) {
                    Log.i(Config.TAG, "Command execution completed successfully.");
                } else if (rc == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
                    Config.printLastCommandOutput(Log.INFO);
                }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                File tFile = new File(DOC_PATH+"/out");
//                for (File child : tFile.listFiles()){
//                    child.delete();
//                }
//            }
//        }).start();

    }

    public void ExtractVideoAudio(String cmd){
    //ffmpeg -i input.mp4 -vn -ar 44100 -ac 2 -ab 192k -f mp3 audio.mp3
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


    public void MergeImageToVideo(String cmd){
    //ffmpeg -f image2 -framerate 23.976 -i temp%05d.jpg -c:v libx264 out.mp4
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

    public void MergeAudioToVideo(String cmd){
    //ffmpeg -i video.mp4 -i audio.wav -c:v copy -c:a aac output.mp4
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

    @Override
    public void onRangeValues(RangeSeekBar rangeSeekBar, int start, int end) {

    }

    long startTime ;
    int processedIMAGE;
    public void executeDetect1(){
        try {
            long h1 = 1;
            long h2 = Math.round(IMAGENUM*0.25);
            for (long i = h1;i<h2;i++){

                    startTime = System.nanoTime();
                    Log.d("CHECKVERSION", "Thread 1 run: " + i);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";
                        String newPath = OUTPUT_PATH + "/" + String.valueOf(i) + ".jpg";
//                                Path oldPath = Paths.get(INPUT_PATH+"/"+String.valueOf(counting)+".jpg");
//                                Path newPath = Paths.get(OUTPUT_PATH+"/"+String.valueOf(counting)+".jpg");

                        FaceDetectionInPicture(oldPath, newPath,detector1);
                        processedIMAGE++;
//                                Files.move(oldPath, newPath);
                }
            }
            while (true){
                if (processedIMAGE >= IMAGENUM){
                    recursiveDelete();
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }

    void recursiveDelete(){
        new Thread(new Runnable() {
            @Override
            public void run() {
//                File allFile = new File(INPUT_PATH);
//                for (File child : allFile.listFiles()){
//                    child.delete();
//                }
            }
        }).start();

    }

    public void executeDetect2(){
        try {
            long h1 = Math.round(IMAGENUM*0.25);
            long h2 = Math.round(IMAGENUM*0.50);
            for (long i = h1;i<=h2;i++){

                startTime = System.nanoTime();
                Log.d("CHECKVERSION", "Thread 2 run: " + i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";
                    String newPath = OUTPUT_PATH + "/" + String.valueOf(i) + ".jpg";
//                                Path oldPath = Paths.get(INPUT_PATH+"/"+String.valueOf(counting)+".jpg");
//                                Path newPath = Paths.get(OUTPUT_PATH+"/"+String.valueOf(counting)+".jpg");

                    FaceDetectionInPicture(oldPath, newPath,detector2);
                    processedIMAGE++;
//                                Files.move(oldPath, newPath);
                }
            }
            while (true){
                if (processedIMAGE >= IMAGENUM){
                    recursiveDelete();
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }

    public void executeDetect3(){
        try {
            long h1 = Math.round(IMAGENUM*0.50);
            long h2 = Math.round(IMAGENUM*0.75);
            for (long i = h1;i<=h2;i++){

                startTime = System.nanoTime();
                Log.d("CHECKVERSION", "Thread 3 run: " + i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";
                    String newPath = OUTPUT_PATH + "/" + String.valueOf(i) + ".jpg";
//                                Path oldPath = Paths.get(INPUT_PATH+"/"+String.valueOf(counting)+".jpg");
//                                Path newPath = Paths.get(OUTPUT_PATH+"/"+String.valueOf(counting)+".jpg");

                    FaceDetectionInPicture(oldPath, newPath,detector3);
                    processedIMAGE++;
//                                Files.move(oldPath, newPath);
                }
            }
            while (true){
                if (processedIMAGE >= IMAGENUM){
                    recursiveDelete();
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }

    public void executeDetect4(){
        try {
            long h1 = Math.round(IMAGENUM*0.75);
            long h2 = IMAGENUM;
            for (long i = h1;i<=h2;i++){

                startTime = System.nanoTime();
                Log.d("CHECKVERSION", "Thread 4 run: " + i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    String oldPath = INPUT_PATH + "/" + String.valueOf(i) + ".jpg";
                    String newPath = OUTPUT_PATH + "/" + String.valueOf(i) + ".jpg";
//                                Path oldPath = Paths.get(INPUT_PATH+"/"+String.valueOf(counting)+".jpg");
//                                Path newPath = Paths.get(OUTPUT_PATH+"/"+String.valueOf(counting)+".jpg");

                    FaceDetectionInPicture(oldPath, newPath,detector4);
                    processedIMAGE++;
//                                Files.move(oldPath, newPath);
                }
            }
            while (true){
                if (processedIMAGE >= IMAGENUM){
                    recursiveDelete();
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
    }

    String INPUT_PATH = DOC_PATH + "/out";
    String OUTPUT_PATH = DOC_PATH + "/out2";

    public void FaceDetectionInPicture(String IMG_PATH, String PATH, Classifier detector) throws IOException {

            bitmap = BitmapFactory.decodeFile(IMG_PATH);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            handleResult(bitmap,PATH,detector);

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
            Bitmap resize = BitmapEditor.getResizedBitmap(bm, 320, 320);
            List<Classifier.Recognition> results = detector.recognizeImage(resize);
            if (results.size() <= 0){
                Log.e("FFmpeg", "Results FAILLLLLLLLL");
                return false;
            }
                        for (final Classifier.Recognition result : results) {

                            final RectF location = result.getLocation();

                            double l = location.left;   //x
                            double t = location.top;    //y
                            double r = location.right;  //w
                            double b = location.bottom; //h
//

                            float size_video = video_width;
                            location.left = location.left * size_video; //1920
                            location.top = location.top * size_video;
                            location.right = location.right * size_video;//1920
                            location.bottom = location.bottom * size_video;

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

    public static Bitmap loadBitmapFromView(View v) {
        if (v.getMeasuredHeight() > 0) {
            v.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            v.draw(c);
            return b;
        }
        return null;
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
                //Canvas cv = new Canvas(bitmap);
                canvas.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint);
            }
        }
        return bitmap;
    }

    MediaPlayer.OnCompletionListener myVideoViewCompletionListener =
            new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    Toast.makeText(FFmpegProcessActivity.this, "End of Video",
                            Toast.LENGTH_LONG).show();
                }
            };

    MediaPlayer.OnPreparedListener MyVideoViewPreparedListener =
            new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    duration = myVideoView.getDuration(); //in millisecond
                    sk.setMax(duration);
                    Toast.makeText(FFmpegProcessActivity.this,
                            "Duration: " + duration + " (ms)",
                            Toast.LENGTH_LONG).show();

                }
            };

    MediaPlayer.OnErrorListener myVideoViewErrorListener =
            new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    Toast.makeText(FFmpegProcessActivity.this,
                            "Error!!!",
                            Toast.LENGTH_LONG).show();
                    return true;
                }
            };

    protected static Bitmap screenshot(View view) {
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    public Bitmap faceDectecFrame(Bitmap bitmap){
        clearFocus();
        Bitmap resize = BitmapEditor.getResizedBitmap(bitmap, 320, 320);
        List<Classifier.Recognition> results = detector1.recognizeImage(resize);

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

        int height2 = 1080;
        int width2 = 1080;

        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
        int h = (int) Math.round((float) ((2 * (height - yPos)) * height2));
        int w = (int) Math.round((float) ((2 * (width - xPos)) * width2));
        int x = (int) Math.round((float) (X * width2));
        int y = (int) Math.round((float) (Y * height2));

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
        fram_focus_layout.addView(txt, params1);


    }

    public void clearFocus() {

        if (null != fram_focus_layout && fram_focus_layout.getChildCount() > 0) {
            fram_focus_layout.removeViews(0, fram_focus_layout.getChildCount());
        }
    }
}
