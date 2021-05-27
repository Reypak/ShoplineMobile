package com.matt.shopline.fragments.search;

import android.content.Context;
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
import androidx.paging.PagedList;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.PostView;
import com.matt.shopline.screens.follow.FollowList;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class Search extends Fragment {
    FirebaseFirestore db;
    private EditText etSearch;
    private String searchValue;
    /* private List<User> searchList;
    private SearchListAdapter adapter; */
    private ProgressBar progressBar;
    private TextView errorText;
    private RecyclerView recyclerView;
    private ViewGroup rootView;
    private FirestorePagingAdapter<User, FollowList.BlogViewHolder> adapter1;
    private FirestorePagingAdapter<User, PostViewHolder> adapter2;

    public static void openActivity(Class<?> AClass, Context context, String key, String id) {
        Intent intent = new Intent(context, AClass);
        intent.putExtra(key, id);
        context.startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = rootView.findViewById(R.id.etSearch);
        etSearch.setHint("Search shops, items and services");
        final View btnClear = rootView.findViewById(R.id.btnClear);
        View btnBack = rootView.findViewById(R.id.btnBack);
        progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        btnClear.setVisibility(View.GONE);
        errorText = rootView.findViewById(R.id.errorText);
        errorText.setText("Results will appear here");
        errorText.setVisibility(View.VISIBLE);

        recyclerView = rootView.findViewById(R.id.recView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setHasFixedSize(true);


        /* searchList = new ArrayList<>();
        adapter = new SearchListAdapter(searchList);
        recyclerView.setAdapter(adapter);
        */

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
                if (editable.length() != 0) {
                    btnClear.setVisibility(View.VISIBLE);
                } else {
                    btnClear.setVisibility(View.GONE);
                }

                searchValue = etSearch.getText().toString().trim();
                searchData(searchValue);
//                searchInDatabase(searchValue);
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
                btnClear.setVisibility(View.GONE);
                /*etSearch.clearFocus();
                // hide keyboard from view
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                 .hideSoftInputFromWindow(etSearch.getWindowToken(), 0);*/
            }
        });

        return rootView;
    }

    private void searchData(String searchValue) {
        adapter1 = new FirestorePagingAdapter<User, FollowList.BlogViewHolder>(getOptions(getString(R.string.users), searchValue)) {
            @NonNull
            @Override
            public FollowList.BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
                return new FollowList.BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FollowList.BlogViewHolder holder, final int position, @NonNull final User model) {
                MyFirestorePagingAdapter.hideProgress(rootView);
                errorText.setVisibility(View.GONE);

                String userID = getItem(position).getId();
                DocumentReference userRef = db.collection(getString(R.string.users)).document(userID);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            User user = task.getResult().toObject(User.class);
                            String profileUrl = user.getProfileUrl();
                            String username = user.getUsername();
                            String occupation = user.getOccupation();

                            holder.setUserData(username, occupation, getActivity(), profileUrl);

                        } else {
                            // delete void field
                            getItem(position).getReference().delete();
                            refresh();
                        }
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // open profile
                        openActivity(FeedUserProfile.class, requireContext(), "userID", getItem(position).getId());
                    }
                });
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                if (state == LoadingState.FINISHED) {
                    if (getItemCount() == 0) {
                        MyFirestorePagingAdapter.hideProgress(rootView);
                    }
                }
            }
        };

        // second adapter for posts
        adapter2 = getPosts(getOptions("posts", searchValue));

        // set two adapters
        ConcatAdapter concatenated = new ConcatAdapter(adapter1, adapter2);
        recyclerView.setAdapter(concatenated);
    }

    private FirestorePagingAdapter<User, PostViewHolder> getPosts(FirestorePagingOptions<User> options) {
        return new FirestorePagingAdapter<User, PostViewHolder>(options) {

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
                return new PostViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, final int position, @NonNull User model) {
                errorText.setVisibility(View.GONE);

                String postID = getItem(position).getId();
                DocumentReference postRef = db.collection("posts").document(postID);
                postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            String product = task.getResult().get("product").toString();
                            String price = task.getResult().get("price").toString();
                            String imageUrl = task.getResult().get("imageUrl").toString();

                            String offers = null;
                            if (task.getResult().get("offers") != null) {
                                offers = task.getResult().get("offers").toString();
                            }

                            holder.setData(product, price, offers);
                            holder.setImageURL(getActivity(), imageUrl);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    openActivity(PostView.class, requireContext(), "postID", getItem(position).getId());
                                }
                            });
                        }
                    }
                });
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                if (state == LoadingState.FINISHED) {
                    checkCount();
                }
            }
        };
    }

    private void checkCount() {
        if (adapter1.getItemCount() == 0 && adapter2.getItemCount() == 0) {
            MyFirestorePagingAdapter.hideProgress(rootView);
            errorText.setText("No Results");
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private FirestorePagingOptions<User> getOptions(String path, String searchValue) {
        // search query
        Query reference = db.collection(path).whereArrayContains("search_keywords", searchValue.toLowerCase());

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(3)
                .build();

        return new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(reference, config, User.class)
                .build();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img;
        private final View mView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            View viewLoc = mView.findViewById(R.id.viewLocation);
            viewLoc.setVisibility(View.GONE);

            img = mView.findViewById(R.id.imageView);
            img.getLayoutParams().height = 130;
            img.getLayoutParams().width = 130;
            img.requestLayout();
        }

        public void setData(String product, String price, String offers) {
            TextView textView = mView.findViewById(R.id.tvProduct);
            TextView textView2 = mView.findViewById(R.id.tvPrice);
            TextView textView3 = mView.findViewById(R.id.tvOffers);

            View viewOffers = mView.findViewById(R.id.viewOffers);

            textView.setText(product);

            // string formatter to add commas to currency
            String formatted;
            if (!price.isEmpty()) {
                int amount = Integer.parseInt(price);
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                formatted = decimalFormat.format(amount);

            } else {
                // empty value
                formatted = price + "0";
            }

            textView2.setText(String.format("UGX %s", formatted));

            if (offers != null) {
                textView3.setText(offers);
                viewOffers.setVisibility(View.VISIBLE);
            }

        }

        public void setImageURL(final Context ctx, String imageURL) {
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }
    }

   /* private void searchInDatabase(String searchValue) {
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
    }*/

   /* private static class SearchListAdapter extends RecyclerView.Adapter<SearchViewHolder> {
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
    }*/

}