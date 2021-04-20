package com.matt.shopline.screens.orders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.adapters.MyFirestorePagingAdapter;
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
    private String product;
    private String price;
    private String imageUrl;
    private String offers;
    private ViewGroup rootView;
    private Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_orderslist, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        initialize();

        mOrdersList = rootView.findViewById(R.id.recView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mOrdersList.setLayoutManager(mLayoutManager);
        mOrdersList.setHasFixedSize(true);

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
                MyFirestorePagingAdapter.hideProgress(rootView);
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
                            holder.setAction(model.getStatus(), getItem(position).getReference(), adapter, model.getUserID());

                            // get User details
                            if (userID != null) {
                                DocumentReference userRef = db.collection("users").document(userID);
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.getResult().exists()) {
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
                                        } else {
                                            // remove item
                                            getItem(position).getReference().delete();
                                            refresh();
                                        }
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
                    public boolean onLongClick(final View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                        popupMenu.getMenu().add(R.string.delete);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle() == getString(R.string.delete)) {
                                    // get Doc reference of item
                                    holder.deleteOrder(getItem(position).getReference(), view, 0, adapter);
                                }
                                return false;
                            }
                        });

                        popupMenu.show();
                        return false;
                    }
                });
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                if (state == LoadingState.FINISHED) {
                    if (getItemCount() == 0) {
                        MyFirestorePagingAdapter.hideProgress(rootView);
                        TextView errorText = rootView.findViewById(R.id.errorText);
                        errorText.setVisibility(View.VISIBLE);
                        String msg = "Orders from Your Customers will appear here";
                        if (bundle != null) {
                            msg = msg.replace("Your Customers", "You");
                        }
                        errorText.setText(msg);
                    }
                }
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
        DocumentReference userOrderRef;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setAction(final long state, final DocumentReference reference, final FirestorePagingAdapter adapter, final String userID) {
            Button btnAction = mView.findViewById(R.id.btnAction);
            Button btnAccept = mView.findViewById(R.id.btnAccept);
            btnAccept.setVisibility(View.GONE);
            btnAction.setVisibility(View.VISIBLE);
            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            // buyers order ref
            if (userID != null) {
                userOrderRef = db.collection("users").document(userID)
                        .collection("orders_me").document(reference.getId());
            }

            /*STATE
              0-Default
              1-Decline & Accept
              2-Declined
              3-Accepted/Approved ; Awaiting delivery
              4-Confirm Delivery
              5-Pending approval
              6-Rate Order - pop up
              7-Delivered
              */
            if (state == 0) {
                btnAction.setText("Cancel");
            } else if (state == 1) {
                btnAction.setText("Decline");
                btnAccept.setVisibility(View.VISIBLE);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateOrderStatus(userOrderRef, reference, 3, view, "Accepted", adapter);
//                        updateOrderStatus(reference, 4, view, "State 4");
//                        adapter.refresh();
                    }
                });
            } else if (state == 2) {
                btnAction.setText("Declined");
            } else if (state == 3) {
                btnAction.setText("Confirmed");
            } else if (state == 4) {
                btnAction.setText("Confirm Delivery");
            } else if (state == 5) {
                btnAction.setText("Pending Approval");
            }

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (state == 0 || state == 1 || state == 2) {
                        deleteOrder(reference, view, state, adapter);
                    } else if (state == 4) {
                        updateOrderStatus(reference, null, 5, view, "Pending", null);
                    }
                }
            });
        }

        private void deleteOrder(final DocumentReference reference, final View view, final long state, final FirestorePagingAdapter adapter) {
            // dialog box for confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Cancel Order?");
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // delete order record
                    reference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            adapter.refresh();
                            Toast.makeText(view.getContext(), "Canceled", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // order declined
                    if (state == 1) {
                        updateOrderStatus(userOrderRef, null, 2, view, "Declined", null);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }

        private void updateOrderStatus(final DocumentReference reference, final DocumentReference reference2, final long state, final View view, final String msg, final FirestorePagingAdapter adapter) {
            // update order status
            reference.update("status", state).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // if order accepted
                        if (state == 3) {
                            // requires 2nd reference for personal customers Orders ref state to 4
                            updateOrderStatus(reference2, null, 4, view, "State 4", adapter);
                        } else if (state == 4) {
                            // refresh adapter
                            adapter.refresh();
                        }
                        Toast.makeText(view.getContext(), msg, Toast.LENGTH_SHORT).show();
                    } else {
                        // else order was canceled
                        Toast.makeText(view.getContext(), "Order was canceled!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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