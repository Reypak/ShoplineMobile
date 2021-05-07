package com.matt.shopline.fragments.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.matt.shopline.R;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class Search extends Fragment {
    FirebaseFirestore db;
    private EditText etSearch;
    private String searchValue;
    private List<User> searchList;
    private SearchListAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = rootView.findViewById(R.id.etSearch);
        etSearch.setHint("Search " + getString(R.string.app_name));
        View btnClear = rootView.findViewById(R.id.btnClear);
        View btnBack = rootView.findViewById(R.id.btnBack);
        progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        errorText = rootView.findViewById(R.id.errorText);
        errorText.setText("Results will appear here");
        errorText.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = rootView.findViewById(R.id.recView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        searchList = new ArrayList<>();

        adapter = new SearchListAdapter(searchList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = etSearch.getText().toString().trim();
                searchInDatabase(searchValue);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load discover
                Discover.loadFragment(requireActivity(), new Discover());
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.getText().clear();
                /*etSearch.clearFocus();
                // hide keyboard from view
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(etSearch.getWindowToken(), 0);*/
            }
        });

        return rootView;
    }

    private void searchInDatabase(String searchValue) {
        // search query
        Query reference = db.collection(getString(R.string.users)).whereArrayContains("search_keywords", searchValue.toLowerCase());
        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // map result to object
                    searchList = task.getResult().toObjects(User.class);
                    // update the adapter searchList
                    adapter.searchList = searchList;
                    adapter.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);

                    if (searchList.size() == 0) {
                        errorText.setText("No Results");
                        errorText.setVisibility(View.VISIBLE);
                    } else {
                        errorText.setVisibility(View.GONE);
                    }

                }
            }
        });
    }

    private static class SearchListAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        private List<User> searchList;

        private SearchListAdapter(List<User> searchList) {
            this.searchList = searchList;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
            return new SearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            User user = searchList.get(position);
            holder.setUserData(user.getUsername(), user.getOccupation(), user.getProfileUrl(), user.getUserID());
        }

        @Override
        public int getItemCount() {
            return searchList.size();
        }
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final View mView;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserData(String username, String occupation, String imageURL, final String userID) {
            final TextView textView = mView.findViewById(R.id.tvUsername);
            TextView textView2 = mView.findViewById(R.id.tvOccupation);
            final ImageView img = mView.findViewById(R.id.profile_image);

            textView.setText(username);
            textView2.setVisibility(View.GONE);
            if (occupation != null) {
                if (!occupation.isEmpty()) {
                    textView2.setText(occupation);
                    textView2.setVisibility(View.VISIBLE);
                }
            }

            Picasso.with(img.getContext())
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open profile
                    Intent intent = new Intent(img.getContext(), FeedUserProfile.class);
                    intent.putExtra("userID", userID);
                    img.getContext().startActivity(intent);
                }
            });
        }
    }

}