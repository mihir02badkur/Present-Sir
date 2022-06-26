package com.raina.PresentSir.faceRecognition;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public interface SimilarityClassifier {


    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    class Recognition implements Serializable {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;
        /**
         * Display name for the recognition.
         */
        private final String title;


        private final Float distance;
        private Object extra;

        public Recognition(
                final String id, final String title, final Float distance) {
            this.id = id;
            this.title = title;
            this.distance = distance;
            this.extra = null;

        }

        public Object getExtra() {
            return this.extra;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        @NonNull
        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (distance != null) {
                resultString += String.format(Locale.getDefault(), "(%.1f%%) ", distance * 100.0f);
            }

            return resultString.trim();
        }

    }
}
