package com.matt.shopline.fragments.search;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.PostView;
import com.squareup.picasso.Picasso;

public class Discover extends Fragment {
    FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ViewGroup rootView;
    private CollectionReference discoverRef;

    public static void loadFragment(FragmentActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /*private void setDiscover() {
        CollectionReference postRef = db.collection("posts");
        postRef.limit(5).whereGreaterThan("likes", 0)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot snapshot : task.getResult()) {
                    String id = snapshot.getId();

                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", new Timestamp(new Date()));

                    discoverRef.document(id).set(map);
                    Toast.makeText(requireActivity(), id, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_discover, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar1);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_discover);
        setHasOptionsMenu(true);

        db = FirebaseFirestore.getInstance();
        discoverRef = db.collection("discover");

        recyclerView = rootView.findViewById(R.id.recView);

        getPosts();

//        setDiscover();

        return rootView;
    }

    private void getPosts() {

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(discoverRef, config, User.class)
                .build();

        // delete void field
        // open profile
        FirestorePagingAdapter<User, BlogViewHolder> adapter = new FirestorePagingAdapter<User, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discover_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final User model) {
                MyFirestorePagingAdapter.hideProgress(rootView);
                String postID = getItem(position).getId();
                DocumentReference postRef = db.collection("posts").document(postID);
                postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            String imageURL = task.getResult().getString("imageUrl");
                            holder.setData(rootView.getContext(), imageURL);
                        }
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // open post
                        Search.openActivity(PostView.class, requireContext(), "postID", getItem(position).getId());
                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, null)
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            // open Search
            loadFragment(requireActivity(), new Search());
        }
        return super.onOptionsItemSelected(item);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        public BlogViewHolder(final View itemView) {
            super(itemView);
            itemView.startAnimation(AnimationUtils.loadAnimation(itemView.getContext(), R.anim.float_in));
        }

        public void setData(Context ctx, String imageURL) {
            ImageView img = itemView.findViewById(R.id.imageView);
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }
    }
}