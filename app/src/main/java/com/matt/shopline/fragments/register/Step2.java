package com.matt.shopline.fragments.register;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.matt.shopline.R;

public class Step2 extends Fragment {
    OnDataPass dataPass;
    private EditText etEmail, etPwrd;
    private String email, pwrd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_step2, container, false);

        etEmail = rootView.findViewById(R.id.etEmail);
        etPwrd = rootView.findViewById(R.id.etPwrd);

        etPwrd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                pwrd = etPwrd.getText().toString();
                if (!pwrd.isEmpty() && pwrd.length() < 6) {
                    etPwrd.setError("Password too short!");
                }
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        email = etEmail.getText().toString();
        pwrd = etPwrd.getText().toString().trim();
        String[] data = {email, pwrd};
        onDataPass(data);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPass = (OnDataPass) context;
    }

    public void onDataPass(String[] data) {
        dataPass.DataStep2(data);
    }

}