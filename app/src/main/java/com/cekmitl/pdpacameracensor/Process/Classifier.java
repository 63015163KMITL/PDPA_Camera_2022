package com.cekmitl.pdpacameracensor.Process;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;

public interface Classifier {
    List<Recognition> recognizeImage(Bitmap bitmap);

    class Recognition {

        private final String id;

        private final Float confidence;

        private final RectF location;

        private final int detectedClass;
        private final float x;
        private final float y;

        public Recognition(final String id, final Float confidence, final RectF location, int detectedClass, float x, float y) {
            this.id = id;

            this.confidence = confidence;
            this.location = location;
            this.detectedClass = detectedClass;
            this.x = x;
            this.y = y;
        }

        public String getId() {
            return id;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public Float getX() { return x;}

        public Float getY() { return y;}

        public int getDetectedClass() {
            return detectedClass;
        }

    }
}
