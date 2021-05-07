package com.matt.shopline.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.matt.shopline.fragments.analytics.Account;

public class AnalyticsAdapter extends FragmentStatePagerAdapter {

    private final String date;
    int totalTabs;

    public AnalyticsAdapter(FragmentManager fm, int totalTabs, String date) {
        super(fm);
        this.totalTabs = totalTabs;
        this.date = date;
    }

    // this is for fragment tabs
    @NonNull
    @Override
    public Fragment getItem(int position) {
        Account account = new Account();
        Bundle bundle = new Bundle();
        bundle.putString("date", date);

        switch (position) {
            case 0:
                if (date != null) {
                    account.setArguments(bundle);
                }
                return account;
            case 1:
                // put sales
                bundle.putBoolean("sales", true);
                account.setArguments(bundle);
                return account;
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