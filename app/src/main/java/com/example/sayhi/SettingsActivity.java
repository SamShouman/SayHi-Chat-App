package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private TextView name,status;
    private DatabaseReference mRef;
    private String currUserID;
    private ProgressDialog progressDialog;
    private StorageReference sRef;
    private CircleImageView circleImageView;
    private static final int ID=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currUser != null;
        currUserID= currUser.getUid();
        mRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currUserID);
        mRef.keepSynced(true);
        sRef= FirebaseStorage.getInstance().getReference();

        name=findViewById(R.id.name);
        status=findViewById(R.id.status);
        Button changePic = findViewById(R.id.changePic);
        Button changeStatus = findViewById(R.id.changeStatus);
        circleImageView=findViewById(R.id.circleImageView);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                String userName= Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                String userStatus= Objects.requireNonNull(dataSnapshot.child("Status").getValue()).toString();
                final String userImage= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();


                name.setText(userName);
                status.setText(userStatus);
                if(!userImage.equals("default"))
                Picasso.with(SettingsActivity.this).load(userImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.usersayhii).into(circleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(SettingsActivity.this).load(userImage).placeholder(R.drawable.usersayhii).into(circleImageView);
                    }
                });
                progressDialog.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"An error has occurred.",Toast.LENGTH_SHORT).show();
                Intent returnToMainActivity=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(returnToMainActivity);
                finishAffinity();
                finish();
            }
        });

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToStatusActivity=new Intent(getApplicationContext(),StatusActivity.class);
                startActivity(goToStatusActivity);
            }
        });

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Please Choose a Profile Picture"),ID);

            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] imageURL = new String[1];
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        imageURL[0] = Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                        if (!imageURL[0].equals("default")){

                            CharSequence[] remove={"Remove Profile Picture"};
                            AlertDialog.Builder builder=new AlertDialog.Builder(SettingsActivity.this);
                            builder.setItems(remove, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ProgressDialog p=new ProgressDialog(SettingsActivity.this);
                                    p.setMessage("Removing profile picture...");
                                    p.setCancelable(false);
                                    p.show();

                                    mRef.child("Image").setValue("default");
                                    StorageReference profileImagePath = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL[0]);
                                    profileImagePath.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            ConstraintLayout layout = findViewById(R.id.layout);
                                            if (task.isSuccessful()) {
                                                Snackbar.make(layout, "Profile picture deleted.", Snackbar.LENGTH_SHORT).show();
                                                circleImageView.setImageDrawable(getResources().getDrawable(R.drawable.usersayhii));
                                            } else {
                                                Toast.makeText(getApplicationContext(), "An error has occurred, please try again!"
                                                        , Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                    p.dismiss();

                                }
                            });
                            AlertDialog dialog= builder.create();

                            dialog.show();

                    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ID && resultCode == RESULT_OK ){
            assert data != null;
            Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(SettingsActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri resultUri = result.getUri();



                final StorageReference filePath=sRef.child("profile_images").child(currUserID+".jpg");





                final ProgressDialog progressDialog=new ProgressDialog(SettingsActivity.this);
                progressDialog.setMessage("Uploading photo...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                            mRef.child("Image").setValue(Objects.requireNonNull(task.getResult()).toString());
                            Toast.makeText(getApplicationContext(),"Photo uploaded successfully.",Toast.LENGTH_SHORT).show();

                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"An error has occurred. Please try again!",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnToMainActivity=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(returnToMainActivity);

    }

    @Override
    protected void onStart() {
        super.onStart();
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
