package com.matt.shopline.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.matt.shopline.R;
import com.matt.shopline.fragments.profile.Profile;

public class FeedUserProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_user_profile);

        loadFragment(new Profile());
    }

    private void loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            Intent intent = getIntent();
            String userID = intent.getStringExtra("userID");

            if (userID != null) {
                Bundle bundle = new Bundle();
                bundle.putString("userID", userID);
                fragment.setArguments(bundle);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}