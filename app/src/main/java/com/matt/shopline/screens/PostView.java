package com.matt.shopline.screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.matt.shopline.R;
import com.matt.shopline.objects.Comment;
import com.matt.shopline.objects.User;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.matt.shopline.fragments.profile.Catalog.durationFromNow;

public class PostView extends AppCompatActivity {

    private String postID;
    private Query postComments;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private EditText etComment;
    private FirestorePagingAdapter adapter;
    private RecyclerView mCommentsList;
    private DocumentReference deleteRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.comments);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        postID = intent.getStringExtra("postID");

//        Toast.makeText(this, postID, Toast.LENGTH_SHORT).show();

        View btnComment = findViewById(R.id.btnComment);
        etComment = findViewById(R.id.etComment);

        mCommentsList = findViewById(R.id.recView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mCommentsList.setLayoutManager(mLayoutManager);

        postComments = db.collection("posts").document(postID)
                .collection(getString(R.string.comments).toLowerCase())
                .orderBy(getString(R.string.timestamp), Query.Direction.DESCENDING);

        getComments();

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etComment.getText().toString().trim().isEmpty()) {
                    addComment();
                }

            }
        });

    }

    private void getComments() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<Comment> options = new FirestorePagingOptions.Builder<Comment>()
                .setLifecycleOwner(this)
                .setQuery(postComments, config, Comment.class)
                .build();

        adapter = new FirestorePagingAdapter<Comment, BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull final Comment model) {
                String comment = model.getComment();
                userID = model.getUserID();
                long timestamp = model.getTimestamp();

                holder.setCommentData(comment, timestamp);

                if (userID != null) {
                    DocumentReference userRef = db.collection(getString(R.string.users)).document(userID);
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User user = task.getResult().toObject(User.class);
                            String profileUrl = user.getProfileUrl();
                            String username = user.getUsername();
                            String occupation = user.getOccupation();

                            holder.setUserData(username, occupation, getApplicationContext(), profileUrl, model.getUserID());

                        }
                    });
                }

                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // get reference for item to be deleted
                        deleteRef = getItem(position).getReference();
                        userID = model.getUserID();

                        registerForContextMenu(view);
                        return false;
                    }
                });
            }
        };
        mCommentsList.setAdapter(adapter);
    }

    private void addComment() {
        CollectionReference postComments = db.collection("posts").document(postID)
                .collection(getString(R.string.comments).toLowerCase());
        // constructor to set data
        Comment comment = new Comment(user.getUid(), etComment.getText().toString().trim(), System.currentTimeMillis());
        postComments.add(comment)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        adapter.refresh();
                    }
                });
        etComment.setText(null);
    }

    private void deleteComment(DocumentReference reference) {
        // delete item
        reference.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        adapter.refresh();
                        Toast.makeText(PostView.this, getString(R.string.delete) + "d", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // show option for user comments only
        if (user.getUid().equals(userID)) {
            menu.add(0, v.getId(), 0, R.string.delete);
        }

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() == getString(R.string.delete)) {
            deleteComment(deleteRef);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCommentData(String comment, long timestamp) {
            TextView textView = mView.findViewById(R.id.tvComment);
            TextView textView2 = mView.findViewById(R.id.tvTime);
            textView.setText(comment);

            SimpleDateFormat sdf = new SimpleDateFormat();
            String newTime = sdf.format(timestamp);
            try {
                // convert new sdf to date
                Date d = sdf.parse(newTime);
                textView2.setText(durationFromNow(d));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public void setUserData(String username, String occupation, final Context ctx, String imageURL, final String userID) {
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

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open profile
                    Intent intent = new Intent(mView.getContext(), FeedUserProfile.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                }
            });
        }
    }

}