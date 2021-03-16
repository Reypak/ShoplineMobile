package com.matt.shopline.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.FirestorePagingAdapter;
import com.matt.shopline.objects.User;

public class Feed extends Fragment {
    private Query userFeed;
    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore db;
    private RecyclerView mFeedList;
    private FirestorePagingAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        userID = user.getUid();
        // view user Feed
        userFeed = db.collection(getString(R.string.users))
                .document(userID)
                .collection("feed")
                .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);

        mFeedList = rootView.findViewById(R.id.recView);
        refreshLayout = rootView.findViewById(R.id.swipeRefresh);
        // todo: re enable
        refreshLayout.setEnabled(false);
        mFeedList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mFeedList.setLayoutManager(mLayoutManager);

        getPosts();

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

      /*  FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userCatalog, User.class)
                .build();*/

        adapter = new com.matt.shopline.adapters.FirestorePagingAdapter(options, getActivity());
        mFeedList.setAdapter(adapter);
    }

}