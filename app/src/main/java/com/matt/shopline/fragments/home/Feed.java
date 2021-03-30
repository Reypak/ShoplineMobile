package com.matt.shopline.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.objects.User;

public class Feed extends Fragment {
    private Query userFeed;
    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore db;
    private RecyclerView mFeedList;
    private MyFirestorePagingAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private ViewGroup rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        userID = user.getUid();
        // view user Feed
        userFeed = db.collection(getString(R.string.users))
                .document(userID)
                .collection("feed")
                .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
        // todo : Add a priority field : { 0-50 followers = 1, 50-100 =2 }

        mFeedList = rootView.findViewById(R.id.recView);
        refreshLayout = rootView.findViewById(R.id.swipeRefresh);
        // todo: re enable
        refreshLayout.setEnabled(false);
        mFeedList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mFeedList.setLayoutManager(mLayoutManager);

        getPosts();

    /*    userFeed.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (!value.isEmpty()) {
                        getPosts();
                    } else {
                        Toast.makeText(getActivity(), "Feed is Empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });*/

        return rootView;
    }

    private void getPosts() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(2)
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(3)
                .build();


        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userFeed, config, User.class)
                .build();

        adapter = new MyFirestorePagingAdapter(options, getActivity(), rootView);
        mFeedList.setAdapter(adapter);
    }

}