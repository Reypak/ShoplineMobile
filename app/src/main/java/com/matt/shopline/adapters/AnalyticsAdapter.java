package com.matt.shopline.adapters;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.matt.shopline.fragments.analytics.Account;

public class AnalyticsAdapter extends FragmentStatePagerAdapter {

    int totalTabs;
    private final String date;

    public AnalyticsAdapter(FragmentManager fm, int totalTabs, String date) {
        super(fm);
        this.totalTabs = totalTabs;
        this.date = date;
    }

    // this is for fragment tabs
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Account account = new Account();
                if (date != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("date", date);
                    account.setArguments(bundle);
                }
                return account;
            case 1:
            case 2:
                return new Fragment();
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