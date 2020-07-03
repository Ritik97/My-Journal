package com.example.myjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private ImageView addImage;
    private ImageView image;
    private TextView username;
    private TextView date;
    private EditText title;
    private EditText thought;
    private ProgressBar progressBar;

    private Button save;

    private String currentUserName;
    private String currentUserId;


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //connection to firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);//To make the Action bar disappear

        progressBar = findViewById(R.id.post_progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        addImage = findViewById(R.id.post_camera_bt);
        image = findViewById(R.id.post_imageview);
        username = findViewById(R.id.post_username_textview);
        date = findViewById(R.id.post_date_textview);
        title = findViewById(R.id.post_title_et);
        thought = findViewById(R.id.post_thought_et);
        save = findViewById(R.id.post_save_thought_bt);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        addImage.setOnClickListener(this);
        save.setOnClickListener(this);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUsername();

            username.setText(currentUserName);
            Date currentDate = Calendar.getInstance().getTime();
            String formattedDate = DateFormat.getDateInstance().format(currentDate);
            date.setText(formattedDate);

        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //user already loggedin

                } else {

                }
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.post_camera_bt:
                //get image from gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;

            case R.id.post_save_thought_bt:
                //save journal
                saveJournal();
                break;


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                image.setImageURI(imageUri);

            }

        }
    }

    private void saveJournal() {

        final String currentTitle = title.getText().toString().trim();
        final String currentThought = thought.getText().toString().trim();
        if (!TextUtils.isEmpty(currentTitle) && !TextUtils.isEmpty(currentThought) && imageUri != null) {

            progressBar.setVisibility(View.VISIBLE);
            final StorageReference filePath = storageReference
                    .child("journal_image")  //Folder Name
                    .child("my_image" + Timestamp.now().getSeconds()); //File Name

            filePath.putFile(imageUri)  //Adding Image
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    //todo:Create a journal object
                                    Journal journal = new Journal();
                                    journal.setTitle(currentTitle);
                                    journal.setThought(currentThought);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUsername(currentUserName);
                                    journal.setUserId(currentUserId);

                                    //todo:invoke collection reference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(PostJournalActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.INVISIBLE);

                                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("onFailure: ", e.getMessage());

                                        }
                                    });
                                }
                            });


                            //todo:and save journal  instance

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.d("onFailure: ", e.getMessage());
                }
            });

        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


}


