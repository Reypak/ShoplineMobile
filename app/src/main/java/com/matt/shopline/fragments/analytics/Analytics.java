package com.matt.shopline.fragments.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.matt.shopline.R;
import com.matt.shopline.adapters.AnalyticsAdapter;

import java.util.ArrayList;

public class Analytics extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_analytics, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar1);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_analytics);

        tabLayout = rootView.findViewById(R.id.tabLayout);
        viewPager = rootView.findViewById(R.id.viewPager);
        Spinner spinner = rootView.findViewById(R.id.spinner);
        ArrayList<String> dates = new ArrayList<>();
        dates.add("Today");
        dates.add("Yesterday");
        dates.add("How");
        ArrayAdapter adapter = new ArrayAdapter(requireActivity(), R.layout.spinner_item, dates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Button btnDate = rootView.findViewById(R.id.btnDate);
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
        getTabs();
        return rootView;
    }

    private void getTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Account"));
        tabLayout.addTab(tabLayout.newTab().setText("Sales"));
        tabLayout.addTab(tabLayout.newTab().setText("Market Stats"));

        AnalyticsAdapter adapter = new AnalyticsAdapter(requireActivity(), getFragmentManager(), tabLayout.getTabCount());
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

    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(requireActivity(), view);
        String[] strings = {"Hello", "Yesto"};
        popupMenu.getMenu().add(R.string.share);
        popupMenu.getMenu().add("Add to Features");
        popupMenu.getMenu().add(R.string.edit);
        popupMenu.getMenu().add(R.string.delete);
        popupMenu.getMenu().add("Advertise Post");

      /*  popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle() == context.getString(R.string.delete)) {
                    Snackbar snackbar = Snackbar
                            .make(rootView.getRootView().findViewById(android.R.id.content),
                                    "Confirm " + context.getString(R.string.delete),
                                    Snackbar.LENGTH_LONG)
                            .setAction(R.string.yes, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    deletePost();
                                }
                            });

                    snackbar.show();
                } else if (menuItem.getTitle() == context.getString(R.string.add_wishlist)) {
                    addWishList();
                } else if (menuItem.getTitle() == context.getString(R.string.edit)) {
                    // string array with data
                    String[] strings = {postID, product, price, description, offers};
                    Intent intent = new Intent(context, Upload.class);
                    intent.putExtra("data", strings);
                    context.startActivity(intent);
                }
                return false;
            }
        });*/
        popupMenu.show();
    }
/*    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, null)
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            // open Search
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.fragment_container, new Search())
                    .commit();
        }
        return super.onOptionsItemSelected(item);
    }*/
}