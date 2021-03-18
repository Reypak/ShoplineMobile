package com.matt.shopline.adapters;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.matt.shopline.R;
import com.matt.shopline.screens.follow.FollowList;

public class FollowTabAdapter extends FragmentStatePagerAdapter {

    int totalTabs;
    private Context myContext;
    private String userID;

    public FollowTabAdapter(Context context, FragmentManager fm, int totalTabs, String userID) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
        this.userID = userID;
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:

                FollowList followers = new FollowList();
                // pass data
                bundle.putString("userID", userID);
                followers.setArguments(bundle);
                return followers;

            case 1:

                FollowList following = new FollowList();
                // pass data
                bundle.putString(myContext.getString(R.string.you), "1");
                bundle.putString("userID", userID);
                following.setArguments(bundle);
                return following;

            default:
                return null;
        }
    }

    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }
}