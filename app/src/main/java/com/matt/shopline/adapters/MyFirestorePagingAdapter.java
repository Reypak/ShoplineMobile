package com.matt.shopline.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matt.shopline.R;
import com.matt.shopline.fragments.home.Home;
import com.matt.shopline.objects.Post;
import com.matt.shopline.objects.User;
import com.matt.shopline.screens.FeedUserProfile;
import com.matt.shopline.screens.PostView;
import com.matt.shopline.screens.Upload;
import com.matt.shopline.screens.orders.Orders;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyFirestorePagingAdapter extends FirestorePagingAdapter<User, MyFirestorePagingAdapter.BlogViewHolder> {

    private final FirebaseUser user;
    private final FirebaseFirestore db;
    private final Context context;
    private final View rootView;
    private final Boolean b;
    private String imageUrl;
    private String product;
    private String description;
    private String postID;
    private String size;
    private DocumentReference postRef;
    private CollectionReference postLikes, reposts;
    private String duserID;
    private String price;
    private String offers;
    private String username;
    private CollectionReference userOrders;


    public MyFirestorePagingAdapter(FirestorePagingOptions<User> options, Context context, View rootView, Boolean b) {
        super(options);
        this.context = context;
        this.rootView = rootView;
        this.b = b;
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

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
            output += elapsedMonths + " mn";
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

    public static void hideProgress(View rootView) {
        // hide progress bar
        ProgressBar progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final User model) {
        hideProgress(rootView);
        hideSwipe(rootView);

        // check for repost userID value
        Object ruserID = getItem(position).get("ruserID");
        if (ruserID != null) {
            holder.showRepostUser(ruserID.toString());
        }

        ImageButton imageButton = holder.mView.findViewById(R.id.btnOptions);
        View btnOrder = holder.mView.findViewById(R.id.btnOrder);
        ImageView img = holder.mView.findViewById(R.id.profile_image);
        final ImageButton btnLike = holder.mView.findViewById(R.id.btnLike);
        final TextView tvLikes = holder.mView.findViewById(R.id.tvLikes);

        postID = getItem(position).getId();
        postRef = db.collection("posts").document(postID);
      /*  postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        final Post post = task.getResult().toObject(Post.class);
                        product = post.getProduct();
                        price = post.getPrice();
                        description = post.getDescription();
                        imageUrl = post.getImageUrl();
                        duserID = post.getUserID();
                        size = post.getSize();
                        String timestamp = String.valueOf(post.getTimestamp());

                        // check if offers field exists
                        offers = null; // set offers value to null
                        if (post.getOffers() != null) {
                            offers = post.getOffers();
                            View btnOffers = holder.mView.findViewById(R.id.btnOffers);
                            btnOffers.setVisibility(View.VISIBLE); // visible offers button
                            btnOffers.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    offers = post.getOffers();
//                                    String product = task.getResult().get("product").toString();
                                    // create dialog message
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(post.getProduct());
                                    builder.setIcon(R.drawable.ic_gift);
                                    builder.setMessage(offers);
                                    builder.show();
                                }
                            });
                        }
                        // setting item size
                        if (size != null) {
                            TextView tvSize = holder.mView.findViewById(R.id.tvSize);
                            tvSize.setVisibility(View.VISIBLE);
                            tvSize.setText(size);
                        }

                        holder.setData(product, price, description, timestamp);
                        holder.setImageURL(context, imageUrl);

                        DocumentReference userRef = db.collection("users").document(duserID);
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                User user = task.getResult().toObject(User.class);
                                username = user.getUsername();
                                String occupation = user.getOccupation();
                                String profileUrl = user.getProfileUrl();
                                holder.setUserData(username, occupation, context, profileUrl);
                            }
                        });
                    } else {
                        // if post does not exist
                        getItem(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                refresh();
                            }
                        });
                    }
                }
            }
        });*/

        postRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (value.exists()) {
                        final Post post = value.toObject(Post.class);
                        product = post.getProduct();
                        price = post.getPrice();
                        description = post.getDescription();
                        imageUrl = post.getImageUrl();
                        duserID = post.getUserID();
                        size = post.getSize();
                        long timestamp = post.getTimestamp();

                        // check if offers field exists
                        offers = null; // set offers value to null
                        if (post.getOffers() != null) {
                            offers = post.getOffers();
                            View btnOffers = holder.mView.findViewById(R.id.btnOffers);
                            btnOffers.setVisibility(View.VISIBLE); // visible offers button
                            btnOffers.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    offers = post.getOffers();
//                                    String product = task.getResult().get("product").toString();
                                    // create dialog message
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(post.getProduct());
                                    builder.setIcon(R.drawable.ic_gift);
                                    builder.setMessage(offers);
                                    builder.show();
                                }
                            });
                        }
                        // setting item size
                        if (size != null) {
                            TextView tvSize = holder.mView.findViewById(R.id.tvSize);
                            tvSize.setVisibility(View.VISIBLE);
                            tvSize.setText(size);
                        }

                        holder.setData(product, price, description, timestamp);
                        holder.setImageURL(context, imageUrl);

                        DocumentReference userRef = db.collection("users").document(duserID);
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User user = task.getResult().toObject(User.class);
                                    username = user.getUsername();
                                    String occupation = user.getOccupation();
                                    String profileUrl = user.getProfileUrl();
                                    holder.setUserData(username, occupation, context, profileUrl);
                                }
                            }
                        });
                    } else {
                        // if post does not exist
                        getItem(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                refresh();
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
                    // get reposts from counter
                    if (value.get("reposts") != null) {
                        TextView tvRepost = holder.mView.findViewById(R.id.tvRepost);
                        tvRepost.setText(value.get("reposts").toString());
                    }
                    // get comments from counter
                    if (value.get("comments") != null) {
                        TextView tvComments = holder.mView.findViewById(R.id.tvComments);
                        tvComments.setText(value.get("comments").toString());
                    }
                }
            }
        });

        // get post likes
        final CollectionReference likeRef = postRef.collection("likes");

        // check if current userID exists
        likeRef.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // show post liked
                    btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite));
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
                            btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_border));
                            tvLikes.setTextColor(context.getResources().getColor(R.color.colorTextSecondary));
                        } else {
                            // liked state
                            btnLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite));
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
                        Snackbar snackbar = Snackbar.make(rootView.getRootView().findViewById(android.R.id.content),
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
                }, 200);

            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // rerun the onBind method to get required data
                onBindViewHolder(holder, position, model);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // display popup
                        showPopup(view);
                    }
                }, 300);
            }
        });

        // post comments
        holder.setComments(getItem(position).getId());
        holder.setReposts(getItem(position).getId());
    }

    private void hideSwipe(View rootView) {
        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.swipeRefresh);
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onViewRecycled(@NonNull BlogViewHolder holder) {
        holder.setImageURL(context, null);
        holder.showRepostUser(null);
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        if (state == LoadingState.FINISHED) {
            if (getItemCount() == 0) {
                // hide progress bar
                hideProgress(rootView);
                hideSwipe(rootView);

                if (b) {
                    // reset shared prefs
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    sharedPreferences.edit().putString(context.getString(R.string.title_home), null).apply();
                    // reload home activity
                    ((FragmentActivity) context)
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.fragment_container, new Home())
                            .commit();
                }
            }
        }
    }

    private void repost(final String postID, boolean b) {
        // current user Following Ref
        final CollectionReference userCatalog = db.collection(context.getString(R.string.users))
                .document(user.getUid())
                .collection(context.getString(R.string.catalog).toLowerCase());
        // post re-post ref
        reposts = db.collection("posts").document(postID)
                .collection("reposts");

        if (b) {
            final Map<String, Object> data = new HashMap<>();
            data.put("exists", true);

            /*if () {
                data.put("mine", true);
            }*/

            // send currentUserID to re-post collection
            reposts.document(user.getUid()).set(data)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            data.remove("exists");
                            data.put(context.getString(R.string.timestamp), new Timestamp(new Date()));
                            data.put("ruserID", user.getUid());

                            userCatalog.document(postID).set(data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Re-posted", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        } else {
            reposts.document(user.getUid()).delete();
            // check repost data
            userCatalog.document(postID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult().exists()) {
                        DocumentReference delRef = task.getResult().getReference();

                        // check if has mine value
                        if (task.getResult().get("mine") != null) {
                            // only remove ruserID
                            delRef.update("ruserID", FieldValue.delete());
                        } else {
                            // remove whole document
                            delRef.delete();
                        }
                    }
                }
            });
        }

    }

    private void addWishList() {
        // current user wishList Ref
        CollectionReference userWishList = db.collection(context.getString(R.string.users))
                .document(user.getUid())
                .collection(context.getString(R.string.wishlist));

        Map<String, Object> data = new HashMap<>();
        // add timestamp for arrangement
        data.put(context.getString(R.string.timestamp), System.currentTimeMillis());

        userWishList.document(postID).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context, "Added to " + context.getString(R.string.wishlist), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void Order(final String userID, final String postID, String qty, String location) {
        // orders ref
        userOrders = db.collection(context.getString(R.string.users)).document(userID)
                .collection("orders_customer");
        final Map<String, Object> data = new HashMap<>();
        data.put(context.getString(R.string.timestamp), -System.currentTimeMillis());
        data.put("quantity", qty);
        data.put("userID", user.getUid());
        data.put("postID", postID);
        data.put("status", 1);
        data.put(context.getString(R.string.location).toLowerCase(), location);

        // send currentUserID to seller orders collection
        userOrders.add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                // current user order ref
                userOrders = db.collection(context.getString(R.string.users)).document(user.getUid())
                        .collection("orders_me");
                // remove redundant current userID
                data.put("status", 0);
                data.remove("userID");
                userOrders.document(task.getResult().getId()).set(data);
                Toast.makeText(context, "Order Complete. Thank you for using Shopline", Toast.LENGTH_SHORT).show();

                loadActivity(Orders.class, context.getString(R.string.you), "");
                // load orders window

                DocumentReference orders = db.collection(context.getString(R.string.users))
                        .document(userID)
                        .collection("data")
                        .document(context.getString(R.string.title_orders).toLowerCase());

                orders.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // update orders by 1
                            documentSnapshot.getReference().update("orders", FieldValue.increment(1));
                        } else {
                            Map<String, Object> data = new HashMap<>();
                            data.put(context.getString(R.string.title_orders).toLowerCase(), 1);
                            documentSnapshot.getReference().set(data);
                        }
                    }
                });

            }
        });
    }

    private void loadActivity(Class<?> aClass, String name, String value) {
        Intent intent = new Intent(context, aClass);
        if (name != null) {
            intent.putExtra(name, value); // send intent to load You Tab
        }
        context.startActivity(intent);
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

    private void unlikePost() {
        postLikes = db.collection("posts").document(postID)
                .collection("likes");
        // delete current userID document
        postLikes.document(user.getUid()).delete();
    }

    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        popupMenu.getMenu().add(R.string.share);

        // if current User
        if (duserID.equals(user.getUid())) {
            popupMenu.getMenu().add("Add to Features");
            popupMenu.getMenu().add(R.string.edit);
            popupMenu.getMenu().add(R.string.delete);
            popupMenu.getMenu().add("Advertise Post");
        } else {
            popupMenu.getMenu().add(R.string.add_wishlist);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle() == context.getString(R.string.delete)) {
                    Snackbar snackbar = Snackbar
                            .make(rootView.getRootView().findViewById(android.R.id.content),
                                    "Confirm " + context.getString(R.string.delete),
                                    Snackbar.LENGTH_LONG)
                            .setAction(R.string.yes, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    deletePost();
                                }
                            });

                    snackbar.show();
                } else if (menuItem.getTitle() == context.getString(R.string.add_wishlist)) {
                    addWishList();
                } else if (menuItem.getTitle() == context.getString(R.string.edit)) {
                    // string array with data
                    String[] strings = {postID, product, price, description, offers, size};
                    Intent intent = new Intent(context, Upload.class);
                    intent.putExtra("data", strings);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void deletePost() {
        final ProgressDialog dialog = ProgressDialog.show(context, null,
                "Deleting Post" + context.getString(R.string.load), true);
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
                        CollectionReference userCatalog = db.collection(context.getString(R.string.users))
                                .document(user.getUid())
                                .collection(context.getString(R.string.catalog).toLowerCase());
                        userCatalog.document(postID).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, context.getString(R.string.delete) + "d " + product, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();

                                        // inform adapter of deleted item
                                        refresh();
                                    }
                                });
                    }
                });
    }

    public class BlogViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img;
        View mView;
        private TextView textView2;
        private TextView textView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
            img = mView.findViewById(R.id.imageView);

            img.post(new Runnable() {
                @Override
                public void run() {
                    // set view height same as width
                    img.getLayoutParams().height = img.getMeasuredWidth();
                    img.requestLayout();
                }
            });
        }

        public void showRepostUser(final String ruserID) {
            View viewRepostUser = mView.findViewById(R.id.viewRepostUser);
            final TextView tvRepostUser = mView.findViewById(R.id.tvRepostUser);
            viewRepostUser.setVisibility(View.GONE);
            if (ruserID != null) {
                viewRepostUser.setVisibility(View.VISIBLE);

                DocumentReference userRef = db.collection("users").document(ruserID);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            User user = task.getResult().toObject(User.class);
                            tvRepostUser.setText(user.getUsername() + " reposted");
                        }
                    }
                });
                // open profile
                viewRepostUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadActivity(FeedUserProfile.class, "userID", ruserID);
                    }
                });
            }
        }

        public void setReposts(final String postID) {
            // re-post button
            final ImageButton btnRepost = mView.findViewById(R.id.btnRepost);
            reposts = postRef.collection("reposts");
            reposts.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        btnRepost.setColorFilter(context.getResources().getColor(R.color.colorHighlight));
                    }
                }
            });

            btnRepost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (btnRepost.getColorFilter() != null) {
                        repost(postID, false);
                        btnRepost.clearColorFilter();
                    } else {
                        repost(postID, true);
                        btnRepost.setColorFilter(context.getResources().getColor(R.color.colorHighlight));
                    }

                }
            });
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
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String location = sharedPreferences.getString(context.getString(R.string.location).toLowerCase(), null);

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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

        public void setData(String product, String price, String description, long timestamp) {
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
            textView2.setText(String.format("UGX %s", formatted));
            textView3.setText(description);

            SimpleDateFormat sdf = new SimpleDateFormat();
            // cast timestamp String to long
//            long date = Long.parseLong(timestamp);
            // format the long value to sdf String
            String newTime = sdf.format(timestamp);
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

        public void setImageURL(final Context ctx, final String imageURL) {
            img.post(new Runnable() {
                @Override
                public void run() {

                    Picasso.with(ctx)
                            .load(imageURL)
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(img);
                }
            });
        }
    }
}
