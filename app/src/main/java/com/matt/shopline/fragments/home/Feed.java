package com.matt.shopline.fragments.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.objects.User;

public class Feed extends Fragment {
    private Query userFeed;
    private RecyclerView mFeedList;
    private ViewGroup rootView;
    private MyFirestorePagingAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // view user Feed
        if (user != null) {
            userFeed = db.collection(getString(R.string.users))
                    .document(user.getUid())
                    .collection("feed")
                    .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
        }
        // todo : Add a priority field : { 0-50 followers = 1, 50-100 =2 }

        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.refresh();
            }
        });
        mFeedList = rootView.findViewById(R.id.recView);
//        mFeedList.setHasFixedSize(true);
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

        adapter = new MyFirestorePagingAdapter(options, getActivity(), rootView, true);
        mFeedList.setAdapter(adapter);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("show")) {
                    View itemView = mFeedList.getChildAt(0);
                    if (getActivity() != null) {
                        if (itemView != null) {
                           /* TapTargetView.showFor(getActivity(),
                                    TapTarget.forView(itemView.findViewById(R.id.btnOrder),
                                            "Place Order", "Make your order for any product you like")
                                            .descriptionTextSize(15)
                                            .cancelable(true)
                                            .tintTarget(true),
                                    null);*/

                            TapTargetSequence sequence = new TapTargetSequence(requireActivity())
                                    .targets(
                                            TapTarget.forView(itemView.findViewById(R.id.btnOrder),
                                                    "Place Order", "Make your order for any product you like")
                                                    .descriptionTextSize(15)
                                                    .tintTarget(false),

                                            TapTarget.forView(itemView.findViewById(R.id.btnRepost),
                                                    "Repost", "Add product to your feed, for your followers to view")
                                                    .descriptionTextSize(15)
                                                    .outerCircleColor(R.color.colorHighlight)
                                                    .tintTarget(true)
                                    )
                                    .listener(new TapTargetSequence.Listener() {
                                        @Override
                                        public void onSequenceFinish() {
                                        }

                                        @Override
                                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                                        }

                                        @Override
                                        public void onSequenceCanceled(TapTarget lastTarget) {
                                        }
                                    });
                            sequence.start();
                            sequence.considerOuterCircleCanceled(true);
                            sequence.continueOnCancel(true);
                        }
                    }
                }
            }
        };
        requireActivity().registerReceiver(receiver, new IntentFilter("show"));
    }

}