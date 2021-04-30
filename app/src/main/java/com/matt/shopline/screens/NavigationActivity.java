package com.matt.shopline.screens;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.matt.shopline.R;
import com.matt.shopline.fragments.Notifications;
import com.matt.shopline.fragments.analytics.Analytics;
import com.matt.shopline.fragments.home.Home;
import com.matt.shopline.fragments.profile.Profile;
import com.matt.shopline.fragments.search.Discover;
import com.matt.shopline.objects.BottomMenuHelper;

public class NavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static FirebaseUser user;
    private BottomNavigationView navigation;
    private DocumentReference notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null) {
                    //loading the default fragment
                    loadFragment(new Home());
                    notificationCounter();
                }
            }
        }, 100);

        //getting bottom navigation view and attaching the listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        startShowcase();
    }

    private void startShowcase() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("show2")) {

                    TapTargetSequence sequence = new TapTargetSequence(NavigationActivity.this)
                            .targets(
                                    TapTarget.forView(navigation.findViewById(R.id.navigation_profile),
                                            "Profile", "View and edit your profile details \n Catalog, Reviews and Wishlist")
                                            .descriptionTextSize(15)
                                            .cancelable(false),

                                    TapTarget.forView(navigation.findViewById(R.id.navigation_notifications),
                                            "Notifications", "Check your notifications and updates here")
                                            .descriptionTextSize(15)
                                            .tintTarget(false)
                            ).listener(new TapTargetSequence.Listener() {
                                @Override
                                public void onSequenceFinish() {
                                    Intent intent = new Intent("show");
                                    sendBroadcast(intent);
                                }

                                @Override
                                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                                }

                                @Override
                                public void onSequenceCanceled(TapTarget lastTarget) {
                                    onSequenceFinish();
                                }
                            });
                    sequence.start();
                }
            }
        };
        registerReceiver(receiver, new IntentFilter("show2"));
    }

    private void notificationCounter() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        notifications = db.collection(getString(R.string.users))
                .document(user.getUid())
                .collection("data")
                .document(getString(R.string.title_notifications).toLowerCase());

        notifications.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                long num;
                if (error == null) {
                    if (value.exists()) {
                        num = value.getLong(getString(R.string.title_notifications).toLowerCase());
                        // value is not null
                        if (num != 0) {
                            BottomMenuHelper.showBadge(NavigationActivity.this, navigation, R.id.navigation_notifications);
                        } else {
                            BottomMenuHelper.removeBadge(navigation, R.id.navigation_notifications);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            openLogin();
            finish();
        } else {
            user.reload();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new Home();
                break;

            case R.id.navigation_search:
                fragment = new Discover();
                break;
            case R.id.navigation_analytics:
                fragment = new Analytics();
                break;
            case R.id.navigation_notifications:
                fragment = new Notifications();
                BottomMenuHelper.removeBadge(navigation, R.id.navigation_notifications);
                // reset counter
                notifications.update(getString(R.string.title_notifications).toLowerCase(), 0);
                break;

            case R.id.navigation_profile:
                fragment = new Profile();
                break;
        }

        return loadFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        if (navigation.getSelectedItemId() == R.id.navigation_home) {
            // close app
            super.onBackPressed();
        } else {
            // load home fragment
            navigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    public void Upload(View view) {
        Intent intent = new Intent(this, Upload.class);
        startActivity(intent);
    }

    public void openLogin() {
        Intent intent = new Intent(this, LandingPage.class);
        startActivity(intent);
        finish();
    }

}