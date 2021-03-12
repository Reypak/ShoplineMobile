package com.matt.shopline.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.matt.shopline.R;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.Orders;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Home extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private RecyclerView mSuggestionList;
    private GridLayoutManager mLayoutManager;
    private CollectionReference userSuggestions, userFollowers, userFollowing;
    private FirestoreRecyclerAdapter<User, BlogViewHolder> adapter;
    private String username;
    private long notificationCount;
    private TextView itemCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar1);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        userSuggestions = db.collection("users")
                .document(user.getUid())
                .collection("suggestions");

        mSuggestionList = rootView.findViewById(R.id.recView);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mSuggestionList.setLayoutManager(mLayoutManager);

        checkSuggestions();
        getSuggestions();

        return rootView;
    }

    private void getSuggestions() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(userSuggestions, User.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<User, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull User model) {
                final Button btnFollow = holder.itemView.findViewById(R.id.btnFollow);
                btnFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userID = getSnapshots().getSnapshot(position).getId();
                        // follow user
                        followUser(userID);
                    }

                    private void followUser(String userID) {
                        // followed User ref
                        userFollowers = db.collection("users")
                                .document(userID)
                                .collection("followers");

                        // current user Following Ref
                        userFollowing = db.collection("users")
                                .document(user.getUid())
                                .collection("following");

                        Map<String, Object> userdata = new HashMap<>();
                        userdata.put("exists", true);

                        // send currentUserID to followed user collection
                        userFollowers.document(user.getUid()).set(userdata);

                        // send followed userID to current user Following collection
                        userFollowing.document(userID).set(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                btnFollow.setText("Following");
                                btnFollow.setBackgroundColor(Color.parseColor("#265458F7"));
                                btnFollow.setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        });
                    }

                });
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // userID of item
                        String userID = getSnapshots().getSnapshot(position).getId();

                        Intent intent = new Intent(getActivity(), FeedUserProfile.class);
                        intent.putExtra("userID", userID);
                        startActivity(intent);
                    }
                });
                String userID = getSnapshots().getSnapshot(position).getId();
                DocumentReference userRef = db.collection(getString(R.string.users)).document(userID);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        username = task.getResult().get("username").toString();
                        String occupation = task.getResult().get("occupation").toString();
                        String profileUrl = task.getResult().get("profileUrl").toString();
                        holder.setUsername(username);
                        holder.setOccupation(occupation);
                        holder.setImageURL(getActivity(), profileUrl);
                    }
                });
            }
        };

        mSuggestionList.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, null)
                .setActionView(R.layout.basket_layout)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        FrameLayout frameLayout = (FrameLayout) menu.getItem(0).getActionView();
        itemCount = frameLayout.findViewById(R.id.badge);

        setupBadge();

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Orders();
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupBadge() {
        // number of notifications
        notificationCount = 0;
        itemCount.setVisibility(View.GONE);

        // ref to stored orders counter
        DocumentReference orders = db.collection(getString(R.string.users))
                .document(user.getUid())
                .collection("data")
                .document(getString(R.string.title_orders).toLowerCase());

        orders.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                // get value of stored long value
                if (error == null) {
                    if (value.exists()) {
                        notificationCount = value.getLong("orders");
                        if (notificationCount > 0) {
                            itemCount.setVisibility(View.VISIBLE);
                            itemCount.setText(String.valueOf(notificationCount));

                            ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1,
                                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            animation.setDuration(500);
                            itemCount.startAnimation(animation);

                        } else {
                            itemCount.setVisibility(View.GONE);
                        }

                    }
                }
            }
        });


    }

    private void checkSuggestions() {
        userSuggestions.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    // if suggestions do not exist
                    if (value.isEmpty()) {
                        // snapshot of users
                        Query userRef = db.collection("users")
                                .limit(4); // limit number of users
                        userRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                                    // getting users ID's
                                    String data = documentSnapshot.getId();
                                    /*Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();*/

                                    Map<String, Object> userdata = new HashMap<>();
                                    userdata.put("exists", true);

                                    // add ID's to current users suggestions
                                    userSuggestions.document(data).set(userdata);
                                }

                            }
                        });
                    }
                }

                   /* for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        // getting users ID's
                        String data = documentSnapshot.getId();
                        Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();
                    }*/
            }
        });

    }

   /* @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            Orders();
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void Orders() {
        Intent intent = new Intent(getActivity(), Orders.class);
        startActivity(intent);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username) {
            TextView textView = mView.findViewById(R.id.Username);
            textView.setText(username);
        }

        public void setOccupation(String occupation) {
            TextView textView = mView.findViewById(R.id.tvOccupation);
            if (occupation.isEmpty()) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setText(occupation);
            }

        }

        public void setImageURL(final Context ctx, String imageURL) {
            ImageView img = mView.findViewById(R.id.profile_image);
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }
    }

}
