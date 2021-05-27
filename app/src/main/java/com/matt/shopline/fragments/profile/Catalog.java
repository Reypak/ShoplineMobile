package com.matt.shopline.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.objects.User;

public class Catalog extends Fragment {

    private Query userCatalog;
    private String userID;
    private RecyclerView mCatalogList;
    private ViewGroup rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_catalog, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Bundle bundle = getArguments();
        if (bundle != null) {
            // get userID stored by TabAdapter
            userID = bundle.getString("userID");
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // catalog

        if (userID != null) {
            if (bundle.containsKey("offers")) {
                // user offers only
                userCatalog = db.collection(getString(R.string.users))
                        .document(userID)
                        .collection(getString(R.string.catalog).toLowerCase()).orderBy("offers");
            } else {
                // normal user Catalog
                userCatalog = db.collection(getString(R.string.users))
                        .document(userID)
                        .collection(getString(R.string.catalog).toLowerCase()).orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
            }
        } else {
            // view user WishList
            userID = user.getUid();
            userCatalog = db.collection(getString(R.string.users))
                    .document(userID)
                    .collection(getString(R.string.wishlist))
                    .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
        }

        mCatalogList = rootView.findViewById(R.id.recView);
//        refreshLayout = rootView.findViewById(R.id.swipeRefresh);
//        mCatalogList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mCatalogList.setLayoutManager(mLayoutManager);

        getPosts();

        return rootView;
    }

    private void getPosts() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(2)
                .setEnablePlaceholders(true)
//                .setPrefetchDistance(0)
                .setPageSize(1)
                .build();

        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userCatalog, config, User.class)
                .build();

        MyFirestorePagingAdapter adapter = new MyFirestorePagingAdapter(options, getActivity(), rootView, false);
        mCatalogList.setAdapter(adapter);
    }

}