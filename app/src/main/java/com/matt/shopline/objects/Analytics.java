package com.matt.shopline.objects;

public class Analytics {
    String title;
    float[] data;
    String count;
    String percentile;

    public Analytics(String title, float[] data, String count, String percentile) {
        this.title = title;
        this.data = data;
        this.count = count;
        this.percentile = percentile;
    }

    public String getTitle() {
        return title;
    }

    public float[] getData() {
        return data;
    }

    public String getCount() {
        return count;
    }

    public String getPercentile() {
        return percentile;
    }
}
