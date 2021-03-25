package com.matt.shopline.fragments.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import com.matt.shopline.fragments.Home;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Suggestions extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private RecyclerView mSuggestionList;
    private GridLayoutManager mLayoutManager;
    private CollectionReference userSuggestions, userFollowers, userFollowing;
    private FirestoreRecyclerAdapter<User, BlogViewHolder> adapter;
    private String username;
    private Button btnStart;
    private int followCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_suggestions, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // reference to suggestions
        userSuggestions = db.collection("users")
                .document(user.getUid())
                .collection("suggestions");

        mSuggestionList = rootView.findViewById(R.id.recView);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mSuggestionList.setLayoutManager(mLayoutManager);

        checkSuggestions();
        getSuggestions();

        btnStart = rootView.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                        "Getting things ready" + getString(R.string.load), true);
                dialog.show();
                // save prefs
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPreferences.edit().putString(getString(R.string.title_home), "1").apply();

                // load 5 seconds
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .replace(R.id.fragment_container, new Home())
                                .commit();
                    }
                }, 5000);

               /* // send broadcast to Landing page
                Intent intent = new Intent("finish");
                getActivity().sendBroadcast(intent);*/
            }
        });
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
                // initial state
                btnFollow.setText("Follow");
                btnFollow.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btnFollow.setTextColor(Color.WHITE);

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
                                // delete id from suggestions (2 seconds)
                                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }, 2000);
                            }
                        });
                        // increment count
                        followCount++;
                        if (followCount == 2) {
                            btnStart.setVisibility(View.VISIBLE);
                        }
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
                // get Followers
                userRef.collection("data").document(getString(R.string.followers).toLowerCase())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            TextView textView2 = holder.mView.findViewById(R.id.tvFollowers);
                            textView2.setText(String.format("%s %s",
                                    task.getResult().get(getString(R.string.followers).toLowerCase()).toString(),
                                    getString(R.string.followers).toLowerCase()));
                        }
                    }
                });
            }
        };

        mSuggestionList.setAdapter(adapter);
    }

    private void checkSuggestions() {
        userSuggestions.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().isEmpty()) {
                    // snapshot of users
                    Query userRef = db.collection("users")
                            .limit(10); // limit number of users
                    userRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                                // getting users ID's
                                String data = documentSnapshot.getId();
                                /*Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();*/
                                if (!data.equals(user.getUid())) {
                                    Map<String, Object> userdata = new HashMap<>();
                                    userdata.put("exists", true);

                                    // add ID's to current users suggestions
                                    userSuggestions.document(data).set(userdata);
                                }
                            }
                        }
                    });
                }
            }
        });
                   /* for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        // getting users ID's
                        String data = documentSnapshot.getId();
                        Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();
                    }*/

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