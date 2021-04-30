package com.matt.shopline.screens.orders;

import android.graphics.PorterDuff;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.matt.shopline.R;

public class RatingView {

    public void setView(int rating, TextView textView3, View iconRating) {
        if (rating == 1) {
            setItem(textView3, iconRating, iconRating.getContext().getString(R.string.terrible), android.R.color.holo_red_light, R.drawable.ic_mood_sad);
        } else if (rating == 2) {
            setItem(textView3, iconRating, iconRating.getContext().getString(R.string.bad), android.R.color.holo_red_light, R.drawable.ic_mood_sad);
        } else if (rating == 3) {
            setItem(textView3, iconRating, iconRating.getContext().getString(R.string.okay), R.color.colorHighlight, R.drawable.ic_mood_neutral);
        } else if (rating == 4) {
            setItem(textView3, iconRating, iconRating.getContext().getString(R.string.good), R.color.colorGreen, R.drawable.ic_mood_smile);
        } else if (rating == 5) {
            setItem(textView3, iconRating, iconRating.getContext().getString(R.string.great), R.color.colorGreen, R.drawable.ic_mood_smile);
        }
    }

    private void setItem(TextView textView, View iconRating, String s, int color, int image) {
        textView.setText(s);
        textView.setTextColor(textView.getResources().getColor(color));
        iconRating.setBackground(ContextCompat.getDrawable(iconRating.getContext(), image));
        iconRating.getBackground().setColorFilter(iconRating.getResources().getColor(color), PorterDuff.Mode.SRC_IN);
    }
}
