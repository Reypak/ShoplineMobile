package com.matt.shopline.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.matt.shopline.R;
import com.matt.shopline.fragments.home.Feed;
import com.matt.shopline.fragments.home.Suggestions;
import com.matt.shopline.screens.Orders;

public class Home extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
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

        // get shared prefs
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        sharedPreferences.edit().putString(getString(R.string.title_home), null).apply();
        final String location = sharedPreferences.getString(getString(R.string.title_home), null);
        if (location != null) {
            // load Feed Fragment
            loadFragment(new Feed());
        } else {
            View fab = rootView.findViewById(R.id.fab);
            fab.setVisibility(View.GONE);
            loadFragment(new Suggestions());
        }

        /*// broadcast receiver to receive intent data from login activity
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish")) {
                    loadFragment(new Feed());
                }
            }
        };
        getActivity().registerReceiver(receiver, new IntentFilter("finish"));*/

        return rootView;
    }

    private void loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
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

    public void Orders() {
        Intent intent = new Intent(getActivity(), Orders.class);
        startActivity(intent);
    }

}
