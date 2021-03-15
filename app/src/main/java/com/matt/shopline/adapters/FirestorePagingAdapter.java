package com.matt.shopline.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matt.shopline.R;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.Orders;
import com.matt.shopline.screens.PostView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestorePagingAdapter extends com.firebase.ui.firestore.paging.FirestorePagingAdapter<User, com.matt.shopline.adapters.FirestorePagingAdapter.BlogViewHolder> {

    private String imageUrl;
    private String product;
    private String postID;
    private DocumentReference postRef;
    private CollectionReference postLikes;
    private String duserID;
    private String price;
    private String offers;
    private String username;
    private CollectionReference userOrders;
    private SwipeRefreshLayout refreshLayout;

    private Query userCatalog;
    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore db;
    private RecyclerView mCatalogList;
    private FirestorePagingAdapter adapter;

    private Context context;
    public FirestorePagingAdapter(FirestorePagingOptions<User> options, Context context) {
        super(options);
        this.context = context;
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final User model) {
        ImageButton imageButton = holder.mView.findViewById(R.id.btnOptions);
        View btnOrder = holder.mView.findViewById(R.id.btnOrder);
        ImageView img = holder.mView.findViewById(R.id.profile_image);
        final ImageButton btnLike = holder.mView.findViewById(R.id.btnLike);
        final TextView tvLikes = holder.mView.findViewById(R.id.tvLikes);
        db = FirebaseFirestore.getInstance();
        postID = getItem(position).getId();
        postRef = db.collection("posts").document(postID);
//                Toast.makeText(context, postID, Toast.LENGTH_SHORT).show();
        postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        product = task.getResult().get("product").toString();
                        price = task.getResult().get("price").toString();
                        String description = task.getResult().get("description").toString();
                        imageUrl = task.getResult().get("imageUrl").toString();
                        duserID = task.getResult().get("userID").toString();
                        String timestamp = task.getResult().get("timestamp").toString();

                        // check if offers field exists
                        offers = null; // set offers value to null
                        if (task.getResult().get("offers") != null) {
                            offers = task.getResult().get("offers").toString();
                            View btnOffers = holder.mView.findViewById(R.id.btnOffers);
                            btnOffers.setVisibility(View.VISIBLE); // visible offers button
                            btnOffers.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    offers = task.getResult().get("offers").toString();
                                    String product = task.getResult().get("product").toString();
                                    // create dialog message
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(product);
                                    builder.setIcon(R.drawable.ic_gift);
                                    builder.setMessage(offers);
                                    builder.show();
                                }
                            });
                        }

                        // get comments from counter
                        if (task.getResult().get("comments") != null) {
                            String comments = task.getResult().get("comments").toString();
                            TextView tvComments = holder.mView.findViewById(R.id.tvComments);
                            tvComments.setText(comments);
                        }

                        holder.setData(product, price, description, timestamp);
                        holder.setImageURL(context, imageUrl);

                        DocumentReference userRef = db.collection("users").document(duserID);
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                username = task.getResult().get("username").toString();
                                String occupation = task.getResult().get("occupation").toString();
                                String profileUrl = task.getResult().get("profileUrl").toString();
                                holder.setUserData(username, occupation, context, profileUrl);
                            }
                        });
                    }
                }
            }
        });

        // get post likes
        postRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (value.get("likes") != null) {
                        String likes = value.get("likes").toString();
                        tvLikes.setText(likes);
                    }
                }
            }
        });

        // get post likes
        final CollectionReference likeRef = postRef.collection("likes");
               /* likeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null) {
                            // count number of Documents
                            int likes = value.getDocuments().size();
                            tvLikes.setText(String.valueOf(likes));
                        }

                    }
                });*/

        // check if current userID exists
        likeRef.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // show post liked
                    btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite));
                    tvLikes.setTextColor(Color.RED);
                }
            }
        });







        // post comments
        holder.setComments(getItem(position).getId());
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        switch (state) {
            case LOADING_INITIAL:
                // The initial load has begun
                // ...
            case LOADING_MORE:
//                refreshLayout.setRefreshing(true);
                // The adapter has started to load an additional page
                // ...
            case LOADED:
//                refreshLayout.setRefreshing(false);
                // The previous load (either initial or additional) completed
                // ...
            case ERROR:
//                refreshLayout.setRefreshing(false);
                // The previous load (either initial or additional) failed. Call
                // the retry() method in order to retry the load operation.
                // ...
            case FINISHED:
//                refreshLayout.setRefreshing(false);
        }
    }

    public class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        private TextView textView2;
        private TextView textView;
        private ImageView img;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
            img = mView.findViewById(R.id.imageView);
            // re-post button
            ImageButton btnRepost = mView.findViewById(R.id.btnRepost);
//            btnRepost.setColorFilter(getResources().getColor(R.color.colorHighlight));
        }

        public void setComments(final String postID) {
            ImageButton btnComment = mView.findViewById(R.id.btnComment);

        }

        public void setData(String product, String price, String description, String timestamp) {
            textView = mView.findViewById(R.id.tvProduct);
            textView2 = mView.findViewById(R.id.tvPrice);
            TextView textView3 = mView.findViewById(R.id.tvDesc);
            TextView textView4 = mView.findViewById(R.id.tvTime);

            // string formatter to add commas to currency
            String formatted;
            if (!price.isEmpty()) {
                int amount = Integer.parseInt(price);
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                formatted = decimalFormat.format(amount);

            } else {
                // empty value
                formatted = price + "0";
            }
            textView.setText(product);
            textView2.setText("UGX " + formatted);
            textView3.setText(description);

            SimpleDateFormat sdf = new SimpleDateFormat();
            // cast timestamp String to long
            long date = Long.parseLong(timestamp);
            // format the long value to sdf String
            String newTime = sdf.format(date);
            try {
                // convert new sdf to date
                Date d = sdf.parse(newTime);
                textView4.setText(durationFromNow(d));
            } catch (ParseException e) {
                e.printStackTrace();
            }

             /* // date converter
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("HH:mm · dd MMM yy");
            // convert timestamp string to long
            long date = Long.parseLong(timestamp);
            String newTime = sdf.format(date);
            textView4.setText(newTime);*/

        }

        public void setUserData(String username, String occupation, final Context ctx, String imageURL) {
            final TextView textView = mView.findViewById(R.id.tvUsername);
            TextView textView2 = mView.findViewById(R.id.tvOccupation);
            ImageView img = mView.findViewById(R.id.profile_image);

            textView.setText(username);
            if (occupation.isEmpty()) {
                textView2.setVisibility(View.GONE);
            } else {
                textView2.setText(occupation);
            }

            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }

        public void setImageURL(final Context ctx, String imageURL) {

            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }
    }
    public static String durationFromNow(Date startDate) {

        long different = System.currentTimeMillis() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long weeksInMilli = daysInMilli * 7;
        long monthsInMilli = daysInMilli * 30;
        long yearsInMilli = monthsInMilli * 12;

        long elapsedYears = different / yearsInMilli;
        different = different % yearsInMilli;

        long elapsedMonths = different / monthsInMilli;
        different = different % monthsInMilli;

        long elapsedWeeks = different / weeksInMilli;
        different = different % weeksInMilli;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        String output = "";
        if (elapsedYears > 0) {
            output += elapsedYears + "y";
        } else if (elapsedYears > 0 || elapsedMonths > 0) {
            output += elapsedMonths + " mon";
        } else if (elapsedMonths > 0 || elapsedWeeks > 0) {
            output += elapsedWeeks + "w";
        } else if (elapsedWeeks > 0 || elapsedDays > 0) {
            output += elapsedDays + "d";
        } else if (elapsedDays > 0 || elapsedHours > 0) {
            output += elapsedHours + "h";
        } else if (elapsedHours > 0 || elapsedMinutes > 0) {
            output += elapsedMinutes + "m";
        } else if (elapsedMinutes > 0 || elapsedSeconds > 0) {
            output += elapsedSeconds + "s";
        }

        return output;
    }
}
