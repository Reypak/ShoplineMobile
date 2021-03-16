package com.matt.shopline.fragments.orders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.objects.Order;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.PostView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class OrdersList extends Fragment {

    private Query userOrders;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirestorePagingAdapter<Order, BlogViewHolder> adapter;
    private RecyclerView mOrdersList;
    private LinearLayoutManager mLayoutManager;
    private Bundle bundle;
    private String product, price, imageUrl, offers, location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_orderslist, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        initialize();

        mOrdersList = rootView.findViewById(R.id.recView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mOrdersList.setLayoutManager(mLayoutManager);

        getOrders();

        return rootView;
    }

    private void initialize() {
        bundle = getArguments();
        if (bundle == null) {
            // load from Customers orders
            userOrders = db.collection(getString(R.string.users)).document(user.getUid())
                    .collection("orders_customer")
                    .orderBy(getString(R.string.timestamp));
            // reset orders counter
            ResetOrdersCounter();
        } else {
            // load from user orders
            userOrders = db.collection(getString(R.string.users)).document(user.getUid())
                    .collection("orders_me")
                    .orderBy(getString(R.string.timestamp));
        }
    }

    private void getOrders() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(1)
                .build();

        FirestorePagingOptions<Order> options = new FirestorePagingOptions.Builder<Order>()
                .setLifecycleOwner(this)
                .setQuery(userOrders, config, Order.class)
                .build();

        adapter = new FirestorePagingAdapter<Order, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final Order model) {
//                String itemID = getItem(position).getId();
                String postID = model.getPostID();
                final String location = model.getLocation();
                final String quantity = " (" + model.getQuantity() + ")";
                final String userID = model.getUserID();

                DocumentReference postRef = db.collection("posts").document(postID);
                postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            product = task.getResult().get("product").toString();
                            price = task.getResult().get("price").toString();
                            imageUrl = task.getResult().get("imageUrl").toString();

                            offers = null;
                            if (task.getResult().get("offers") != null) {
                                offers = task.getResult().get("offers").toString();
                            }

                            holder.setData(product + quantity, price, offers, location);
                            holder.setImageURL(getActivity(), imageUrl);

                            // get User details
                            if (userID != null) {
                                DocumentReference userRef = db.collection("users").document(userID);
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        String profileUrl = task.getResult().get("profileUrl").toString();
                                        holder.setUserData(getActivity(), profileUrl);

                                        ImageView img = holder.mView.findViewById(R.id.profile_image);
                                        img.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(getActivity(), FeedUserProfile.class);
                                                intent.putExtra("userID", userID);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                });
                            }
                        } else {
                            // if post does not exist
                            getItem(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    adapter.refresh();
                                    Toast.makeText(getActivity(), "Removed Deleted Post", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), PostView.class);
                        intent.putExtra("postID", model.getPostID()); // send intent to load You Tab
                        startActivity(intent);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // get Doc reference of item and delete
                        getItem(position).getReference().delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        adapter.refresh();
                                        Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return false;
                    }
                });
            }

        };
        mOrdersList.setAdapter(adapter);
    }

    private void ResetOrdersCounter() {
        DocumentReference orders = db.collection(getString(R.string.users))
                .document(user.getUid())
                .collection("data")
                .document(getString(R.string.title_orders).toLowerCase());
        orders.update("orders", 0);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserData(final Context ctx, String imageURL) {
            ImageView img = mView.findViewById(R.id.profile_image);
            img.setVisibility(View.VISIBLE);
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }

        public void setData(String product, String price, String offers, String location) {
            TextView textView = mView.findViewById(R.id.tvProduct);
            TextView textView2 = mView.findViewById(R.id.tvPrice);
            TextView textView3 = mView.findViewById(R.id.tvOffers);
            TextView textView4 = mView.findViewById(R.id.tvLocation);
            View viewOffers = mView.findViewById(R.id.viewOffers);

            textView.setText(product);
            textView4.setText(R.string.no_location);
            if (location != null) {
                textView4.setText(location);
            }

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

            textView2.setText("UGX " + formatted);

            if (offers != null) {
                textView3.setText(offers);
                viewOffers.setVisibility(View.VISIBLE);
            }

        }

        public void setImageURL(final Context ctx, String imageURL) {
            ImageView img = mView.findViewById(R.id.imageView);
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }
    }
}