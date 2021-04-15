package com.matt.shopline.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matt.shopline.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Upload extends AppCompatActivity {
    private ImageView imageView;
    private final int Image_Request_Code = 7;
    private Uri FilePathUri;
    private View viewOffers, offerIcon, viewSizes, sizesIcon;
    private ProgressDialog dialog;
    private FirebaseUser user;
    private MenuItem btnUpload;
    private TextView etDesc, etProduct, etPrice, etOffer, seekText;
    private Bundle extras;
    private Map<String, Object> postData;
    private CollectionReference postRef, userCatalog;
    private String[] strings;
    private String size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_upload);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        postRef = db.collection("posts");
        user = FirebaseAuth.getInstance().getCurrentUser();
        // current user Following Ref
        userCatalog = db.collection("users")
                .document(user.getUid())
                .collection("catalog");

        viewOffers = findViewById(R.id.viewOffers);
        viewSizes = findViewById(R.id.viewSizes);

        offerIcon = findViewById(R.id.offerIcon);
        sizesIcon = findViewById(R.id.sizesIcon);

        viewOffers.setVisibility(View.GONE);
        viewSizes.setVisibility(View.GONE);

        etProduct = findViewById(R.id.etProduct);
        etPrice = findViewById(R.id.etPrice);
        etOffer = findViewById(R.id.etOffers);
        etDesc = findViewById(R.id.etDescription);

        final SeekBar seekBar = findViewById(R.id.seekBar);
        final RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // change color to show disabled
                seekBar.getThumb().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                seekBar.getProgressDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                seekText.setTextColor(Color.LTGRAY);
                switch (i) {
                    case R.id.radio1:
                        size = "S";
                        break;
                    case R.id.radio2:
                        size = "M";
                        break;
                    case R.id.radio3:
                        size = "L";
                        break;
                    default:
                        // no item is checked
                        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                        seekText.setTextColor(getResources().getColor(R.color.colorPrimary));
                        break;
                }
            }
        });

        seekText = findViewById(R.id.progressText);
        seekText.setText(String.valueOf(seekBar.getProgress()));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radioGroup.clearCheck();
                seekText.setText(String.valueOf(i));
                size = String.valueOf(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);
            }
        });

        extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("data")) {
                getSupportActionBar().setTitle(getString(R.string.edit) + " Post");
                View imageLayout = findViewById(R.id.imageLayout);
                imageLayout.setVisibility(View.GONE);

                strings = extras.getStringArray("data");
                etProduct.setText(strings[1]);
                etPrice.setText(strings[2]);
                etDesc.setText(strings[3]);
                etOffer.setText(strings[4]);

                if (strings[4] != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Offers(null);
                        }
                    }, 500);
                }
                if (strings[5] != null) {
                    try {
                        // set progress
                        seekBar.setProgress(Integer.parseInt(strings[5]));
//                    size = strings[5];
                    } catch (Exception e) {
                        // get radio buttons
                        RadioButton b1, b2, b3;
                        b1 = findViewById(R.id.radio1);
                        b2 = findViewById(R.id.radio2);
                        b3 = findViewById(R.id.radio3);
                        // set Checked state
                        switch (strings[5]) {
                            case "S":
                                setChecked(b1);
                                break;
                            case "M":
                                setChecked(b2);
                                break;
                            case "L":
                                setChecked(b3);
                                break;
                        }
                    }
                    // show sizes view
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Sizes(null);
                        }
                    }, 500);
                }
            } else {
//              cast intent to image uri
                Uri data = extras.getParcelable(Intent.EXTRA_STREAM);
                putImage(data);
            }
        }
    }

    private void setChecked(RadioButton radioButton) {
        radioButton.setChecked(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {
            putImage(data.getData());
        }
    }

    private void putImage(Uri data) {
        FilePathUri = data;
//            Compression(FilePathUri);

        // puts file into image view
        Picasso.with(this).load(FilePathUri)
                /*.fit()
                .centerCrop()*/
                .into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.title_upload)
                .setIcon(R.drawable.ic_check)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        btnUpload = menu.getItem(0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == 0) {
            if (extras != null && extras.containsKey("data")) {
                updateData();
            } else {
                uploadImage();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateData() {
        collectData();
        postRef.document(strings[0]).update(postData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(Upload.this, "Post Updated", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    private void collectData() {
        postData = new HashMap<>();
        postData.put("product", etProduct.getText().toString().trim());
        postData.put("price", etPrice.getText().toString().trim());
        postData.put("description", etDesc.getText().toString().trim());
        // if view is visible
        String offers = etOffer.getText().toString().trim();
        if (viewOffers.getVisibility() == View.VISIBLE && !offers.isEmpty()) {
            postData.put("offers", offers);
        }
        if (viewSizes.getVisibility() == View.VISIBLE) {
            postData.put("size", size);
        }
    }

    private void uploadData(String imageUrl) {
        // upload product
        collectData();

        // add fields to the map
        postData.put("timestamp", System.currentTimeMillis());
        postData.put("userID", user.getUid());

        if (imageUrl != null) {
            postData.put("imageUrl", imageUrl);
        }

        postRef.add(postData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                String postID = task.getResult().getId();

                Map<String, Object> data = new HashMap<>();
                // server timestamp
                data.put(getString(R.string.timestamp), new Timestamp(new Date()));

                userCatalog.document(postID).set(data);

                Toast.makeText(Upload.this, "Post Uploaded", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });


    }

    private void uploadImage() {
        // check if image loaded
        if (FilePathUri != null) {
            // lock upload button
            btnUpload.setEnabled(false);

            dialog = ProgressDialog.show(this, "",
                    "Uploading your post...", true);
            dialog.setCancelable(true);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images").child(user.getUid());
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");

            // compression
            Bitmap bitmap = null;
            try {
                Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);
                bitmap = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * 0.5), (int) (b.getHeight() * 0.5), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
            byte[] compressedData = outputStream.toByteArray();
            bitmap.recycle(); // free up memory
//            FilePathUri = null; // clear FilePathUrl to refresh

            fileReference.putBytes(compressedData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            // start to send data to firestore
                            uploadData(imageUrl);
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "Please Select Image", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateViews(View viewParent, View viewIcon) {
        // toggle visibility of offers field
        if (viewParent.getVisibility() == View.GONE) {
            viewParent.setVisibility(View.VISIBLE);
        } else {
            viewParent.setVisibility(View.GONE);
        }
        // animate icon rotate to an X
        viewIcon.animate().rotationBy(135);
    }

    public void Sizes(View view) {
        animateViews(viewSizes, sizesIcon);
    }

    public void Offers(View view) {
        animateViews(viewOffers, offerIcon);
    }
}