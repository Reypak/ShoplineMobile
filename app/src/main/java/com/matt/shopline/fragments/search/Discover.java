package com.matt.shopline.fragments.search;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.matt.shopline.R;

public class Search extends Fragment {
    FirebaseFirestore db;
    private EditText etSearch;
    private View btnSearch;
    private String searchValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = rootView.findViewById(R.id.etSearch);
        btnSearch = rootView.findViewById(R.id.btnSearch);

        db = FirebaseFirestore.getInstance();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.clearFocus();
                // hide keyboard from view
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

                searchValue = etSearch.getText().toString().trim();
                CollectionReference userRef = db.collection("users");
                Query query = userRef.whereEqualTo("username", searchValue);
                query.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                        String data = queryDocumentSnapshot.get("username").toString();
                                        Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        });
            }
        });


        return rootView;
    }

}