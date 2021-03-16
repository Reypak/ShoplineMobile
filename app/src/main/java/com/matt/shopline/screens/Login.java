package com.matt.shopline.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.matt.shopline.R;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText etEmail;
    private EditText etPwrd;
    private Button btnLogin;
//    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        etEmail = findViewById(R.id.etEmail);
        etPwrd = findViewById(R.id.etPwrd);
        btnLogin = findViewById(R.id.login);

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

                                // send broadcast to Landing page
                                Intent intent = new Intent("finish");
                                sendBroadcast(intent);

                                Toast.makeText(Login.this, "Logged in as " + task.getResult().getUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, "Authentication failed. Check network connection.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
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
}