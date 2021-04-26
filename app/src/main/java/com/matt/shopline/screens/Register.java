package com.matt.shopline.screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matt.shopline.R;
import com.matt.shopline.fragments.register.OnDataPass;
import com.matt.shopline.fragments.register.Step1;
import com.matt.shopline.fragments.register.Step2;
import com.matt.shopline.fragments.register.Step3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class Register extends FragmentActivity implements OnDataPass {
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private TextView stepNumber;
    private Button btnNext;
    private String email;
    private String password;
    private String username;
    private Uri profileUri;
    private String phone;
    private String location;
    //    private String category;
    private String bio;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String profileUrl;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_stepper);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.pager);
        // BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT pauses the fragment as soon as it is changed
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mPager.setAdapter(pagerAdapter);
        mPager.setOffscreenPageLimit(2); // keeps the fragment alive up to 3 pages away
        btnNext = findViewById(R.id.btnNext);
        stepNumber = findViewById(R.id.stepNumber);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnNext.getText().equals(getString(R.string.sign_up))) {

                    register();
//                    Toast.makeText(getApplicationContext(), username + bio + email + password + location + phone, Toast.LENGTH_LONG).show();

                } else {
                    // go to next page
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
            }
        });

        stepNumber(0);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // setting the step number
                stepNumber(position);
                if (position == 2) {
                    btnNext.setText(getString(R.string.sign_up));

                } else {
                    btnNext.setText("Next");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void register() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // check if email and password is empty
        if (!email.isEmpty() && !password.isEmpty()) {

            // check is username is null
            if (!username.isEmpty()) {

                final ProgressDialog dialog = ProgressDialog.show(Register.this, "",
                        "Signing up...", true);

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            db = FirebaseFirestore.getInstance();

                            // set the profile image if exists
                            if (profileUri != null) {
                                setProfileImage();
                            } else {
                                // call update method to add Username and profile image
                                updateUser(Register.this, user, username, profileUrl);
                            }

                            // Create a new user data
                            Map<String, Object> userdata = new HashMap<>();
                            userdata.put("username", username);
                            userdata.put("email", user.getEmail());
                            userdata.put("userID", user.getUid());
                            userdata.put("phone", phone);
                            userdata.put("location", location);
//                            userdata.put("category", category);
                            userdata.put("bio", bio);

                            // Add a new document with user ID
                            db.collection("users").document(user.getUid())
                                    .set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();

                                    // send broadcast to Landing page
                                    Intent intent = new Intent("finish");
                                    sendBroadcast(intent);

                                    openMain(); // go to main
                                    setToken(user.getUid());
                                    // subscribe to notify
                                    FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid() + "_notifications");
                                }
                            });

                            // save to SharedPrefs
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit()
                                    .putString(getString(R.string.location).toLowerCase(), location)
                                    .apply();
                        } else {
                            dialog.dismiss();
                            String msg = task.getException().getMessage();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Register.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                mPager.setCurrentItem(0, true);
                Toast.makeText(Register.this, "Name is required.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // go step behind
            stepBack(null);
//            mPager.setCurrentItem(1, true);
            Toast.makeText(Register.this, "Fill in details.", Toast.LENGTH_SHORT).show();
        }

    }

    public void setToken(String userID) {
        String device_token = FirebaseInstanceId.getInstance().getToken();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users")
                .document(userID);
        userRef.update("token", device_token);
    }

    private void setProfileImage() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images").child(user.getUid());
        StorageReference fileReference = storageReference.child("profile.jpg");

        // compression
        Bitmap bitmap = null;
        try {
            Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(), profileUri);
            bitmap = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * 0.5), (int) (b.getHeight() * 0.5), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
        byte[] compressedData = outputStream.toByteArray();
        bitmap.recycle();

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
                        updateUser(Register.this, user, username, profileUrl);
                    }
                });
            }
        });
    }

    private void updateUser(final Context ctx, FirebaseUser user, final String username, final String profileUrl) {
        // update user profile
        UserProfileChangeRequest profileUpdates;
        if (profileUrl != null) {
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .setPhotoUri(Uri.parse(profileUrl))
                    .build();
        } else {
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();
        }

        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ctx, "Logged in as " + username, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private void stepNumber(int item) {
        stepNumber.setText(MessageFormat.format("{0} of {1}", item + 1, NUM_PAGES));
    }

    public void stepBack(View view) {
        onBackPressed();
    }

    @Override
    public void DataStep1(String[] data, Uri uri) {
        username = data[0];
        bio = data[1];
        profileUri = uri;
    }

    @Override
    public void DataStep2(String[] data) {
        email = data[0];
        password = data[1];
    }

    @Override
    public void DataStep3(String[] data) {
        phone = data[0];
        location = data[1];
    }

    private void openMain() {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
        finish();
    }

    private static class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm, int behaviorResumeOnlyCurrentFragment) {
            super(fm, behaviorResumeOnlyCurrentFragment);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new Step1();
                case 1:
                    return new Step2();
                case 2:
                    return new Step3();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}