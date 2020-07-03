package com.example.myjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView email;
    private EditText password;
    private ProgressBar progressBar;
    private Button loginButton;
    private Button signUpButton;

    //Declaring FireStore Instances
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Setting up the FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);//To make the Action bar disappear

        progressBar = findViewById(R.id.progressbar_login);
        email = findViewById(R.id.email_field_login);
        password = findViewById(R.id.password_field_login);
        loginButton = findViewById(R.id.bt_login);
        signUpButton = findViewById(R.id.bt_create_account_login);
        firebaseAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccount.class));

            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(email.getText().toString())
                        && !TextUtils.isEmpty(password.getText().toString())) {
                    loginEmailPasswordUser(email.getText().toString().trim(), password.getText().toString().trim());
                } else {
                    Toast.makeText(LoginActivity.this, "Empty Fields Not Required", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loginEmailPasswordUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        //Invoking Firebase's Login Method
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            final String userId = currentUser.getUid();

                            //Getting user's data from collection using userId
                            collectionReference
                                    .whereEqualTo("userId", userId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                            @Nullable FirebaseFirestoreException e) {
                                            assert queryDocumentSnapshots != null;
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                    JournalApi journalApi = JournalApi.getInstance();
                                                    journalApi.setUserId(userId);
                                                    journalApi.setUsername(snapshot.getString("username"));
                                                    progressBar.setVisibility(View.INVISIBLE);

                                                    startActivity(new Intent(LoginActivity.this, PostJournalActivity.class));
                                                }
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("onFailure: ", e.getMessage());
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        /* firebaseAuth.addAuthStateListener(authStateListener);*/
    }
}
