package com.matt.shopline.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.matt.shopline.R;
import com.matt.shopline.fragments.Home;
import com.matt.shopline.fragments.Notifications;
import com.matt.shopline.fragments.Search;
import com.matt.shopline.fragments.profile.Catalog;
import com.matt.shopline.fragments.profile.UserProfile;

public class NavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static FirebaseUser user;
    private BottomNavigationView navigation;

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
                }
            }
        }, 100);


        //getting bottom navigation view and attaching the listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

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
                fragment = new Search();
                break;
            case R.id.navigation_analytics:
                fragment = new Catalog();
                break;
            case R.id.navigation_notifications:
                fragment = new Notifications();
                break;

            case R.id.navigation_profile:
                fragment = new UserProfile();
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

    public void signOut(View view) {
        //sign out
        FirebaseAuth.getInstance().signOut();
        openLogin();
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