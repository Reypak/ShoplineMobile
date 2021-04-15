package com.matt.shopline.screens.follow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.squareup.picasso.Picasso;

public class FollowList extends Fragment {

    private Query userFollowers;
    private FirebaseFirestore db;
    private RecyclerView mList;
    private Bundle bundle;
    private String userID, You;
    private ViewGroup rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_followlist, container, false);

        db = FirebaseFirestore.getInstance();
        mList = rootView.findViewById(R.id.recView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mList.setLayoutManager(mLayoutManager);

        initialize();

        getFollowers();

        return rootView;
    }

    private void initialize() {
        bundle = getArguments();
        userID = bundle.getString("userID");
        You = bundle.getString("You");

        if (You == null) {
            // load followers
            userFollowers = db.collection(getString(R.string.users)).document(userID)
                    .collection(getString(R.string.followers).toLowerCase());
        } else {
            // load following
            userFollowers = db.collection(getString(R.string.users)).document(userID)
                    .collection(getString(R.string.following).toLowerCase());
        }
    }

    private void getFollowers() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userFollowers, config, User.class)
                .build();

        // delete void field
        // open profile
        FirestorePagingAdapter<User, BlogViewHolder> adapter = new FirestorePagingAdapter<User, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final User model) {
                MyFirestorePagingAdapter.hideProgress(rootView);
                String userID = getItem(position).getId();
                DocumentReference userRef = db.collection(getString(R.string.users)).document(userID);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            User user = task.getResult().toObject(User.class);
                            String profileUrl = user.getProfileUrl();
                            String username = user.getUsername();
                            String occupation = user.getOccupation();

                            holder.setUserData(username, occupation, getActivity(), profileUrl, model.getUserID());

                        } else {
                            // delete void field
                            getItem(position).getReference().delete();
                            refresh();
                        }
                    }
                });

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // open profile
                        Intent intent = new Intent(getActivity(), FeedUserProfile.class);
                        intent.putExtra("userID", getItem(position).getId());
                        startActivity(intent);
                    }
                });
            }

        };
        mList.setAdapter(adapter);
    }


    public class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserData(String username, String occupation, final Context ctx, String imageURL, final String userID) {
            final TextView textView = mView.findViewById(R.id.tvUsername);
            TextView textView2 = mView.findViewById(R.id.tvOccupation);
            ImageView img = mView.findViewById(R.id.profile_image);

            textView.setText(username);
            textView2.setVisibility(View.GONE);
            if (occupation != null) {
                if (!occupation.isEmpty()) {
                    textView2.setText(occupation);
                    textView2.setVisibility(View.VISIBLE);
                }
            }

            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);

        }

    }
}