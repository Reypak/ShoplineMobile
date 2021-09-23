package com.matt.shopline.fragments.register;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.matt.shopline.R;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class Step1 extends Fragment {
    OnDataPass dataPass;
    int Image_Request_Code = 7;
    Uri FilePathUri;
    private EditText etUsername;
    private EditText etBio;
    private ImageView profileImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_step1, container, false);

        etUsername = rootView.findViewById(R.id.etUsername);
        etBio = rootView.findViewById(R.id.etBio);
        profileImage = rootView.findViewById(R.id.profile_image);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);
            }
        });
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();
//            Compression(FilePathUri);

            // puts file into image view
            Picasso.get().load(FilePathUri)
                    .fit()
                    .centerCrop()
                    .into(profileImage);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String[] data = {username, bio};
        onDataPass(data, FilePathUri);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPass = (OnDataPass) context;
    }

    public void onDataPass(String[] data, Uri uri) {
        dataPass.DataStep1(data, uri);
    }

}