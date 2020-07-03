package com.example.myjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccount extends AppCompatActivity {

    private EditText username;
    private EditText email;
    private EditText password;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //setting up firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("users");
    //private DocumentReference documentReference = db.collection("Users").document("demo-user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);//To make the Action bar disappear

        username = findViewById(R.id.username);
        email = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);
        progressBar = findViewById(R.id.progressbar);
        Button createNewAcc = findViewById(R.id.bt_create_account);
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    //user already loggedin
                } else {
                    //no user yet
                }
            }
        };

        createNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(username.getText().toString())
                        && !TextUtils.isEmpty(email.getText().toString())
                        && !TextUtils.isEmpty(password.getText().toString())) {

                    String user_name = username.getText().toString().trim();
                    String e_mail = email.getText().toString().trim();
                    String pass = password.getText().toString().trim();
                    createUserEmailAccount(user_name, e_mail, pass);
                } else {
                    Toast.makeText(CreateAccount.this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void createUserEmailAccount(final String uname, String email, String password) {
        if (!TextUtils.isEmpty(uname)
                && !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)) {

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String userID = currentUser.getUid();

                                //create user map so that we can create a user collection
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", userID);
                                userObj.put("username", uname);

                                //save to our firestore database

                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(CreateAccount.this, "Account Created", Toast.LENGTH_SHORT).show();
                                                Log.d("MyMsg", "here we go");
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    String name = task.getResult().getString("username");

                                                                    JournalApi journalApi = JournalApi.getInstance();
                                                                    journalApi.setUsername(name);
                                                                    journalApi.setUserId(userID);

                                                                    Intent intent = new Intent(CreateAccount.this, PostJournalActivity.class);
                                                                    intent.putExtra("username", name);
                                                                    intent.putExtra("userid", userID);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    progressBar.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CreateAccount.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                                Log.d("MyMsg", e.getMessage());
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateAccount.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
