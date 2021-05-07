package com.matt.shopline.fragments.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.matt.shopline.R;
import com.matt.shopline.screens.orders.Orders;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Home extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private long notificationCount;
    private TextView itemCount;
    private Toolbar toolbar;
    private ViewGroup rootView;
    private SharedPreferences sharedPreferences;
    private int day;

    public static List<String> generateSearchKeyword(String inputText) {
        // text to lowercase
        String inputString = inputText.toLowerCase();
        List<String> keywords = new ArrayList<>();
        // split the words in string
        String[] words = inputString.split(" ");
        // for each word
        for (String word : words) {
            StringBuilder appendString = new StringBuilder();
            // append string with each character
            for (int i = 0; i <= inputString.length() - 1; i++) {
                // add new character to appended string
                appendString.append(inputString.charAt(i));
                // store to list
                keywords.add(appendString.toString());
            }
            // remove first word
            inputString = inputString.replace(word + " ", "");
        }
        return keywords;
    }

  /*  private void addData(String inputText) {
        List<String> searchKeywords = generateSearchKeyword(inputText);
        Map<String, Object> map = new HashMap<>();
        map.put("title", inputText);
        map.put("search_keywords", searchKeywords);

        CollectionReference reference = db.collection("search");
        reference.add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                Toast.makeText(requireActivity(), "Done", Toast.LENGTH_SHORT).show();
            }
        });
    }
*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        toolbar = rootView.findViewById(R.id.toolbar1);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        // get shared prefs
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        sharedPreferences.edit().putString(getString(R.string.title_home), null).apply();
//        sharedPreferences.edit().putInt("day", 0).apply();

        final String location = sharedPreferences.getString(getString(R.string.title_home), null);
        if (location != null) {
            // load Feed Fragment
            loadFragment(new Feed());

            // get day value
            int first_day = sharedPreferences.getInt("day", 0);
            // check if same day
            if (first_day != day) {
                startShowcase();
            }

        } else {
            View fab = rootView.findViewById(R.id.fab);
            fab.setVisibility(View.GONE);
            loadFragment(new Suggestions());
        }

        /*// broadcast receiver to receive intent data from login activity
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish")) {
                    loadFragment(new Feed());
                }
            }
        };
        getActivity().registerReceiver(receiver, new IntentFilter("finish"));*/
//        addData("Sample Book Title");


        return rootView;
    }

    private void startShowcase() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    // save current day value
                    sharedPreferences.edit().putInt("day", day).apply();

                    @SuppressLint("ResourceType") final TapTargetSequence sequence = new TapTargetSequence(requireActivity())
                            .targets(
                                    TapTarget.forView(rootView.findViewById(R.id.fab),
                                            "Create Post", "Upload your new products here")
                                            .descriptionTextSize(15)
                                            .tintTarget(false)
                                    ,
                                    TapTarget.forToolbarMenuItem(toolbar, 1,
                                            "Show Orders", "Track your Shopline orders")
                                            .descriptionTextSize(15)
                                            .tintTarget(false)

                                   /* TapTarget.forBounds(rickTarget, "Down", ":^)")
                                            .cancelable(false)
                                            .icon(rick)*/
                            )
                            .listener(new TapTargetSequence.Listener() {
                                // This listener will tell us when interesting(tm) events happen in regards
                                // to the sequence
                                @Override
                                public void onSequenceFinish() {
                                    Intent intent = new Intent("show2");
                                    requireActivity().sendBroadcast(intent);
                                    // Yay
                                }

                                @Override
                                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                                    // Perform action for the current target
                                }

                                @Override
                                public void onSequenceCanceled(TapTarget lastTarget) {
//                                    onSequenceFinish();
                                    // Boo
                                }
                            });
                    sequence.start();
                    sequence.continueOnCancel(true);
                    sequence.considerOuterCircleCanceled(true);

                    /*TapTargetView.showFor(requireActivity(), TapTarget.forToolbarMenuItem(toolbar, 2,
//                            TapTarget.forView(rootView.findViewById(R.id.fab)TapTarget.forView(rootView.findViewById(R.id.fab)
                            "Create Post", "Upload your new products here")
                                    .outerCircleColor(R.color.colorPrimary)
                                    .outerCircleAlpha(0.96f)
                                    .targetCircleColor(Color.WHITE)
                                    .titleTextSize(20)
                                    .titleTextColor(Color.WHITE)
                                    .descriptionTextSize(15)
                                    .cancelable(true)
                                    .tintTarget(true)
                                    .targetRadius(60),
                            new TapTargetView.Listener() {
                                @Override
                                public void onTargetClick(TapTargetView view) {
                                    super.onTargetClick(view);      // This call is optional
//                                    Orders();
                                }
                            });*/
                }
            }
        }, 2000);
    }

    private void loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(Menu.NONE, 1, Menu.NONE, null)
                .setActionView(R.layout.basket_layout)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        FrameLayout frameLayout = (FrameLayout) menu.getItem(0).getActionView();
        itemCount = frameLayout.findViewById(R.id.badge);

        setupBadge();

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Orders();
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupBadge() {
        // number of notifications
        notificationCount = 0;
        itemCount.setVisibility(View.GONE);

        // ref to stored orders counter
        DocumentReference orders = db.collection(getString(R.string.users))
                .document(user.getUid())
                .collection("data")
                .document(getString(R.string.title_orders).toLowerCase());

        orders.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                // get value of stored long value
                if (error == null) {
                    if (value.exists()) {
                        notificationCount = value.getLong("orders");
                        if (notificationCount > 0) {
                            itemCount.setVisibility(View.VISIBLE);
                            itemCount.setText(String.valueOf(notificationCount));

                            ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1,
                                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            animation.setDuration(500);
                            itemCount.startAnimation(animation);

                        } else {
                            itemCount.setVisibility(View.GONE);
                        }

                    }
                }
            }
        });
    }

    public void Orders() {
        Intent intent = new Intent(getActivity(), Orders.class);
        startActivity(intent);
    }

}
