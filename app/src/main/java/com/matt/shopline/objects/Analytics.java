package com.matt.shopline.objects;

import java.util.List;

public class Analytics {
    String title;
    List<Float> data;
    String count;
    String percentile;

    public Analytics(String title, List<Float> data, String count, String percentile) {
        this.title = title;
        this.data = data;
        this.count = count;
        this.percentile = percentile;
    }

    public String getTitle() {
        return title;
    }

    public List<Float> getData() {
        return data;
    }

    public String getCount() {
        return count;
    }

    public String getPercentile() {
        return percentile;
    }
}
