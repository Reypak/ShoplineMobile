package com.matt.shopline.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
    private int Image_Request_Code = 7;
    private Uri FilePathUri;
    private View viewOffers, offerIcon, viewSizes, sizesIcon;
    private ProgressDialog dialog;
    private FirebaseUser user;
    private MenuItem btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_upload);

        viewOffers = findViewById(R.id.viewOffers);
        viewSizes = findViewById(R.id.viewSizes);

        offerIcon = findViewById(R.id.offerIcon);
        sizesIcon = findViewById(R.id.sizesIcon);

        viewOffers.setVisibility(View.GONE);
        viewSizes.setVisibility(View.GONE);

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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();
//            Compression(FilePathUri);

            // puts file into image view
            Picasso.with(this).load(FilePathUri)
                    /*.fit()
                    .centerCrop()*/
                    .into(imageView);
        }
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
            uploadImage();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadData(String imageUrl) {
        // upload product
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postRef = db.collection("posts");

        // current user Following Ref
        final CollectionReference userCatalog = db.collection("users")
                .document(user.getUid())
                .collection("catalog");

        TextView etProduct = findViewById(R.id.etProduct);
        TextView etPrice = findViewById(R.id.etPrice);
        TextView etOffer = findViewById(R.id.etOffers);
        TextView etDesc = findViewById(R.id.etDescription);

        Map<String, Object> postData = new HashMap<>();
        postData.put("product", etProduct.getText().toString().trim());
        postData.put("price", etPrice.getText().toString().trim());
        postData.put("description", etDesc.getText().toString().trim());
        postData.put("timestamp", System.currentTimeMillis());
        postData.put("userID", user.getUid());

        String offers = etOffer.getText().toString().trim();
        if (viewOffers.getVisibility() == View.VISIBLE && !offers.isEmpty()) {
            postData.put("offers", offers);
        }

        if (imageUrl != null) {
            postData.put("imageUrl", imageUrl);
        }

        postRef.add(postData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                String postID = task.getResult().getId();

                Map<String, Object> data = new HashMap<>();
                // server timestamp
                data.put("timestamp", new Timestamp(new Date()));

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

            user = FirebaseAuth.getInstance().getCurrentUser();
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