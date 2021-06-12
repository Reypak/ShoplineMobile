package com.matt.shopline.fragments.profile;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matt.shopline.BuildConfig;
import com.matt.shopline.R;
import com.matt.shopline.adapters.TabAdapter;
import com.matt.shopline.screens.LandingPage;
import com.matt.shopline.screens.follow.FollowView;
import com.matt.shopline.screens.orders.RatingView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.matt.shopline.fragments.home.Home.generateSearchKeyword;

public class Profile extends Fragment {
    int Image_Request_Code = 7;
    private ImageView profileImage;
    private TextView tvUsername, tvLocation, tvBio, tvEmail, tvPhone, tvOccupation, tvWebsite, tvFollowers, tvCatalog, tvRef;
    private String phone, location, bio, email, username, occupation, website;
    private FirebaseUser user;
    private View downArrow;
    private View expandedView;
    private View viewFollow;
    private Button btnEdit, btnFollow;
    private EditText etUsername, etBio, etPhone, etLocation, etOccupation, etWebsite;
    private Uri FilePathUri;
    private DrawerLayout drawer;
    private FirebaseFirestore db;
    private String profileUrl;
    private String profileUri;
    private ViewGroup rootView;
    private Bundle bundle;
    private String userID;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button btnRef;

    public static void addSearchData(String inputText, String userID, FirebaseFirestore db, Context context) {
        // generate search keywords, store in List
        List<String> searchKeywords = generateSearchKeyword(inputText);

        // update user ref
        DocumentReference reference = db.collection(context.getString(R.string.users)).document(userID);
        reference.update("search_keywords", searchKeywords);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.activity_drawer, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar1);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_profile);

        setHasOptionsMenu(true);

        drawer = rootView.findViewById(R.id.drawer_layout);
        NavigationView navView = rootView.findViewById(R.id.nav_view);

        tabLayout = rootView.findViewById(R.id.tabLayout);
        viewPager = rootView.findViewById(R.id.viewPager);

        profileImage = rootView.findViewById(R.id.profile_image);
        tvUsername = rootView.findViewById(R.id.Username);
        tvLocation = rootView.findViewById(R.id.tvLocation);
        tvBio = rootView.findViewById(R.id.tvBio);
        tvEmail = rootView.findViewById(R.id.tvEmail);
        tvPhone = rootView.findViewById(R.id.tvPhone);
        tvWebsite = rootView.findViewById(R.id.tvWebsite);
        tvFollowers = rootView.findViewById(R.id.tvFollowers);
        tvCatalog = rootView.findViewById(R.id.tvCatalog);
        tvRef = rootView.findViewById(R.id.tvReferrals);
        tvOccupation = rootView.findViewById(R.id.tvOccupation);
        tvOccupation.setVisibility(View.GONE);
        btnEdit = rootView.findViewById(R.id.btnEdit);
        btnFollow = rootView.findViewById(R.id.btnFollow);
        btnRef = rootView.findViewById(R.id.btnRef);
        viewFollow = rootView.findViewById(R.id.viewFollow);
        downArrow = rootView.findViewById(R.id.downArrow);
        View viewFollowers = rootView.findViewById(R.id.btnFollowers);
        expandedView = rootView.findViewById(R.id.expandedView);
        expandedView.setVisibility(View.GONE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        String versionName = BuildConfig.VERSION_NAME;
        TextView appVersion = drawer.findViewWithTag(getString(R.string.app_name));
        appVersion.setText(String.format("Version %s", versionName));

        getUserData();
        getProfileImage();

        if (bundle != null) {
            // for other user
            getTabs(getString(R.string.catalog), getString(R.string.offers), userID, false);
        } else {
            // for currentUser
            getTabs("Posts", "Wishlist", user.getUid(), true);
        }

        downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expandedView.getVisibility() != View.VISIBLE) {
                    expandedView.setVisibility(View.VISIBLE);
                } else {
                    expandedView.setVisibility(View.GONE);
                }
                downArrow.animate().rotationBy(180);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.logout) {
                    //sign out
                    FirebaseAuth.getInstance().signOut();
                    // unsubscribe to notify
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(user.getUid() + "_notifications");

                    Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LandingPage.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else if (menuItem.getItemId() == R.id.invite) {
                    shareIntent();
                } else if (menuItem.getItemId() == R.id.feedback) {
                    composeEmail();
                }
                drawer.closeDrawer(GravityCompat.END);
                return false;
            }
        });

        viewFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FollowView.class);
                if (userID != null) {
                    // selected user
                    intent.putExtra("userID", userID);
                } else {
                    // view current user
                    intent.putExtra("userID", user.getUid());
                }
                startActivity(intent);
            }
        });

        btnRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReferrals();
            }
        });

        return rootView;
    }

    private void showReferrals() {
        BottomSheetDialog dialog = new BottomSheetDialog(rootView.getContext(), R.style.BottomSheetDialog);
        dialog.setContentView(R.layout.referral_sheet);
        dialog.show();
    }

    private void listenRating() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("rating")) {
                    View ratingView = rootView.findViewById(R.id.ratingView);
                    TextView textView3 = rootView.findViewById(R.id.tvRating);
                    View iconRating = rootView.findViewById(R.id.iconRating);

                    ratingView.setVisibility(View.VISIBLE);
                    int rating = intent.getIntExtra("rating", 0);
                    new RatingView().setView(rating, textView3, iconRating);
                }
            }
        };
        rootView.getContext().registerReceiver(receiver, new IntentFilter("rating"));
    }

    private void getUserData() {
        DocumentReference userRef = null;

        bundle = getArguments();
        if (bundle != null) {
            // other User
            userID = bundle.getString("userID");
            userRef = db.collection("users").document(userID);

            getFollowers(userID);
            getCatalog(userID, "catalog", tvCatalog);
            getCatalog(userID, "referrals", tvRef);

        } else {
            // Current User
            // get username from AuthInstance
            if (user != null) {
                username = user.getDisplayName();
                tvUsername.setText(username);

                userRef = db.collection("users").document(user.getUid());

                getFollowers(user.getUid());
                getCatalog(user.getUid(), "catalog", tvCatalog);
                getCatalog(user.getUid(), "referrals", tvRef);
            }
        }

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    // in case username is empty
                    if (tvUsername.getText().toString().isEmpty()) {
                        username = task.getResult().get("username").toString();
                        tvUsername.setText(username);
                        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(username);
                    }

                    location = task.getResult().get("location").toString();
                    bio = task.getResult().get("bio").toString();
                    email = task.getResult().get("email").toString();
                    phone = task.getResult().get("phone").toString();

                    if (task.getResult().get("profileUrl") != null) {
                        profileUri = task.getResult().get("profileUrl").toString();
                    }

                    getProfileImage();

                    if (task.getResult().get("occupation") != null && !task.getResult().get("occupation").toString().isEmpty()) {
                        occupation = task.getResult().get("occupation").toString();
                        tvOccupation.setText(occupation);
                        tvOccupation.setVisibility(View.VISIBLE);
                    }

                    if (task.getResult().get("website") != null && !task.getResult().get("website").toString().isEmpty()) {
                        website = task.getResult().get("website").toString();
                        tvWebsite.setText(website);
//                    tvOccupation.setVisibility(View.VISIBLE);
                    }

                    tvLocation.setText(location);
                    tvBio.setText(bio);
                    tvEmail.setText(email);
                    tvPhone.setText(phone);

                    listenRating(); // listen for rating value

                    // edit button only load dialog after getting
                    btnEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditProfile();
                        }
                    });
                }
            }
        });
    }

    private void getCatalog(String userID, final String path, final TextView tv) {
        // user catalog count number of docs
        DocumentReference userCatalog = db.collection("users").document(userID)
                .collection("data").document(path);
        userCatalog.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String catalog = documentSnapshot.get(path).toString();
                    tv.setText(catalog);
                }
            }
        });
    }

    private void getTabs(String Tab1, String Tab3, String userID, boolean b) {
        tabLayout.addTab(tabLayout.newTab().setText(Tab1));
        tabLayout.addTab(tabLayout.newTab().setText("Reviews"));
        tabLayout.addTab(tabLayout.newTab().setText(Tab3));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // pass userID to constructor for Catalog tab
        TabAdapter adapter = new TabAdapter(getActivity(), getFragmentManager(), tabLayout.getTabCount(), userID, b);
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

    private void getFollowers(final String userID) {
        /**
         * run this method first to allow following
         * if current user is already following then =>
         * second click method ensures delete of document
         */
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followUser(userID);
                getFollowers(userID);
            }
        });

        // reference to selected user's follower count data
        DocumentReference followersCount = db.collection("users")
                .document(userID)
                .collection("data")
                .document(getString(R.string.followers).toLowerCase());
        followersCount.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (value.exists()) {
                        String followers = value.get("followers").toString();
                        tvFollowers.setText(followers);
                    }
                }
            }
        });

        // check user Followers Ref for current userID
        DocumentReference userFollowers = db.collection(getString(R.string.users))
                .document(userID)
                .collection(getString(R.string.followers).toLowerCase())
                .document(user.getUid());
        userFollowers.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // following state
                    btnFollow.setText(R.string.following);
                    btnFollow.setBackgroundColor(Color.parseColor("#265458F7"));
                    btnFollow.setTextColor(Color.parseColor("#5458F7"));

                    btnFollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Deleting the document from Followers collection
                            documentSnapshot.getReference().delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // original state
                                            btnFollow.setText(getString(R.string.follow));
                                            btnFollow.setBackgroundColor(Color.parseColor("#5458F7"));
                                            btnFollow.setTextColor(Color.WHITE);

                                            getFollowers(userID); // refresh followers
                                        }
                                    });

                            CollectionReference userFollowing = db.collection(getString(R.string.users))
                                    .document(user.getUid())
                                    .collection("following");

                            //  Deleting the document from current users Following collection
                            userFollowing.document(userID).delete();

                            // unsubscribe user
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(userID);
                        }
                    });
                }
            }
        });

       /* // check user Followers Ref
        final CollectionReference userFollowers = db.collection("users").document(userID)
                .collection("followers");
        userFollowers.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                // number of documents
                int followers = task.getResult().size();
                tvFollowers.setText(String.valueOf(followers));

                // check to see if currentUser is following viewed User
                for (final DocumentSnapshot documentSnapshot : task.getResult()) {
                    // getting users ID's
                    final String data = documentSnapshot.getId();
                    // if currentUserID exists
                    if (data.equals(user.getUid())) {

                        // following state
                        btnFollow.setText("Following");
                        btnFollow.setBackgroundColor(Color.parseColor("#265458F7"));
                        btnFollow.setTextColor(Color.parseColor("#5458F7"));

                        btnFollow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Deleting the document from Followers collection
                                userFollowers.document(data).delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // original state
                                                btnFollow.setText(getString(R.string.follow));
                                                btnFollow.setBackgroundColor(Color.parseColor("#5458F7"));
                                                btnFollow.setTextColor(Color.WHITE);

                                                getFollowers(userID); // refresh followers
                                            }
                                        });

                                CollectionReference userFollowing = db.collection("users")
                                        .document(user.getUid())
                                        .collection("following");

                                //  Deleting the document from current users Following collection
                                userFollowing.document(userID).delete();

                            }
                        });
                    }
                }
            }
        });*/
    }

    private void followUser(final String userID) {
        // followed User ref
        CollectionReference userFollowers = db.collection("users")
                .document(userID)
                .collection("followers");

        // current user Following Ref
        CollectionReference userFollowing = db.collection("users")
                .document(user.getUid())
                .collection("following");

        Map<String, Object> userdata = new HashMap<>();
        userdata.put("exists", true);

        // send currentUserID to followed user collection
        userFollowers.document(user.getUid()).set(userdata);

        // send followed userID to current user Following collection
        userFollowing.document(userID).set(userdata);

        // subscribe to user
        FirebaseMessaging.getInstance().subscribeToTopic(userID);
    }

    private void getProfileImage() {
        // get profile from default user
        if (bundle == null) {
            if (user.getPhotoUrl() != null) {
                profileUri = user.getPhotoUrl().toString();
            }
            // load edit profile
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditProfile();
                }
            });
        }
        Picasso.with(getActivity()).load(profileUri)
                .fit()
                .placeholder(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(profileImage);
    }

    private void EditProfile() {
        final Dialog d = new Dialog(getActivity(), R.style.AppTheme);
        d.setContentView(R.layout.edit_profile);
//        d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        View btnSave, btnClose;
        etUsername = d.findViewById(R.id.etUsername);
        etBio = d.findViewById(R.id.etBio);
        etLocation = d.findViewById(R.id.etLocation);
        etPhone = d.findViewById(R.id.etPhone);
        etOccupation = d.findViewById(R.id.etOccupation);
        etWebsite = d.findViewById(R.id.etWebsite);
        btnSave = d.findViewById(R.id.btnSave);
        btnClose = d.findViewById(R.id.btnClose);
        profileImage = d.findViewById(R.id.profile_image);

        // getting profile image in dialog
        getProfileImage();

        etUsername.setText(username);
        etBio.setText(bio);
        etLocation.setText(location);
        etPhone.setText(phone);
        etOccupation.setText(occupation);
        etWebsite.setText(website);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                        "Updating your profile...", true);
                dialog.setCancelable(true);

                if (FilePathUri != null) {
                    setProfileImage();
                }
                username = etUsername.getText().toString().trim();

                // Create a new user data
                Map<String, Object> userdata = new HashMap<>();
                userdata.put("occupation", etOccupation.getText().toString().trim());
                userdata.put("website", etWebsite.getText().toString().trim());
                userdata.put("username", username);
                userdata.put("phone", etPhone.getText().toString().trim());
                userdata.put("location", etLocation.getText().toString().trim());
                userdata.put("bio", etBio.getText().toString().trim());

                if (username != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            tvUsername.setText(username);
                        }
                    });
                }

                // create keywords
                addSearchData(username, user.getUid(), db, requireContext());

                // Add a new document with user ID
                db.collection(getString(R.string.users)).document(user.getUid())
                        .update(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // save to SharedPrefs
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        sharedPreferences.edit()
                                .putString(getString(R.string.location).toLowerCase(), etLocation.getText().toString().trim())
                                .apply();

                        d.dismiss();
                        dialog.dismiss();
                        Toast.makeText(getActivity(), getString(R.string.title_profile) + " Updated", Toast.LENGTH_SHORT).show();
                        getUserData(); // refresh data
                    }
                });
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);
            }
        });

        d.show();
    }

    private void setProfileImage() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images").child(user.getUid());
        StorageReference fileReference = storageReference.child("profile.jpg");

        // compression
        Bitmap bitmap = null;
        try {
            Bitmap b = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), FilePathUri);
            bitmap = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * 0.5), (int) (b.getHeight() * 0.5), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
        byte[] compressedData = outputStream.toByteArray();
        bitmap.recycle(); // free up memory
        FilePathUri = null; // clear FilePathUrl to refresh

        fileReference.putBytes(compressedData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // this method captures the download link for the uploaded image
                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profileUrl = uri.toString();

                        // sending the uploaded image url to database
                        db.collection("users").document(user.getUid())
                                .update("profileUrl", profileUrl);

                        // update user profile data
                        UserProfileChangeRequest profileUpdates = null;
                        if (profileUrl != null) {
                            profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(profileUrl))
                                    .build();
                        }
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Photo Changed", Toast.LENGTH_SHORT).show();

                                profileImage = rootView.findViewById(R.id.profile_image);
                                getProfileImage();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // get userID Stored data from Activity
        bundle = getArguments();
        if (bundle != null) {
            userID = bundle.getString("userID");
            if (!userID.equals(user.getUid())) {
                btnEdit.setVisibility(View.GONE);
                viewFollow.setVisibility(View.VISIBLE);
                // lock the drawer
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
            // back button
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();
//            Compression(FilePathUri);

            // puts file into image view
            Picasso.with(getActivity()).load(FilePathUri)
                    .fit()
                    .centerCrop()
                    .into(profileImage);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, null)
                .setIcon(R.drawable.ic_alt_icon)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        if (bundle != null) {
            if (!userID.equals(user.getUid())) {
                menu.removeItem(0);
                menu.add(Menu.NONE, 1, Menu.NONE, R.string.share);
//                menu.add(Menu.NONE, 3, Menu.NONE, "Turn on " + getString(R.string.title_notifications));
                menu.add(Menu.NONE, 2, Menu.NONE, "Report");
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            // closes drawer if its open
            if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
            } else {
                drawer.openDrawer(GravityCompat.END);
            }
        } else if (item.getItemId() == android.R.id.home) {
            requireActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Tell your friends about " + getString(R.string.app_name));
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + getString(R.string.app_name) + " and access all the best deals, offers and promos. " +
                "Available on Google Play https://play.google.com/store/apps/details?id=com.matt.shopline");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    public void composeEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + getString(R.string.app_email)));
        startActivity(emailIntent);
    }
}