package com.matt.shopline.fragments.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.matt.shopline.R;
import com.matt.shopline.adapters.AnalyticsAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Analytics extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Spinner spinner;
    private String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_analytics, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar1);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_analytics);

        tabLayout = rootView.findViewById(R.id.tabLayout);
        viewPager = rootView.findViewById(R.id.viewPager);
        spinner = rootView.findViewById(R.id.spinner);

        addSpinnerDates();

        return rootView;
    }

    private void addSpinnerDates() {
        final ArrayList<String> dates = new ArrayList<>();
        dates.add("Today");

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_item, dates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                date = null;
                if (!adapterView.getSelectedItem().equals("Today")) {
                    date = adapterView.getSelectedItem().toString();
                }
                viewPager.removeAllViews();
                tabLayout.removeAllTabs();
                getTabs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // listen for broadcast
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("finish")) {
                    ArrayList<String> newDates = intent.getStringArrayListExtra("dates");
                    // check if value exists
                    DateFormat format = new SimpleDateFormat("d-M-yyyy");
                    String currentDate = format.format(new Date());

//                    String n1 = DateFormat.getDateInstance(DateFormat.DEFAULT).format(new Date());
                    if (newDates != null) {
                        for (String s : newDates) {
                            // create short date format
                            DateFormat format2 = DateFormat.getDateInstance(DateFormat.DEFAULT);

                            Date date2 = null;
                            try {
                                // convert to date
                                date2 = format.parse(s);
                            } catch (ParseException ignored) {

                            }
                            // date to string using short format
                            String n1 = format2.format(date2);

                            // remove duplicates and current Date
                            if (!dates.contains(n1) && !s.contains(currentDate)) {
                                dates.add(n1);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };

        requireActivity().registerReceiver(receiver, new IntentFilter("finish"));
    }

    private void getTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Account"));
        tabLayout.addTab(tabLayout.newTab().setText("Sales"));

        AnalyticsAdapter adapter = new AnalyticsAdapter(getFragmentManager(), tabLayout.getTabCount(), date);
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

/*    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(requireActivity(), view);
        String[] strings = {"Hello", "Yesto"};
        popupMenu.getMenu().add(R.string.share);
        popupMenu.getMenu().add("Add to Features");
        popupMenu.getMenu().add(R.string.edit);
        popupMenu.getMenu().add(R.string.delete);
        popupMenu.getMenu().add("Advertise Post");

        popupMenu.show();
    }*/
}