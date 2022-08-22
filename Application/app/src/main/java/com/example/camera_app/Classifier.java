package com.example.camera_app;

import android.graphics.Bitmap;
import android.graphics.RectF;
import java.util.List;

public interface Classifier {
    List<Recognition> recognizeImage(Bitmap bitmap);

    public class Recognition {

        private final String id;

        private final String title;

        private final Float confidence;

        private RectF location;

        private int detectedClass;

        public Recognition(final String id, final String title, final Float confidence, final RectF location, int detectedClass) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
            this.detectedClass = detectedClass;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public int getDetectedClass() {
            return detectedClass;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null)
                resultString += "[" + id + "] ";
            if (title != null)
                resultString += title + " ";
            if (confidence != null)
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            if (location != null)
                resultString += location + " ";

            return resultString.trim();
        }
    }
}
