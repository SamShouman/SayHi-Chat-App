package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class CreatePostActivity extends AppCompatActivity {
    private static final int ID=1;
    private ImageView image;
    private TextView clickMe;
    private Bitmap photo;
    private ConstraintLayout layout;
    private Uri uri;
    private DatabaseReference mRef;
    private StorageReference sRef;
    private String currUser;
    private String generatedKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Toolbar myToolbar=findViewById(R.id.myToolbar);
        Button createPost = findViewById(R.id.createPost);
        image = findViewById(R.id.image);
        clickMe = findViewById(R.id.clickMe);
        layout=findViewById(R.id.layout);
        currUser= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        mRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(currUser);
        sRef= FirebaseStorage.getInstance().getReference();
        final TextInputEditText caption = findViewById(R.id.caption);

        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addImage();

            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addImage();

            }
        });



        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(photo==null)
                    Snackbar.make(layout,"Please choose a photo to post.",Snackbar.LENGTH_SHORT).show();

                else
                    {   final DatabaseReference mRef=FirebaseDatabase.getInstance().getReference().child("Posts").push();
                        generatedKey=mRef.getKey();

                        final StorageReference filePath=sRef.child("posts_images").child(generatedKey+".jpg");





                        final ProgressDialog progressDialog=new ProgressDialog(CreatePostActivity.this);
                        progressDialog.setMessage("Creating post...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        filePath.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(task.isSuccessful())
                                    return filePath.getDownloadUrl();
                                throw Objects.requireNonNull(task.getException());

                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                progressDialog.dismiss();
                                if(task.isSuccessful()) {

                                    String captionText= Objects.requireNonNull(caption.getText()).toString();
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Users").child(currUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Posts").child(generatedKey).child("Name")
                                                    .setValue(Objects.requireNonNull(dataSnapshot.child("Name").getValue())
                                                            .toString());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    mRef.child("Image").setValue(Objects.requireNonNull(task.getResult()).toString());
                                    mRef.child("Likes").setValue(0);
                                    mRef.child("Dislikes").setValue(0);
                                    mRef.child("Time").setValue(ServerValue.TIMESTAMP);
                                    mRef.child("From").setValue(currUser);

                                    if(captionText.equals(""))
                                        mRef.child("Caption").setValue("none");
                                    else
                                        mRef.child("Caption").setValue(Objects.requireNonNull(captionText));


                                    Toast.makeText(getApplicationContext(),"Post created successfully.",Toast.LENGTH_SHORT).show();

                                    Intent returnToNewsFeed=new Intent(CreatePostActivity.this,NewsFeed.class);
                                    returnToNewsFeed.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(returnToNewsFeed);
                                    finishAffinity();
                                    finish();

                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(),"An error has occurred. Please try again!",Toast.LENGTH_SHORT).show();
                                }

                            }
                        });



                    }


            }
        });



    }

    private String getTime() {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return "" +dateFormat.format(date);
    }

    private void addImage() {

        Intent galleryIntent=new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"Please Choose an Image"),ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==ID && resultCode==RESULT_OK)
        {
            try {
                assert data != null;

                assert data != null;
                uri = data.getData();
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                image.setImageBitmap(photo);
                image.setVisibility(View.VISIBLE);
                clickMe.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference mRef=FirebaseDatabase.getInstance().getReference().child("Users").child(currUser);
        mRef.child("Online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            mRef.child("Online").setValue(ServerValue.TIMESTAMP);
        }



    }
}
