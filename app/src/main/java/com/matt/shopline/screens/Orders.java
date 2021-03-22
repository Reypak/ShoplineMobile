package com.matt.shopline.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.matt.shopline.R;
import com.matt.shopline.adapters.OrdersTabAdapter;


public class Orders extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private OrdersTabAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_orders);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        getTabs();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            // select second tab after 1.5s
            tabLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabLayout.getTabAt(1).select();
                }
            }, 1500);
        }

    }

    private void getTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.customers));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.you));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        adapter = new OrdersTabAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}