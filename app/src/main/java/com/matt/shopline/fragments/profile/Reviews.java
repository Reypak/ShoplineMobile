package com.matt.shopline.fragments.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.fragments.search.Search;
import com.matt.shopline.objects.Review;
import com.matt.shopline.objects.ReviewComment;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.orders.RatingView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import static com.matt.shopline.adapters.MyFirestorePagingAdapter.durationFromNow;

public class Reviews extends Fragment {
    FirebaseFirestore db;
    private ArrayList<Integer> mData;
    private String userID;
    private MyProgressAdapter progressAdapter;
    private TextView tvTotal;
    private RecyclerView mReviewsList;
    private ViewGroup rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_reviews, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            // get userID stored by TabAdapter
            userID = bundle.getString("userID");
        }

        db = FirebaseFirestore.getInstance();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        rootView.findViewById(R.id.progressView).setVisibility(View.INVISIBLE);
        tvTotal = rootView.findViewById(R.id.tvTotal);
        RecyclerView mProgressList = rootView.findViewById(R.id.recView);
        mReviewsList = rootView.findViewById(R.id.recView2);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(requireContext());
        mProgressList.setLayoutManager(mLayoutManager);

        mData = new ArrayList<>();
        progressAdapter = new MyProgressAdapter(mData);
        mProgressList.setAdapter(progressAdapter);

        getProgress();
        getReviews();

        return rootView;
    }

    private void getProgress() {
        if (userID != null) {
            DocumentReference reviewsRef = db.collection("reviews").document(userID);
            reviewsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            Review review = task.getResult().toObject(Review.class);

                            mData.add(review.getFive());
                            mData.add(review.getFour());
                            mData.add(review.getThree());
                            mData.add(review.getTwo());
                            mData.add(review.getOne());
                            // add total last
                            mData.add(review.getTotal());
//                    Collections.reverse(mData);
                            progressAdapter.notifyDataSetChanged();
                            tvTotal.setText(String.valueOf(review.getTotal()));
                            rootView.findViewById(R.id.progressView).setVisibility(View.VISIBLE);

                            // calculate the rating
                            float i = review.getFive() * 5 + review.getFour() * 4 + review.getThree() * 3 + review.getTwo() * 2 + review.getOne();
                            float total = review.getTotal() * 5;
                            float rate = i / total * 5;

                            TextView tvRating = rootView.findViewById(R.id.tvRating);
                            if (rate >= 5) {
                                tvRating.setText(R.string.great);
                            } else if (rate >= 4) {
                                tvRating.setText(R.string.good);
                            } else if (rate >= 3) {
                                tvRating.setText(R.string.okay);
                            } else if (rate >= 2) {
                                tvRating.setText(R.string.bad);
                            } else {
                                tvRating.setText(R.string.terrible);
                            }

                            // send result to profile
                            Intent intent = new Intent("rating");
                            intent.putExtra("rating", (int) rate);
                            requireActivity().sendBroadcast(intent);
                        }
                    }
                }
            });
        }
    }

    private void getReviews() {
        Query reviewComments = db.collection("reviews").document(userID)
                .collection("reviews")
                .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<ReviewComment> options = new FirestorePagingOptions.Builder<ReviewComment>()
                .setLifecycleOwner(this)
                .setQuery(reviewComments, config, ReviewComment.class)
                .build();

        FirestorePagingAdapter<ReviewComment, BlogViewHolder> adapter = new FirestorePagingAdapter<ReviewComment, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                if (state == LoadingState.FINISHED) {
                    if (getItemCount() == 0) {
                        MyFirestorePagingAdapter.hideProgress(rootView);
                        TextView errorText = rootView.findViewById(R.id.errorText);
                        errorText.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final ReviewComment model) {
                MyFirestorePagingAdapter.hideProgress(rootView);
                String comment = model.getComment();
                final String userID2 = getItem(position).getId();
                Date timestamp = model.getTimestamp();

                holder.setCommentData(comment, timestamp, model.getRating());

                if (userID2 != null) {
                    DocumentReference userRef = db.collection(getString(R.string.users)).document(userID2);
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User user = task.getResult().toObject(User.class);
                            String profileUrl = user.getProfileUrl();
                            String username = user.getUsername();
                            String occupation = user.getOccupation();

                            holder.setUserData(username, occupation, profileUrl, userID2);

                        }
                    });
                }

               /* holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // get reference for item to be deleted
                        deleteRef = getItem(position).getReference();
                        userID = model.getUserID();

                        registerForContextMenu(view);
                        return false;
                    }
                });*/
            }

          /*  @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                if (state == LoadingState.FINISHED) {
                    TextView errorText = findViewById(R.id.errorText);
                    errorText.setVisibility(View.GONE);
                    if (getItemCount() == 0) {
                        progressBar.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                    }
                }
            }*/
        };
        mReviewsList.setAdapter(adapter);
    }

    public static class MyProgressAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        private final ArrayList<Integer> mData;

        public MyProgressAdapter(ArrayList<Integer> mData) {
            this.mData = mData;
        }

        @NonNull
        @Override
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_item, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
            int i = mData.get(position);
            // position 5 is Total
            holder.setData(i, mData.get(5), position);
        }

        @Override
        public int getItemCount() {
            // remove last value (Total)
            return mData.size() - 1;
        }
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setData(int i, int total, int position) {
            ProgressBar progressBar = itemView.findViewById(R.id.progressBar);
            TextView tvValue = itemView.findViewById(R.id.tvValue);
            View icon = itemView.findViewById(R.id.icon);

            switch (position) {
                case 0:
                case 1:
                    setColor(progressBar, tvValue, itemView.getResources().getColor(R.color.colorGreen), icon, R.drawable.ic_mood_smile);
                    break;
                case 2:
                    setColor(progressBar, tvValue, itemView.getResources().getColor(R.color.colorHighlight), icon, R.drawable.ic_mood_neutral);
                    break;
                default:
                    setColor(progressBar, tvValue, Color.RED, icon, R.drawable.ic_mood_sad);
            }

            tvValue.setText(String.valueOf(i));
            progressBar.setProgress(i);
            progressBar.setMax(total);
        }

        private void setColor(ProgressBar progressBar, TextView tvValue, int color, View icon, int image) {
            progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            icon.setBackground(ContextCompat.getDrawable(itemView.getContext(), image));
            icon.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            tvValue.setTextColor(color);
        }
    }

    public class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCommentData(String comment, Date timestamp, int rating) {
            TextView textView = mView.findViewById(R.id.tvComment);
            TextView textView2 = mView.findViewById(R.id.tvTime);
            View ratingView = mView.findViewById(R.id.ratingView);
            TextView textView3 = mView.findViewById(R.id.tvRating);
            View iconRating = mView.findViewById(R.id.iconRating);

            if (!comment.isEmpty()) {
                textView.setText(comment);
                textView.setPadding(20, 5, 5, 5);
            } else {
                textView.setVisibility(View.GONE);
            }

            textView2.setText(durationFromNow(timestamp));

            ratingView.setVisibility(View.VISIBLE);
            new RatingView().setView(rating, textView3, iconRating);

           /* if (rating == 1) {
                setItem(textView3, iconRating, getString(R.string.terrible), android.R.color.holo_red_light, R.drawable.ic_mood_sad);
            } else if (rating == 2) {
                setItem(textView3, iconRating, getString(R.string.bad), android.R.color.holo_red_light, R.drawable.ic_mood_sad);
            } else if (rating == 3) {
                setItem(textView3, iconRating, getString(R.string.okay), R.color.colorHighlight, R.drawable.ic_mood_neutral);
            } else if (rating == 4) {
                setItem(textView3, iconRating, getString(R.string.good), R.color.colorGreen, R.drawable.ic_mood_smile);
            } else if (rating == 5) {
                setItem(textView3, iconRating, getString(R.string.great), R.color.colorGreen, R.drawable.ic_mood_smile);
            }*/
        }

     /*   private void setItem(TextView textView, View iconRating, String s, int color, int image) {
            textView.setText(s);
            textView.setTextColor(itemView.getResources().getColor(color));
            iconRating.setBackground(ContextCompat.getDrawable(itemView.getContext(), image));
            iconRating.getBackground().setColorFilter(itemView.getResources().getColor(color), PorterDuff.Mode.SRC_IN);
        }*/

        public void setUserData(String username, String occupation, String imageURL, final String userID) {
            final TextView textView = mView.findViewById(R.id.tvUsername);
            TextView textView2 = mView.findViewById(R.id.tvOccupation);
            ImageView img = mView.findViewById(R.id.profile_image);

            textView.setText(username);
            if (occupation.isEmpty()) {
                textView2.setVisibility(View.GONE);
            } else {
                textView2.setText(occupation);
            }

            Picasso.get()
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open profile
                    Search.openActivity(FeedUserProfile.class, requireContext(), "userID", userID);
                }
            });
        }
    }
}