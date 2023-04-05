package com.cekmitl.pdpacameracensor.ImageEditor;

import android.graphics.Color;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;

import com.cekmitl.pdpacameracensor.R;

/**
 * Created by Nyxot on 21/11/2016.
 */

public class DrawingView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize, lastBrushSize;
    private boolean erase = false;

    public DrawingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing()
    {
        //get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        //change size brush
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
        drawPaint.setStrokeWidth(brushSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {

        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //makeTransparent(canvasBitmap, 50);

        drawCanvas = new Canvas(canvasBitmap);
    }

    public Bitmap makeTransparent(Bitmap src, int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        // config paint
        final Paint paint = new Paint();
        paint.setAlpha(value);
        canvas.drawBitmap(src, 0, 0, paint);
        return transBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN : drawPath.moveTo(touchX, touchY);
                                            break;
            case MotionEvent.ACTION_MOVE : drawPath.lineTo(touchX, touchY);
                                            break;
            case MotionEvent.ACTION_UP : drawCanvas.drawPath(drawPath, drawPaint);
                                            drawPath.reset();
                                            break;
            default: return false;
        }
        invalidate();
        return true;
    }

    public void setColor(String newColor)
    {
        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void setBrushSize(float newsize)
    {
        //update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newsize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastsize)
    {
        lastBrushSize = lastsize;
    }

    public float getLastBrushSize()
    {
        return lastBrushSize;
    }

    public void setErase(boolean isErase)
    {
        //set erase true or false
        erase = isErase;
        if(erase)
        {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }else
        {
            drawPaint.setXfermode(null);
        }
    }

    public void startNew()
    {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
