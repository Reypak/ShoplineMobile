package com.matt.shopline.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.objects.Notification;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.Orders;
import com.matt.shopline.screens.PostView;
import com.matt.shopline.screens.follow.FollowView;
import com.squareup.picasso.Picasso;

import java.util.Date;

import static com.matt.shopline.fragments.profile.Catalog.durationFromNow;

public class Notifications extends Fragment {
    private RecyclerView mList;
    private Query userNotify;
    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore db;
    private FirestorePagingAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_notifications, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar1);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_notifications);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        userID = user.getUid();
        // view user Notify
        userNotify = db.collection(getString(R.string.users))
                .document(userID)
                .collection(getString(R.string.title_notifications).toLowerCase())
                .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);

        mList = rootView.findViewById(R.id.recView);
        refreshLayout = rootView.findViewById(R.id.swipeRefresh);
        // todo: re enable
        refreshLayout.setEnabled(false);
        mList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mList.setLayoutManager(mLayoutManager);

        getNotifications();

        return rootView;
    }

    private void getNotifications() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<Notification> options = new FirestorePagingOptions.Builder<Notification>()
                .setLifecycleOwner(this)
                .setQuery(userNotify, config, Notification.class)
                .build();
        adapter = new FirestorePagingAdapter<Notification, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, int position, @NonNull final Notification model) {
                // user query
                if (model.getUserID() != null) {
                    DocumentReference userRef = db.collection(getString(R.string.users)).document(model.getUserID());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User user = task.getResult().toObject(User.class);
                            String profileUrl = user.getProfileUrl();
                            // set data
                            holder.setNotificationData(model.getState(), user.getUsername(), model.getTimestamp(), getActivity(), profileUrl, model.getUserID());
                        }
                    });
                }

                holder.loadPost(model.getPostID(), model.getState());
            }

            @Override
            public void onViewRecycled(@NonNull BlogViewHolder holder) {
                holder.setNotificationData(0, null, null, null, null, null);
            }
        };
        mList.setAdapter(adapter);
    }

    private void loadActivity(Class aClass, String name, String value) {
        Intent intent = new Intent(getActivity(), aClass);
        if (name != null) {
            intent.putExtra(name, value); // send intent to load You Tab
        }
        startActivity(intent);
    }

    public class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void loadPost(final String postID, final long state) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (postID != null) {
                        loadActivity(PostView.class, "postID", postID);
                    } else if (state == 3) {
                        loadActivity(FollowView.class, "userID", user.getUid());
                    } else if (state == 4) {
                        loadActivity(Orders.class, null, null);
                    }
                }
            });
        }

        public void setNotificationData(long state, String username, Date timestamp, Context ctx, String imageURL, final String userID) {
            TextView textView = mView.findViewById(R.id.tvNotification);
            TextView textView2 = mView.findViewById(R.id.tvTime);
            ImageView img = mView.findViewById(R.id.profile_image);
            ImageView icon = mView.findViewById(R.id.icon);

            String comment = null;
            if (state == 1) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                icon.clearColorFilter();
                comment = username + " has liked your post";
            } else if (state == 2) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_message2));
                icon.setColorFilter(Color.parseColor("#FF4CAF50"));
                comment = username + " has commented your post";
            } else if (state == 3) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_user));
                icon.setColorFilter(getResources().getColor(R.color.colorTextOverlay));
                comment = username + " followed " + getString(R.string.you).toLowerCase();
            } else if (state == 4) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_basket));
                icon.setColorFilter(getResources().getColor(R.color.colorTextOverlay));
                comment = username + " placed an order";
            }
            textView.setText(comment);
            textView2.setText(null);
            if (timestamp != null) {
                textView2.setText(durationFromNow(timestamp));
            }

            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open profile
                    Intent intent = new Intent(mView.getContext(), FeedUserProfile.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                }
            });
        }

    }
}