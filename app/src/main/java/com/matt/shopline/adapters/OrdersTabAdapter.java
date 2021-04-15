package com.matt.shopline.adapters;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.matt.shopline.R;
import com.matt.shopline.screens.orders.OrdersList;

public class OrdersTabAdapter extends FragmentStatePagerAdapter {

    int totalTabs;
    private Context myContext;

    public OrdersTabAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new OrdersList();
            case 1:
                OrdersList ordersListYou = new OrdersList();
                // pass data to parent fragment to know that state is for Users Orders
                Bundle bundle = new Bundle();
                bundle.putString(myContext.getString(R.string.you), "1");
                ordersListYou.setArguments(bundle);

                return ordersListYou;
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