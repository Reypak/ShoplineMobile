package com.matt.shopline.fragments.register;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.matt.shopline.R;

public class Step3 extends Fragment {
    OnDataPass dataPass;
    private EditText etPhone, etLocation;
    private String phone, location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_step3, container, false);

        etPhone = rootView.findViewById(R.id.etPhone);
        etLocation = rootView.findViewById(R.id.etLocation);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        phone = etPhone.getText().toString();
        location = etLocation.getText().toString().trim();
        String[] data = {phone, location};
        onDataPass(data);

//        if (!TextUtils.isEmpty(phone) || !TextUtils.isEmpty(location)) {
//            onDataPass(data);
//        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPass = (OnDataPass) context;
    }

    public void onDataPass(String[] data) {
        dataPass.DataStep3(data);
    }

}