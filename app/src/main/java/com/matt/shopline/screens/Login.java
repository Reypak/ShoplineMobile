package com.matt.shopline.screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.matt.shopline.R;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText etEmail;
    private EditText etPwrd;
    //    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        etEmail = findViewById(R.id.etEmail);
        etPwrd = findViewById(R.id.etPwrd);
        Button btnLogin = findViewById(R.id.login);
        final ImageButton btnToggle = findViewById(R.id.btnToggle);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginEmail = etEmail.getText().toString().trim();
                String loginPwrd = etPwrd.getText().toString();

                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPwrd)) {
                    final ProgressDialog dialog = ProgressDialog.show(Login.this, "",
                            "Logging in...", true);
                    dialog.show();

                    mAuth.signInWithEmailAndPassword(loginEmail, loginPwrd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                openMain();
                                Register register = new Register();
                                register.setToken(mAuth.getCurrentUser().getUid());

                                // subscribe Notifications
                                // TODO : Change to Register Window
                                FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid() + "_notifications");

                                // send broadcast to Landing page
                                Intent intent = new Intent("finish");
                                sendBroadcast(intent);

                                Toast.makeText(Login.this, "Logged in as " + task.getResult().getUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                                String msg = task.getException().getMessage();
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePassword(getApplicationContext(), etPwrd, btnToggle);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            openMain();
        }
    }

    public void togglePassword(Context context, TextView etPwrd, ImageButton btnToggle) {
        // change password box and icon
        if (etPwrd.getTransformationMethod() == null) {
            // hide
            btnToggle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_eye_off));
            etPwrd.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            // show
            btnToggle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_eye));
            etPwrd.setTransformationMethod(null);
        }
    }

    private void openMain() {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
        finish();
    }

    public void openRegister(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
        finish();
    }

    public void resetPassword(View view) {
        String email = etEmail.getText().toString().trim();
        if (!email.isEmpty()) {
            final ProgressDialog dialog = ProgressDialog.show(Login.this, null, "Sending " + getString(R.string.email) + getString(R.string.load));
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Login.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
        } else {
            etEmail.requestFocus();
            Toast.makeText(this, "Fill in Password Reset Email", Toast.LENGTH_SHORT).show();
        }

    }
}