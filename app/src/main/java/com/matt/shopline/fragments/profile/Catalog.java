package com.matt.shopline.fragments.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matt.shopline.R;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.Orders;
import com.matt.shopline.screens.PostView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Catalog extends Fragment {

    private Query userCatalog;
    private FirebaseUser user;
    private String userID;
    private FirebaseFirestore db;
    private RecyclerView mCatalogList;
    private FirestorePagingAdapter adapter;
    private String imageUrl;
    private String product;
    private String postID;
    private DocumentReference postRef;
    private CollectionReference postLikes;
    private String duserID;
    private String price;
    private String offers;
    private String username;
    private CollectionReference userOrders;
    private SwipeRefreshLayout refreshLayout;

    public static String durationFromNow(Date startDate) {

        long different = System.currentTimeMillis() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long weeksInMilli = daysInMilli * 7;
        long monthsInMilli = daysInMilli * 30;
        long yearsInMilli = monthsInMilli * 12;

        long elapsedYears = different / yearsInMilli;
        different = different % yearsInMilli;

        long elapsedMonths = different / monthsInMilli;
        different = different % monthsInMilli;

        long elapsedWeeks = different / weeksInMilli;
        different = different % weeksInMilli;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        String output = "";
        if (elapsedYears > 0) {
            output += elapsedYears + "y";
        } else if (elapsedYears > 0 || elapsedMonths > 0) {
            output += elapsedMonths + " mon";
        } else if (elapsedMonths > 0 || elapsedWeeks > 0) {
            output += elapsedWeeks + "w";
        } else if (elapsedWeeks > 0 || elapsedDays > 0) {
            output += elapsedDays + "d";
        } else if (elapsedDays > 0 || elapsedHours > 0) {
            output += elapsedHours + "h";
        } else if (elapsedHours > 0 || elapsedMinutes > 0) {
            output += elapsedMinutes + "m";
        } else if (elapsedMinutes > 0 || elapsedSeconds > 0) {
            output += elapsedSeconds + "s";
        }

        return output;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_catalog, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        /*user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();*/

        Bundle bundle = getArguments();
        if (bundle != null) {
            // get userID stored by TabAdapter
            userID = bundle.getString("userID");
        }

        db = FirebaseFirestore.getInstance();

        if (userID != null) {
            userCatalog = db.collection(getString(R.string.users))
                    .document(userID)
                    .collection(getString(R.string.catalog).toLowerCase()).orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
        } else {
            // view user WishList
            userID = user.getUid();
            userCatalog = db.collection(getString(R.string.users))
                    .document(userID)
                    .collection(getString(R.string.wishlist))
                    .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);
            // negative value arrangement
        }

        mCatalogList = rootView.findViewById(R.id.recView);
        refreshLayout = rootView.findViewById(R.id.swipeRefresh);
        mCatalogList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mCatalogList.setLayoutManager(mLayoutManager);

        getPosts();

        return rootView;
    }

    private void getPosts() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(2)
                .setEnablePlaceholders(true)
//                .setPrefetchDistance(1)
                .setPageSize(1)
                .build();


        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userCatalog, config, User.class)
                .build();

      /*  FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(userCatalog, User.class)
                .build();*/

        adapter = new FirestorePagingAdapter<User, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final User model) {

                ImageButton imageButton = holder.mView.findViewById(R.id.btnOptions);
                View btnOrder = holder.mView.findViewById(R.id.btnOrder);
                ImageView img = holder.mView.findViewById(R.id.profile_image);
                final ImageButton btnLike = holder.mView.findViewById(R.id.btnLike);
                final TextView tvLikes = holder.mView.findViewById(R.id.tvLikes);

                postID = getItem(position).getId();
                postRef = db.collection("posts").document(postID);
//                Toast.makeText(getActivity(), postID, Toast.LENGTH_SHORT).show();
                postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                product = task.getResult().get("product").toString();
                                price = task.getResult().get("price").toString();
                                String description = task.getResult().get("description").toString();
                                imageUrl = task.getResult().get("imageUrl").toString();
                                duserID = task.getResult().get("userID").toString();
                                String timestamp = task.getResult().get(getString(R.string.timestamp)).toString();

                                // check if offers field exists
                                offers = null; // set offers value to null
                                if (task.getResult().get("offers") != null) {
                                    offers = task.getResult().get("offers").toString();
                                    View btnOffers = holder.mView.findViewById(R.id.btnOffers);
                                    btnOffers.setVisibility(View.VISIBLE); // visible offers button
                                    btnOffers.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            offers = task.getResult().get("offers").toString();
                                            String product = task.getResult().get("product").toString();
                                            // create dialog message
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(product);
                                            builder.setIcon(R.drawable.ic_gift);
                                            builder.setMessage(offers);
                                            builder.show();
                                        }
                                    });
                                }

                                // get comments from counter
                                if (task.getResult().get("comments") != null) {
                                    String comments = task.getResult().get("comments").toString();
                                    TextView tvComments = holder.mView.findViewById(R.id.tvComments);
                                    tvComments.setText(comments);
                                }

                                holder.setData(product, price, description, timestamp);
                                holder.setImageURL(getActivity(), imageUrl);

                                DocumentReference userRef = db.collection(getString(R.string.users)).document(duserID);
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        username = task.getResult().get("username").toString();
                                        String occupation = task.getResult().get("occupation").toString();
                                        String profileUrl = task.getResult().get("profileUrl").toString();
                                        holder.setUserData(username, occupation, getActivity(), profileUrl);
                                    }
                                });
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
                    }
                });

                // get post likes
                postRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null) {
                            if (value.get("likes") != null) {
                                String likes = value.get("likes").toString();
                                tvLikes.setText(likes);
                            }
                        }
                    }
                });

                // get post likes
                final CollectionReference likeRef = postRef.collection("likes");
               /* likeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null) {
                            // count number of Documents
                            int likes = value.getDocuments().size();
                            tvLikes.setText(String.valueOf(likes));
                        }

                    }
                });*/

                // check if current userID exists
                likeRef.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // show post liked
                            btnLike.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite));
                            tvLikes.setTextColor(Color.RED);
                        }
                    }
                });

                btnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postID = getItem(position).getId();
                        likeRef.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    // remove like
                                    unlikePost();

                                    // back to default
                                    btnLike.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_border));
                                    tvLikes.setTextColor(getResources().getColor(R.color.colorTextSecondary));
                                } else {
                                    // liked state
                                    btnLike.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite));
                                    tvLikes.setTextColor(Color.RED);

                                    // bounce animation
                                    ScaleAnimation animation = new ScaleAnimation(1, (float) 0.7, 1, (float) 0.7,
                                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    animation.setDuration(300);
                                    animation.setRepeatMode(Animation.REVERSE);
                                    animation.setRepeatCount(3);

                                    btnLike.startAnimation(animation);

                                    likePost();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        "Check Internet Connection", Snackbar.LENGTH_LONG)
                                        .setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                            }
                                        });

                                snackbar.show();
                            }
                        });
                    }
                });

                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // rerun the onBind method to get required data
                        onBindViewHolder(holder, position, model);
                        // display popup
                        showPopup(view);
                    }
                });


                btnOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBindViewHolder(holder, position, model);

                        // delay to allow data collection
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // post is for another user load order Window
                                if (!duserID.equals(user.getUid())) {
                                    holder.setOrderData(offers, duserID, postID);
                                } else {
                                    // open orders
                                    loadActivity(Orders.class, null, null);

                                }
                            }
                        }, 100);

                    }
                });

                // profile image click
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        onBindViewHolder(holder, position, model);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                loadActivity(FeedUserProfile.class, "userID", duserID);

                            }
                        }, 100);

                    }
                });

                // post comments
                holder.setComments(getItem(position).getId());

            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                        // The initial load has begun
                        // ...
                    case LOADING_MORE:
                        refreshLayout.setRefreshing(true);
                        // The adapter has started to load an additional page
                        // ...
                    case LOADED:
                        refreshLayout.setRefreshing(false);
                        // The previous load (either initial or additional) completed
                        // ...
                    case ERROR:
                        refreshLayout.setRefreshing(false);
                        // The previous load (either initial or additional) failed. Call
                        // the retry() method in order to retry the load operation.
                        // ...
                    case FINISHED:
                        refreshLayout.setRefreshing(false);
                }
            }
        };
        mCatalogList.setAdapter(adapter);
    }

    private void unlikePost() {
        postLikes = db.collection("posts").document(postID)
                .collection("likes");
        // delete current userID document
        postLikes.document(user.getUid()).delete();
    }

    private void likePost() {
        // post ref
        postLikes = db.collection("posts").document(postID)
                .collection("likes");
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("exists", true);

        // send currentUserID to post likes collection
        postLikes.document(user.getUid()).set(userdata);
    }

    private void Order(final String userID, final String postID, String qty, String location) {
        // orders ref
        userOrders = db.collection(getString(R.string.users)).document(userID)
                .collection("orders_customer");
        final Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.timestamp), -System.currentTimeMillis());
        data.put("quantity", qty);
        data.put("userID", user.getUid());
        data.put("postID", postID);
        data.put(getString(R.string.location).toLowerCase(), location);

        // send currentUserID to post likes collection
        userOrders.add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                // current user order ref
                userOrders = db.collection(getString(R.string.users)).document(user.getUid())
                        .collection("orders_me");
                // remove redundant current userID
                data.remove("userID");
                userOrders.add(data);
                Toast.makeText(getActivity(), "Order Complete. Thank you for using Shopline", Toast.LENGTH_SHORT).show();

                loadActivity(Orders.class, getString(R.string.you), "");
                // load orders window

                DocumentReference orders = db.collection(getString(R.string.users))
                        .document(userID)
                        .collection("data")
                        .document(getString(R.string.title_orders).toLowerCase());

                orders.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // update orders by 1
                            documentSnapshot.getReference().update("orders", FieldValue.increment(1));
                        } else {
                            Map<String, Object> data = new HashMap<>();
                            data.put(getString(R.string.title_orders).toLowerCase(), 1);
                            documentSnapshot.getReference().set(data);
                        }
                    }
                });

            }
        });
    }

    private void loadActivity(Class aClass, String name, String value) {
        Intent intent = new Intent(getActivity(), aClass);
        if (name != null) {
            intent.putExtra(name, value); // send intent to load You Tab
        }
        startActivity(intent);
    }

    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);

        popupMenu.getMenu().add(R.string.share);

        // if current User
        if (duserID.equals(user.getUid())) {
            popupMenu.getMenu().add("Add to Features");
            popupMenu.getMenu().add("Edit");
            popupMenu.getMenu().add(R.string.delete);
            popupMenu.getMenu().add("Advertise Post");
        } else {
            // check bundle if it has catalog data or if not it is Wishlist and bundle is null
            Bundle bundle = getArguments();
            if (bundle != null) {
                popupMenu.getMenu().add(getString(R.string.add_wishlist));
            } else {
                popupMenu.getMenu().add("Remove");
            }

            popupMenu.getMenu().add("Report");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle() == getString(R.string.delete)) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(android.R.id.content), "Confirm " + getString(R.string.delete), Snackbar.LENGTH_LONG)
                            .setAction(R.string.yes, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    deletePost();
                                }
                            });

                    snackbar.show();
                } else if (menuItem.getTitle() == getString(R.string.add_wishlist)) {
                    addWishList();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void addWishList() {
        // current user wishList Ref
        CollectionReference userWishList = db.collection(getString(R.string.users))
                .document(user.getUid())
                .collection(getString(R.string.wishlist));

        Map<String, Object> data = new HashMap<>();
        // add timestamp for arrangement
        data.put(getString(R.string.timestamp), System.currentTimeMillis());

        userWishList.document(postID).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getActivity(), "Added to " + getString(R.string.wishlist), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deletePost() {
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), null,
                "Deleting Post" + getString(R.string.load), true);
        dialog.setCancelable(true);

        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference imageRef = mStorage.getReferenceFromUrl(imageUrl);
        // delete from Storage Ref
        imageRef.delete();

        // deleting record from FireStore post Reference
        postRef.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // delete from user Catalog Ref
                        CollectionReference userCatalog = db.collection(getString(R.string.users))
                                .document(userID)
                                .collection(getString(R.string.catalog).toLowerCase());
                        userCatalog.document(postID).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getActivity(), getString(R.string.delete) + "d " + product, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();

                                        // inform adapter of deleted item
                                        adapter.refresh();
                                    }
                                });
                    }
                });


    }

    public class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        private TextView textView2;
        private TextView textView;
        private ImageView img;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
            img = mView.findViewById(R.id.imageView);
            // re-post button
            ImageButton btnRepost = mView.findViewById(R.id.btnRepost);
            btnRepost.setColorFilter(getResources().getColor(R.color.colorHighlight));
        }

        public void setComments(final String postID) {
            // to handle click events
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open PostView
                    loadActivity(PostView.class, "postID", postID);
                }
            };

            ImageButton btnComment = mView.findViewById(R.id.btnComment);
            img.setOnClickListener(listener);
            btnComment.setOnClickListener(listener);
        }

        public void setOrderData(String offers, final String userID, final String postID) {
            final BottomSheetDialog dialog = new BottomSheetDialog(mView.getContext(), R.style.BottomSheetDialog);
            dialog.setContentView(R.layout.bottom_sheet);

            TextView tvProduct = dialog.findViewById(R.id.tvProduct);
            TextView tvPrice = dialog.findViewById(R.id.tvPrice);
            TextView tvOffers = dialog.findViewById(R.id.tvOffers);
            TextView tvLocation = dialog.findViewById(R.id.tvLocation);
            final EditText etQty = dialog.findViewById(R.id.etQuantity);
            TextView btnWish = dialog.findViewById(R.id.btnWishlist);
            TextView btnOrder = dialog.findViewById(R.id.btnOrder);
            TextView btnAdd = dialog.findViewById(R.id.btnAdd);
            TextView btnMin = dialog.findViewById(R.id.btnMin);

            tvProduct.setText(textView.getText().toString());
            tvPrice.setText(textView2.getText().toString());

            // get shared prefs
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String location = sharedPreferences.getString(getString(R.string.location).toLowerCase(), null);

            tvLocation.setText(R.string.no_location);

            if (location != null && !location.isEmpty()) {
                tvLocation.setText(location);
            }

            if (offers != null) {
                tvOffers.setText(offers);
            }

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int qty = Integer.parseInt(etQty.getText().toString());
                    qty++; // increment value
                    etQty.setText(String.valueOf(qty));
                }
            });
            btnMin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int qty = Integer.parseInt(etQty.getText().toString());
                    // stop at one
                    if (qty > 1) {
                        qty--; // decrement
                        etQty.setText(String.valueOf(qty));
                    }

                }
            });

            btnOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!etQty.getText().toString().equals("0")) {
                        // dialog box for confirmation
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Confirm Order?");
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Order(userID, postID, etQty.getText().toString(), location);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                }
            });

            btnWish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addWishList();
                    dialog.dismiss();
                }
            });

            dialog.show();
        }

        public void setData(String product, String price, String description, String timestamp) {
            textView = mView.findViewById(R.id.tvProduct);
            textView2 = mView.findViewById(R.id.tvPrice);
            TextView textView3 = mView.findViewById(R.id.tvDesc);
            TextView textView4 = mView.findViewById(R.id.tvTime);

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
            textView.setText(product);
            textView2.setText("UGX " + formatted);
            textView3.setText(description);

            SimpleDateFormat sdf = new SimpleDateFormat();
            // cast timestamp String to long
            long date = Long.parseLong(timestamp);
            // format the long value to sdf String
            String newTime = sdf.format(date);
            try {
                // convert new sdf to date
                Date d = sdf.parse(newTime);
                textView4.setText(durationFromNow(d));
            } catch (ParseException e) {
                e.printStackTrace();
            }

             /* // date converter
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("HH:mm · dd MMM yy");
            // convert timestamp string to long
            long date = Long.parseLong(timestamp);
            String newTime = sdf.format(date);
            textView4.setText(newTime);*/

        }

        public void setUserData(String username, String occupation, final Context ctx, String imageURL) {
            final TextView textView = mView.findViewById(R.id.tvUsername);
            TextView textView2 = mView.findViewById(R.id.tvOccupation);
            ImageView img = mView.findViewById(R.id.profile_image);

            textView.setText(username);
            if (occupation.isEmpty()) {
                textView2.setVisibility(View.GONE);
            } else {
                textView2.setText(occupation);
            }

            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
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

}