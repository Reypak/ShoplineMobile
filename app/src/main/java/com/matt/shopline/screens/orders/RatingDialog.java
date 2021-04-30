package com.matt.shopline.screens.orders;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matt.shopline.R;
import com.matt.shopline.objects.ReviewComment;

import java.util.Date;

public class RatingDialog {
    private int rating;

    public void ratingDialog(final String userID, String product, final Context context, final DocumentReference reference, final FirebaseFirestore db) {
        final Dialog d = new Dialog(context);
        d.setContentView(R.layout.rating_dialog);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final EditText editText = d.findViewById(R.id.etReview);
        TextView textView = d.findViewById(R.id.tvMessage);
        Button btnSubmit = d.findViewById(R.id.btnSubmit);
        textView.setText(String.format("You ordered for %s, how was the service?", product));
        resetViews(d);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (rating != 0) {
                    // create comment object
                    ReviewComment reviewComment = new ReviewComment(rating, editText.getText().toString().trim(), new Date());
                    // ref to user reviews
                    DocumentReference reviewComments = db.collection("reviews").document(userID)
                            .collection("reviews").document(user.getUid());

                    reviewComments.set(reviewComment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DocumentReference userOrderRef2 = db.collection("users").document(userID)
                                    .collection("orders_customer").document(reference.getId());

                            userOrderRef2.update("status", 7); // update sellers status
                            reference.update("status", 7); // update my own status

                            Toast.makeText(view.getContext(), "Done", Toast.LENGTH_SHORT).show();
                        }
                    });
                    d.dismiss();
                } else {
                    Toast.makeText(context, "You must choose a rating", Toast.LENGTH_SHORT).show();
                }
            }
        });
        d.show();
    }

    private void resetViews(Dialog d) {
        setItemText(d, R.id.item1, R.string.terrible, android.R.color.holo_red_light, R.drawable.ic_mood_sad);
        setItemText(d, R.id.item2, R.string.bad, android.R.color.holo_red_light, R.drawable.ic_mood_sad);
        setItemText(d, R.id.item3, R.string.okay, R.color.colorHighlight, R.drawable.ic_mood_neutral);
        setItemText(d, R.id.item4, R.string.good, R.color.colorGreen, R.drawable.ic_mood_smile);
        setItemText(d, R.id.item5, R.string.great, R.color.colorGreen, R.drawable.ic_mood_smile);
    }

    private void setItemText(final Dialog d, final int item, int text, final int color, int image) {
        View itemView = d.findViewById(item);
        itemView.getBackground().clearColorFilter();
        final TextView textView = d.findViewById(item).findViewById(R.id.textView);
        final View iconView = d.findViewById(item).findViewById(R.id.iconView);
        textView.setText(text);
        textView.setTextColor(d.getContext().getResources().getColor(color));
        iconView.setBackground(ContextCompat.getDrawable(iconView.getContext(), image));
        iconView.getBackground().setColorFilter(d.getContext().getResources().getColor(color), PorterDuff.Mode.SRC_IN);
        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // clear the background from all views
                resetViews(d);
                // touched item set background filter
                view.getBackground().setColorFilter(d.getContext().getResources().getColor(color), PorterDuff.Mode.SRC_IN);
                // animate button popup
                ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(200);
                view.startAnimation(animation);
                // change text and icon color to white
                textView.setTextColor(Color.WHITE);
                iconView.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

                // pass rating
                switch (view.getId()) {
                    case R.id.item1:
                        rating = 1;
                        break;
                    case R.id.item2:
                        rating = 2;
                        break;
                    case R.id.item3:
                        rating = 3;
                        break;
                    case R.id.item4:
                        rating = 4;
                        break;
                    case R.id.item5:
                        rating = 5;
                        break;
                }
                return false;
            }
        });
    }
}
