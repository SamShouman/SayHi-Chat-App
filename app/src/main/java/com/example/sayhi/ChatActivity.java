package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity {
    private String receiver;
    private DatabaseReference mRef;
    private String receiverName;
    private TextView lastSeen;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private String currUser;
    private EditText messageToSend;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessagesAdapter adapter;
    private StorageReference storageReference;
    private ConstraintLayout parent;
    private boolean restarted=false;
    private static final int ID=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAuth = FirebaseAuth.getInstance();
        currUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        receiver = getIntent().getStringExtra("UserID");
        storageReference= FirebaseStorage.getInstance().getReference();
        final RecyclerView chatRecView = findViewById(R.id.chatRecView);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setStackFromEnd(true);
        chatRecView.setLayoutManager(layout);


        adapter = new MessagesAdapter(messagesList, getApplicationContext());
        chatRecView.setAdapter(adapter);
        parent=findViewById(R.id.parent);

        Toolbar myToolbar = findViewById(R.id.appBarLayout);


        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton send = findViewById(R.id.send);
        ImageButton add = findViewById(R.id.add);
        messageToSend = findViewById(R.id.messageToSend);


        mRef = FirebaseDatabase.getInstance().getReference();
        final String[] wallpaper = new String[1];



        mRef.child("Users").child(currUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wallpaper[0] = Objects.requireNonNull(dataSnapshot.child("Wallpaper").getValue()).toString();
                if (!wallpaper[0].equals("default")) {
                    final ImageView img = new ImageView(getApplicationContext());
                    Picasso.with(img.getContext()).load(wallpaper[0]).into(img, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            parent.setBackgroundDrawable(img.getDrawable());
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(ChatActivity.this,
                                    "An error has occurred while loading the wallpaper," +
                                    " please restart the application!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });










        loadMessages();


        mRef.child("Users").child(receiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                receiverName = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                messageToSend.setHint("Send to " + receiverName);
                Objects.requireNonNull(getSupportActionBar()).setTitle(receiverName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") View action_bar_view = inflater.inflate(R.layout.chat_bar_layout, null);
        actionBar.setCustomView(action_bar_view);


        TextView name = action_bar_view.findViewById(R.id.chatName);
        lastSeen = action_bar_view.findViewById(R.id.lastSeen);
        profileImage = action_bar_view.findViewById(R.id.chatProfile);
        name.setText(receiverName);

        name.setVisibility(View.INVISIBLE);
        mRef.child("Users").child(receiver).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ifOnline = Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString();
                String profile = Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                Picasso.with(getApplicationContext()).load(profile).placeholder(R.drawable.usersayhii).into(profileImage);
                if (ifOnline.equals("true")) {
                    lastSeen.setText("Online");


                } else {
                   // GetTimeAgo ago = new GetTimeAgo();
                    long lastTime = Long.parseLong(ifOnline);
                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    lastSeen.setText(lastSeenTime);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), 1);
            }
        });



        ItemTouchHelper.SimpleCallback touch = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                    final DatabaseReference mStar=FirebaseDatabase.getInstance().getReference()
                            .child("Star Messages");


                final String[] messageKey = new String[1];
                mRef.child("Messages").child(currUser).child(receiver)

                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int i=0;
                        for(DataSnapshot d: dataSnapshot.getChildren())
                        {
                            if(i==viewHolder.getAdapterPosition())
                            {
                                messageKey[0]=d.getKey();


                                mStar.child(currUser)
                                        .child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.hasChild(messageKey[0]))
                                        {
                                            mRef.child("Messages").child(currUser).child(receiver)
                                                    .child(messageKey[0]).child("Star").setValue("true");
                                            mRef.child("Messages").child(currUser).child(receiver)
                                                    .child(messageKey[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String message= Objects.requireNonNull(dataSnapshot.child("Message").getValue()).toString();
                                                    mStar.child(currUser).child(receiver).child(messageKey[0])
                                                            .child("Message").setValue(message);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                            mStar.child(currUser).child(receiver).child(messageKey[0])
                                                    .child("Time").setValue(ServerValue.TIMESTAMP);

                                            Intent restart=getIntent();
                                            finish();
                                            startActivity(restart);


                                        }

                                        else {

                                                mRef.child("Messages").child(currUser)
                                                        .child(receiver).child(messageKey[0])
                                                        .child("Star").setValue("false");

                                                mStar.child(currUser).child(receiver)
                                                        .child(messageKey[0]).removeValue();

                                                Intent restart=getIntent();
                                                finish();
                                                startActivity(restart);

                                             }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                            i++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        };

        new ItemTouchHelper(touch).attachToRecyclerView(chatRecView);





    }

    private void loadMessages() {
       final String[] lastMessageKey2=new String[1];
        Query q= mRef.child("Messages").child(currUser).child(receiver).orderByKey().limitToLast(1);

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0) {
                    for (DataSnapshot d : dataSnapshot.getChildren())
                        lastMessageKey2[0] = d.getKey();

                    mRef.child("Messages").child(currUser).child(receiver)
                            .child(lastMessageKey2[0])
                            .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()) {

                                if (!Objects.requireNonNull(dataSnapshot.child("From").getValue()).toString().equals(currUser))
                                    mRef.child("Messages").child(currUser).child(receiver)
                                            .child(lastMessageKey2[0]).child("Seen")
                                            .setValue("true");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRef.child("Messages").child(currUser).child(receiver).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot!=null)
                {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    adapter.notifyDataSetChanged();



                }

                if(dataSnapshot.hasChildren()) {
                    final String[] lastMessageKey = new String[1];
                    Query q = mRef.child("Messages").child(currUser).child(receiver).orderByKey().limitToLast(1);
                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren())
                                lastMessageKey[0] = d.getKey();

                            mRef.child("Messages").child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(currUser)) {

                                        mRef.child("Messages").child(receiver).child(currUser).child(lastMessageKey[0])
                                                .child("Seen").setValue("false");

                                        

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            mRef.child("Messages").child(currUser).child(receiver)
                                    .child(lastMessageKey[0]).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren())
                                        if (Objects.requireNonNull(dataSnapshot.child("From").getValue()).toString().equals(currUser))
                                            mRef.child("Messages").child(currUser).child(receiver).child(lastMessageKey[0])
                                                    .child("Seen").setValue("true");


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });







    }






    private void sendMessage() {
        String message = messageToSend.getText().toString().trim();
        String currUser_chat = "Messages/" + currUser + "/" + receiver;
        String receiver_chat = "Messages/" + receiver + "/" + currUser;
        DatabaseReference userMessageID = FirebaseDatabase.getInstance().getReference()
                .child(currUser)
                .child(receiver)
                .push();
        final String messageKey = userMessageID.getKey();
        if (!message.equals("")) {

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("Message", message.trim());
            messageMap.put("Seen", "false");
            messageMap.put("Type", "text");
            messageMap.put("Time", ServerValue.TIMESTAMP);
            messageMap.put("From", currUser);
            messageMap.put("Star","false");



            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(currUser_chat + "/" + messageKey, messageMap);
            messageUserMap.put(receiver_chat + "/" + messageKey, messageMap);

            mRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                }
            });

            addToChat();

            mRef.child("Chat").child(receiver).child(currUser).child("TimeStamp").setValue(ServerValue.TIMESTAMP);
            mRef.child("Chat").child(currUser).child(receiver).child("TimeStamp").setValue(ServerValue.TIMESTAMP);

            DatabaseReference notificationKey= mRef.child("Notifications").child(receiver).push();
            String note=notificationKey.getKey();

            mRef.child("Notifications").child(receiver).child(note).child("from").setValue(currUser);
            mRef.child("Notifications").child(receiver).child(note).child("type").setValue("message");

            messageToSend.setText("");


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            assert data != null;
            Uri image = data.getData();
            final String currUserChat = "Messages/" + currUser + "/" + receiver;
            final String receiverChat = "Messages/" + receiver + "/" + currUser;

            DatabaseReference reference = mRef.child("Messages").child(currUser).child(receiver).push();
            final String messageID = reference.getKey();

            final StorageReference filePath = storageReference.child("messages_images").child(messageID + ".jpg");

            assert image != null;
            filePath.putFile(image).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful())
                        return filePath.getDownloadUrl();
                    throw Objects.requireNonNull(task.getException());

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {




                            Map<String, Object> messageMap = new HashMap<>();
                            messageMap.put("Message", Objects.requireNonNull(task.getResult()).toString());
                            messageMap.put("Seen", "false");
                            messageMap.put("Type", "image");
                            messageMap.put("Time", ServerValue.TIMESTAMP);
                            messageMap.put("From", currUser);
                            messageMap.put("Star","false");


                            Map<String, Object> messageUserMap = new HashMap<>();
                            messageUserMap.put(currUserChat + "/" + messageID, messageMap);
                            messageUserMap.put(receiverChat + "/" + messageID, messageMap);

                        mRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            }
                        });

                    } else {

                        Toast.makeText(getApplicationContext(), "An error has occurred. Please try again!", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        if(requestCode==2 && resultCode==RESULT_OK) {
            assert data != null;
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .start(ChatActivity.this);
        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    assert result != null;
                    Uri resultUri = result.getUri();
                    final String currUserID= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    final StorageReference filePath=storageReference.child("wallpaper_images").child(currUserID+".jpg");

                    final ProgressDialog progressDialog=new ProgressDialog(ChatActivity.this);
                    progressDialog.setMessage("Changing wallpaper...");
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
                                mRef.child("Users").child(currUserID).child("Wallpaper").setValue(Objects
                                        .requireNonNull(task.getResult()).toString());
                                Toast.makeText(getApplicationContext(),"Wallpaper changed successfully.",Toast.LENGTH_SHORT).show();

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








    public void addToChat(){


        mRef.child("Chat").child(currUser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              //  if (!dataSnapshot.hasChild(receiver)) {
                    Map<String, Object> chatAddMap = new HashMap<>();
                    chatAddMap.put("Seen", "false");
                    chatAddMap.put("TimeStamp", ServerValue.TIMESTAMP);


                    Map<String, Object> chatUserMap = new HashMap<>();
                    chatUserMap.put("Chat/" + currUser + "/" + receiver, chatAddMap);
                    chatUserMap.put("Chat/" + receiver + "/" + currUser, chatAddMap);

                    mRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        }
                    });
                //}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

          }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wallpaper_menu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.updateWallpaper){
            Intent galleryIntent=new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent,"Please Choose a New Wallpaper"),ID);
        }

        if(item.getItemId()==R.id.removeWallpaper){
            mRef.child("Users").child(currUser)
                    .child("Wallpaper")
                    .setValue("default")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){

                        Toast.makeText(getApplicationContext(),"Could not remove the wallpaper, please try again!",Toast.LENGTH_SHORT).show();

                    }

                    else{
                        Intent restart=getIntent();
                        restarted=true;
                        finish();
                        startActivity(restart);
                    }


                }
            });

        }




        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("Users").child(currUser).child("Online").setValue("true");
        mRef.child("Users").child(currUser).child("InChatActivity").setValue(receiver);


    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            mRef.child("Online").setValue(ServerValue.TIMESTAMP);
        }

        mRef.child("Users").child(currUser).child("InChatActivity").setValue("false");

    }

    @Override
    protected void onPause() {
        super.onPause();
        mRef.child("Users").child(currUser).child("InChatActivity").setValue("false");


    }


}





