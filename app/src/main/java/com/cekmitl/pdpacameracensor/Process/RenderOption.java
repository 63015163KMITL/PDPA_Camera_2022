package com.cekmitl.pdpacameracensor.Process;

import android.graphics.Paint;

public class RenderOption {

    public Paint _paint;

    public int _video_frame_rate;
    public int _video_width;
    public int _video_height;

    //Censor Type

    public RenderOption(){

    }

    public RenderOption(int video_frame_rate, int video_width , int video_height,Paint paint){
        _video_frame_rate = video_frame_rate;
        _video_width = video_width;
        _video_height = video_height;
        _paint = paint;
    }

    public RenderOption(Paint paint){
        _video_frame_rate = 25;
        _paint = paint;
    }

}
