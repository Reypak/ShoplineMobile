package com.matt.shopline.fragments.register;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.matt.shopline.R;

public class Step4 extends Fragment {
    OnDataPass dataPass;
    private Spinner spinner;
    private EditText etBio;
    private String category, bio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_step4, container, false);

        spinner = rootView.findViewById(R.id.spinner);
        etBio = rootView.findViewById(R.id.etBio);
        category = "None";
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    category = spinner.getSelectedItem().toString();
                    String[] data = {category, ""};
                    onDataPass(data);
                } else {
                    String[] data = {category, ""};
                    onDataPass(data);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        etBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                bio = etBio.getText().toString().trim();
                String[] data = {category, bio};
                onDataPass(data);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPass = (OnDataPass) context;
    }

    public void onDataPass(String[] data) {
        dataPass.DataStep4(data);
    }
}