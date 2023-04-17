package com.cekmitl.pdpacameracensor.ViewAdapter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cekmitl.pdpacameracensor.MainCameraActivity;

public class PickerLayoutManager extends LinearLayoutManager {

    private boolean changeAlpha = true;

    private onScrollStopListener onScrollStopListener;
    private MainCameraActivity mainCameraActivity;

    public PickerLayoutManager(Context context, int orientation, boolean reverseLayout, MainCameraActivity _mainCameraActivity) {
        super(context, orientation, reverseLayout);
        this.mainCameraActivity = _mainCameraActivity;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleDownView();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int orientation = getOrientation();
        if (orientation == HORIZONTAL) {
            int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
            scaleDownView();
            return scrolled;
        } else return 0;
    }

    private void scaleDownView() {
        float mid = getWidth() / 2.0f;
        float scaleDownDistance = 0.7f;
        float unitScaleDownDist = scaleDownDistance * mid;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            float childMid = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2.0f;
            float scaleDownBy = 0.5f;
            float scale = 1.0f + (-1 * scaleDownBy) * (Math.min(unitScaleDownDist, Math.abs(mid - childMid))) / unitScaleDownDist;
            child.setScaleX(scale);
            child.setScaleY(scale);
            if (changeAlpha) {
                child.setAlpha(scale);
            }
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        Log.e("onScrollStateChanged", "onScrollStateChanged");
        if (state == 0) {
            if (onScrollStopListener != null) {
                int selected = 0;
                float lastHeight = 0f;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    if (lastHeight < child.getScaleY()) {
                        lastHeight = child.getScaleY();
                        selected = i;
                    }
                }
                onScrollStopListener.selectedView(getChildAt(selected),selected);
            }
        }
    }

    public void setChangeAlpha(boolean changeAlpha) {
        this.changeAlpha = changeAlpha;
    }

    public void setOnScrollStopListener(onScrollStopListener onScrollStopListener) {
        this.onScrollStopListener = onScrollStopListener;
    }


    public interface onScrollStopListener {
        void selectedView(View view, int position);

    }
}