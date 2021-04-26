package com.matt.shopline.adapters;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.matt.shopline.fragments.profile.Catalog;
import com.matt.shopline.fragments.profile.Reviews;


/**
 * loads tab layout fragment when i click on fragtab option,
 * but when i try to load it second time by clicking on same options,
 * it is not showing tab content.
 * <p>
 * Try using
 * class ViewPagerAdapter extends FragmentStatePagerAdapter {
 * <p>
 * instead of
 * class ViewPagerAdapter extends FragmentPagerAdapter {
 **/
public class TabAdapter extends FragmentStatePagerAdapter {

    private final boolean b;
    int totalTabs;
    private Context myContext;
    private final String userID; // added to constructor to get data from parent Activity

    public TabAdapter(Context context, FragmentManager fm, int totalTabs, String userID, boolean b) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
        this.userID = userID;
        this.b = b;
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("userID", userID);

        switch (position) {
            case 0:
                Catalog catalog = new Catalog();
                // pass userID String from parent activity(UserProfile)
                // to the Catalog fragment through tabAdapter
                catalog.setArguments(bundle);
                return catalog;
            case 1:
                // open reviews
                Reviews reviews = new Reviews();
                reviews.setArguments(bundle);
                return reviews;
            case 2:
                // if boolean is true
                if (b) {
                    Catalog wishList;
                    wishList = new Catalog();
                    return wishList;
                } else {
                    return new Fragment();
                }
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