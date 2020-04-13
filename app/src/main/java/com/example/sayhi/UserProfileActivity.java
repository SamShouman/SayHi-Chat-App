package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private TextView name,status,friends;
    private ImageView profilePicture;
    private Button sendRequest,decline;
    private DatabaseReference friendRequest;
    private DatabaseReference friendsTable;
    private DatabaseReference notifications;
    private DatabaseReference mForOnline;
    private ProgressDialog progressDialog;
    private String currentState;
    private String userKey;
    private String reqType;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        name=findViewById(R.id.name);
        status=findViewById(R.id.status);
        friends=findViewById(R.id.friends);
        profilePicture=findViewById(R.id.imageView);
        sendRequest=findViewById(R.id.sendRequest);
        decline=findViewById(R.id.decline);
        layout=findViewById(R.id.layout);
        userKey=getIntent().getStringExtra("UserID");

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userKey);
        mForOnline= FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        friendRequest=FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        friendsTable=FirebaseDatabase.getInstance().getReference().child("Friends");
        notifications=FirebaseDatabase.getInstance().getReference().child("Notifications");
        currentState="Not Friends";
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loading Profile");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseDatabase.getInstance().getReference().child("Friends").child(userKey).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long friendsNumber=dataSnapshot.getChildrenCount();
                friends.setText(friendsNumber + " Friends");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        decline.setEnabled(false);
        decline.setVisibility(View.INVISIBLE);

        if(userKey.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            sendRequest.setVisibility(View.INVISIBLE);
            sendRequest.setEnabled(false);
        }

        friendRequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userKey)){

                     reqType= Objects.requireNonNull(dataSnapshot.child(userKey).child("Type").getValue()).toString();
                    if(reqType.equals("received")){
                        currentState="Request Received";
                        sendRequest.setText("Accept Friend Request");
                        decline.setEnabled(true);
                        decline.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    }
                    else{
                        if(reqType.equals("sent"))
                        {
                            currentState="Requested";
                            sendRequest.setText("Cancel Friend Request");
                            decline.setVisibility(View.INVISIBLE);
                            decline.setEnabled(false);
                        }

                        else{

                            friendsTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userKey)){
                                        sendRequest.setText("UnFriend " + name.getText().toString());
                                        currentState="Friends";
                                        decline.setVisibility(View.INVISIBLE);
                                        decline.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"An error has occurred.",Toast.LENGTH_SHORT).show();
            }
        });

        friendsTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userKey)){
                    sendRequest.setText("UnFriend");
                    currentState="Friends";
                    decline.setVisibility(View.VISIBLE);
                    decline.setEnabled(true);
                    decline.setText("Check Timeline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressDialog.show();
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String userName= Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                String userStatus= Objects.requireNonNull(dataSnapshot.child("Status").getValue()).toString();
                String userImage= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();

                name.setText(userName);
                status.setText(userStatus);
                Picasso.with(UserProfileActivity.this).load(userImage).placeholder(R.drawable.usersayhii).into(profilePicture);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(UserProfileActivity.this,"An error has occurred.",Toast.LENGTH_SHORT).show();
            }
        });

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                if(currentState.equals("Not Friends")){
                    progressDialog.setMessage("Sending Friend Request...");
                    progressDialog.setTitle("Add");
                    progressDialog.show();
                    sendRequest.setEnabled(false);
                    decline.setVisibility(View.INVISIBLE);
                    decline.setEnabled(false);
                    friendRequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                            child(userKey).
                            child("Type").
                            setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                friendRequest.child(userKey).
                                        child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("Type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendRequest.setEnabled(true);
                                        currentState="Requested";
                                        HashMap<String,String> notData=new HashMap<>();
                                        notData.put("from",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        notData.put("type","request");

                                        notifications.child(userKey).push().setValue(notData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                sendRequest.setText("Cancel Friend Request");
                                                progressDialog.dismiss();
                                                Snackbar.make(layout,"Request sent.",Snackbar.LENGTH_SHORT).show();
                                            }
                                        });



                                    }
                                });
                            } else{
                                Snackbar.make(layout,"An error has occurred.",Snackbar.LENGTH_SHORT).show();

                            }
                        }
                    });

                }

                    if(currentState.equals("Requested")) {
                        decline.setVisibility(View.INVISIBLE);
                        decline.setEnabled(false);
                        progressDialog.setTitle("Cancel");
                        progressDialog.setMessage("Canceling Friend Request...");
                        progressDialog.show();
                        friendRequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(userKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                friendRequest.child(userKey).
                                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                sendRequest.setEnabled(true);
                                                currentState = "Not Friends";
                                                sendRequest.setText("Send Friend Request");
                                                Snackbar.make(layout, "Friend Request Canceled.", Snackbar.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        });
                            }
                        });

                    }


                    if(currentState.equals("Friends")){
                        progressDialog.setMessage("UnFriending "+ name.getText().toString());
                        progressDialog.setTitle("UnFriend");
                        progressDialog.show();
                        sendRequest.setText("Send Friend Request");
                        friendsTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(userKey).removeValue();
                        friendsTable.child(userKey).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                        progressDialog.dismiss();
                        Snackbar.make(layout,"Goodbye " + name.getText().toString()+".",Snackbar.LENGTH_SHORT).show();
                        currentState="Not Friends";
                        decline.setVisibility(View.INVISIBLE);
                        decline.setEnabled(false);

                        FirebaseDatabase.getInstance().getReference()
                                .child("Chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(userKey)
                                .removeValue();

                        FirebaseDatabase.getInstance().getReference()
                                .child("Chat").child(userKey)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .removeValue();


                    }

                    if(currentState.equals("Request Received")){
                        decline.setVisibility(View.INVISIBLE);
                        decline.setEnabled(false);
                        progressDialog.setMessage("Accepting Friend Request...");
                        progressDialog.setTitle("Accept");
                        progressDialog.show();
                        final String currDate= DateFormat.getDateTimeInstance().format(new Date());
                        friendsTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(userKey).child("Date")
                                .setValue(currDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                friendsTable.child(userKey).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("Date")
                                        .setValue(currDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        friendRequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child(userKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                friendRequest.child(userKey).
                                                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @SuppressLint("SetTextI18n")
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                sendRequest.setEnabled(true);
                                                                currentState="Friends";
                                                                sendRequest.setText("UnFriend "+ name.getText().toString());
                                                                progressDialog.dismiss();
                                                                Snackbar.make(layout,"You are now friends.",Snackbar.LENGTH_SHORT).show();

                                                            }
                                                        });
                                            }
                                        });

                                    }
                                });
                            }
                        });

                    }
                }

        });

        decline.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(!currentState.equals("Friends"))
                {
                    progressDialog.setMessage("Declining");
                    progressDialog.show();
                    currentState = "Not Friends";
                    sendRequest.setText("Send Friend Request");
                    decline.setVisibility(View.INVISIBLE);
                    decline.setEnabled(false);
                    friendRequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(userKey).removeValue();
                    friendRequest.child(userKey).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                    progressDialog.dismiss();
                }

                else{
                    Intent goToTimeline=new Intent(getApplicationContext(),TimelineActivity.class);
                    goToTimeline.putExtra("from",userKey);
                    startActivity(goToTimeline);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mForOnline.child("Online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            mForOnline.child("Online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
