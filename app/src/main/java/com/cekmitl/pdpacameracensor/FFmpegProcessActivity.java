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
import com.cekmitl.pdpacameracensor.ui.OutputPack;

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

public class FFmpegProcessActivity extends AppCompatActivity implements OnRangeSeekBarListener{

    private Canvas canvas1;
    private Bitmap bitmap = null;
    private Paint paint;
    private Paint paint_sensor;

    private FFmpeg ffmpeg;
    private File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    public String strMessage = "";

    private int video_time;
    private int video_start_time;
    private int video_end_time;
    private int video_frame_rate = 25;
    public int duration;
    private static int video_height = 1080;
    private static int video_width = 1080;

    public MediaMetadataRetriever mediaMetadataRetriever;
    public VideoView myVideoView;
    public MediaController myMediaController;

    //DETECT FACE
    public Classifier detector1;
    public Classifier detector2;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    public static final int TF_OD_API_INPUT_SIZE = 320;
    private static int VIDEO_SIZE = 480;
    private static final String TF_OD_API_MODEL_FILE = "ModelN.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    //Face Recog
    private FaceRecogitionProcessor faceRecognitionProcesser;
    private Interpreter faceNetInterpreter;
    float CONFIDENT = 0.67f;
    EuclideanDistance distance;


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

        fram_focus_layout = findViewById(R.id.fram_focus_layout);

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
        myVideoView.start();

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

        String path = DOC_PATH + "/";
        String inputVideo = "input.mp4";
        String tempeFrame = "%d.jpg";
        String tempeFramePool = "out/";           //Folder
        String tempeVideo = "tempeVideo.mp4";     //without audio
        String audio = "audio.mp3";
        String finalVideoResulte = "final_output.mp4";

        video_frame_rate = 25;
        video_height = 480;
        video_width = 480;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();

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


    int counting1 = 1;
    int counting2 = 2;
    int FILE_NUMBER = 0;
    long startTime ;
    Thread workingThread1;

    public void executeDetect(){
        workingThread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
//                        workingThread1.join(1);

                        if (getListFile().length > 3) {
                            startTime = System.nanoTime();
                            Log.d("CHECKVERSION", "Thread 1 run: " + counting1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                String oldPath = INPUT_PATH + "/" + String.valueOf(counting1) + ".jpg";
                                String newPath = OUTPUT_PATH + "/" + String.valueOf(counting1) + ".jpg";
//                                Path oldPath = Paths.get(INPUT_PATH+"/"+String.valueOf(counting)+".jpg");
//                                Path newPath = Paths.get(OUTPUT_PATH+"/"+String.valueOf(counting)+".jpg");

                                FaceDetectionInPicture(oldPath, newPath);
//                                Files.move(oldPath, newPath);

                                counting1 += 1;
                                long endTime = System.nanoTime();
                                Log.e("CHECKVERSION", "TIME ALL = " + ((endTime - startTime) / 1000000) + " ms");

                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("SHOWLISTFILE", "NUMBER OF FILE: " + getListFile().length);
                }
            }
        });

//        workingThread2 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    try {
////                        workingThread1.join(1);
//
//
//                        if (getListFile().length > 2){
//                            Log.d("CHECKVERSION", "Thread 2 run: "+ counting2);
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                String oldPath = INPUT_PATH+"/"+String.valueOf(counting2)+".jpg";
//                                String newPath = OUTPUT_PATH+"/"+String.valueOf(counting2)+".jpg";
////                                Path oldPath = Paths.get(INPUT_PATH+"/"+String.valueOf(counting)+".jpg");
////                                Path newPath = Paths.get(OUTPUT_PATH+"/"+String.valueOf(counting)+".jpg");
//
//                                FaceDetectionInPicture(oldPath,newPath,detector2);
////                                Files.move(oldPath, newPath);
//
//                                counting2 += 2;
//                            }
//                        }
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("SHOWLISTFILE", "NUMBER OF FILE: "+getListFile().length);
//                }
//            }
//        });


        workingThread1.start();
//        workingThread2.start();


        }
    int SIZE = 480;

    String INPUT_PATH = DOC_PATH + "/out";
    String OUTPUT_PATH = DOC_PATH + "/out2";

    public void FaceDetectionInPicture(String IMG_PATH,String PATH) throws IOException {



//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //Face detection
//        for (int i = 1; i <= 7890; i++) {
//            for (int i = 435; i <= 480; i++) {
            //int i = 435;
//            Log.e("FFmpeg", "IMAGE NUM : " + i);
//            long startTime = System.nanoTime();
//            long startTime_Bitmap = System.nanoTime();
//            File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            bitmap = BitmapFactory.decodeFile(IMG_PATH);
//            bitmap = BitmapEditor.getResizedBitmap(bitmap, size_video, size_video);
            //bitmap = Bitmap.createBitmap(bitmap);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            //Bitmap temeBitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
//            canvas = new Canvas(bitmap);
//            long endTime_Bitmap = System.nanoTime();
//
//            long startTime_File = System.nanoTime();
//            long startTime_handleResult = System.nanoTime();
//            if (handleResult(bitmap)) {
                handleResult(bitmap,IMG_PATH,PATH);

                //File dir = new File("/storage/emulated/0/Download/");
//                File dir = new File("/storage/emulated/0/Download/out2/");
//                if (!dir.exists()) {
//                    dir.mkdirs();
//
//                }



//            long endTime_File = System.nanoTime();
//
//            long endTime_handleResult = System.nanoTime();
//            long endTime = System.nanoTime();
            //long duration = (endTime - startTime_Bitmap) / 1000000;
//            Log.e("FFmpeg", "");
//            Log.e("FFmpeg", "Bitmap time = " + ((endTime_Bitmap - startTime_Bitmap) / 1000000) + " ms");
//            Log.e("FFmpeg", "handleResult time = " + ((endTime_handleResult - startTime_handleResult) / 1000000) + " ms");
//            Log.e("FFmpeg", "File time = " + ((endTime_File - startTime_File) / 1000000) + " ms");
//            Log.e("FFmpeg", "TIME ALL = " + ((endTime - startTime) / 1000000) + " ms");

//      }

    }


    private File[] getListFile(){
        return new File(INPUT_PATH).listFiles();
    }

    PersonDatabase db;

    ArrayList<OutputPack> pack1 = new ArrayList<>(1500);
    ArrayList<OutputPack> pack2 = new ArrayList<>();
    ArrayList<OutputPack> pack3 = new ArrayList<>();
    int round = 1;
    private boolean handleResult(Bitmap bm,String oldPath,String newPath) throws IOException {
        //clearFocus();

        if (bm == null) {
            makeText(this, "ERROR", LENGTH_SHORT).show();
        } else {
            Bitmap resize = BitmapEditor.getResizedBitmap(bm, 320, 320);
            List<Classifier.Recognition> results = detector1.recognizeImage(resize);
            if (results.size() <= 0){
                Log.e("FFmpeg", "Results FAILLLLLLLLL");
                return false;

            }


            Log.d("PACK1", "PACK1 "+pack1.size());
//            Log.d("PACK1", "T1 "+t1.isAlive());
            switch (round){
                case 1:
                    pack1.add(new OutputPack(results,oldPath,newPath,resize,bm));
                    break;
                case 2:
                    pack2.add(new OutputPack(results,oldPath,newPath,resize,bm));
                    break;
                case 3:
                    pack3.add(new OutputPack(results,oldPath,newPath,resize,bm));
                    break;
            }
//            round+=1;
//            if (round>3){
//                round = 1;
//            }

        }

        Log.e("FFmpeg", "Results OK");
        return true;
    }


    Thread t1,t2,t3;
    int c = 0;
    public void execute() throws IOException {
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (pack1 != null){
                        if (pack1.size() > 0 && pack1.get(0).results != null) {
                            OutputPack pack = pack1.get(0);
                            List<Classifier.Recognition> results = pack.results;
                            String newPath = pack.newPath;
                            String oldPath = pack.oldPath;
                            Bitmap resize = pack.bitmap;
                            Bitmap oldImage = pack.oldIMG;
                            Bitmap bCheck = null;
                            for (final Classifier.Recognition result : results) {

                                final RectF location = result.getLocation();

                                double l = location.left;   //x
                                double t = location.top;    //y
                                double r = location.right;  //w
                                double b = location.bottom; //h
//

                                float size_video = 480;
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


//                        int width2 = bm.getWidth();
//                        int height2 = bm.getHeight();
//                        //1080 คือ ขนาดความกว้างสูงสุดของหน้าจอ
//                        int h = (int) Math.round((float) ((2 * (r - result.getY())) * height2));
//                        int w = (int) Math.round((float) ((2 * (b - result.getX())) * width2));
//                        int x = (int) Math.round((float) (l * width2));
//                        int y = (int) Math.round((float) (t * height2));
//
//                        Bitmap bb = BitmapEditor.getResizedBitmap(bm, width2, height2);
//                        //Bitmap b = BitmapEditor.crop(bb, x, y, w + persen, h + persen);

//                        Log.d("FFmpeg", "handleResult Bitmap crop ////////////////");
//                        Log.d("FFmpeg", "   X = " + x);
//                        Log.d("FFmpeg", "   Y = " + y);
//                        Log.d("FFmpeg", "   newH = " + h);
//                        Log.d("FFmpeg", "   newW = " + w);
//                        Log.d("FFmpeg", "   Bitmap H = " + bb.getHeight());
//                        Log.d("FFmpeg", "   Bitmap W = " + bb.getWidth());
//
//                        Bitmap bCheck = BitmapEditor.crop(bb, x, y, w, h);

                                    //Bitmap bCheck = AddNewFaceActivity.cropBitmap(l, t, r, b,  result.getX(), result.getY(), resize, bm, 100);
                                    bCheck = cropBitmap(l, t, r, b, result.getX(), result.getY(), resize);
                                    //Bitmap bCheck = cropBitmap(location.left, location.top, location.right, location.bottom, resize, resize, 1);
                                    //Bitmap bCheck = BitmapEditor.crop(resize, location.left, location.top, location.right, location.bottom);
//                                float[] array1 = faceRecognitionProcesser.recognize(bCheck);
//                                //db.save_image(face,"Tu");
//                                Score score = db.recognize(array1, CONFIDENT);
                                    Canvas cv = new Canvas(oldImage);
//
//                                if (!(score == null)){
//                                    cv.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint);
//                                }else {
                                    cv.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint_sensor);
//                                }
                                    //setFocusView(bm, location.left, location.top, location.right, location.bottom, result.getX(), result.getY(), 0.05);
                                    // canvas.drawRoundRect(location.left, location.top, location.right, location.bottom, 10, 10, paint_sensor);

                                }
                            }
                            if (oldImage!=null){
                                File output = new File(newPath);
                                OutputStream os = null;
                                try {
                                    os = new FileOutputStream(output);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                oldImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
                                Log.e("THREAD1", "save output : " + output.toString());
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

//                File oldFile = new File(IMG_PATH);
//                oldFile.delete();
                            pack1.remove(0);
//                        c++;
                        }
                    }
                }
                    }

        });

        t1.start();
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
                Score score = db.recognize(array1, CONFIDENT);

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
