package com.matt.shopline.fragments.register;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.matt.shopline.R;
import com.matt.shopline.screens.Login;

public class Step2 extends Fragment {
    OnDataPass dataPass;
    private EditText etEmail, etPwrd;
    private String email, pwrd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_step2, container, false);

        etEmail = rootView.findViewById(R.id.etEmail);
        etPwrd = rootView.findViewById(R.id.etPwrd);

        final ImageButton btnToggle = rootView.findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login login = new Login();
                login.togglePassword(getActivity(), etPwrd, btnToggle);
            }
        });

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
                View errorText = rootView.findViewWithTag(getString(R.string.password));
                if (!pwrd.isEmpty() && pwrd.length() < 6) {
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    errorText.setVisibility(View.GONE);
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