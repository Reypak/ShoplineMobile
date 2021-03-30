package com.matt.shopline.fragments.profile;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.objects.User;

import java.util.Date;

public class Catalog extends Fragment {

    private Query userCatalog;
    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore db;
    private RecyclerView mCatalogList;
    private MyFirestorePagingAdapter adapter;
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
    private ViewGroup rootView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_catalog, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        /*user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();*/

        Bundle bundle = getArguments();
        if (bundle != null) {
            // get userID stored by TabAdapter
            userID = bundle.getString("userID");
        }

        db = FirebaseFirestore.getInstance();

        if (userID != null) {
            userCatalog = db.collection(getString(R.string.users))
                    .document(userID)
                    .collection(getString(R.string.catalog).toLowerCase()).orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
//                    .limit(3);
        } else {
            // view user WishList
            userID = user.getUid();
            userCatalog = db.collection(getString(R.string.users))
                    .document(userID)
                    .collection(getString(R.string.wishlist))
                    .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
            // negative value arrangement
        }

        mCatalogList = rootView.findViewById(R.id.recView);
        refreshLayout = rootView.findViewById(R.id.swipeRefresh);
        mCatalogList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mCatalogList.setLayoutManager(mLayoutManager);

        getPosts();

        return rootView;
    }

    private void getPosts() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(2)
                .setEnablePlaceholders(true)
//                .setPrefetchDistance(1)
                .setPageSize(1)
                .build();


        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userCatalog, config, User.class)
                .build();

      /*  FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userCatalog, User.class)
                .build();*/

        adapter = new MyFirestorePagingAdapter(options, getActivity(), rootView);
        mCatalogList.setAdapter(adapter);
    }

}