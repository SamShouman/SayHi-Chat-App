package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class TimelineActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private Toolbar myToolbar;
    private String user,currUser;
    private String userName;
    private CircleImageView profile;
    private TextView likes,friends,posts,status;
    private Intent i;
    private Button sendMessage,checkProfile;
    private RecyclerView allPosts;
    private ArrayList<String> keyPosition=new ArrayList<>();
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.left_to_right,R.anim.right_to_left);

        if(Build.VERSION.SDK_INT>20) {
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(false);
        }
        setContentView(R.layout.activity_timeline);
        Firebase.setAndroidContext(this);

        i=getIntent();
        mRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        currUser=mAuth.getCurrentUser().getUid();
        user = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        checkProfile=findViewById(R.id.checkProfile);
        sendMessage=findViewById(R.id.sendMessage);
        status=findViewById(R.id.status);
        allPosts=findViewById(R.id.allPosts);
        myToolbar=findViewById(R.id.myToolbar);
        profile=findViewById(R.id.profile);
        likes=findViewById(R.id.likes);
        friends=findViewById(R.id.friends);
        posts=findViewById(R.id.posts);
        mRef.keepSynced(true);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if(i.hasExtra("from")) {
            user = i.getStringExtra("from");

        }

        else
            {   user=currUser;

                checkProfile.setEnabled(false);
                checkProfile.setVisibility(View.INVISIBLE);

                sendMessage.setEnabled(false);
                sendMessage.setVisibility(View.INVISIBLE);
        }

            mRef.child("Users").child(user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userName = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                    if(!i.hasExtra("from"))
                    Objects.requireNonNull(getSupportActionBar()).setTitle("My Timeline");
                    else
                        Objects.requireNonNull(getSupportActionBar()).setTitle(userName);

                    final String image = Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                    Picasso.with(TimelineActivity.this).load(image).placeholder(R.drawable.usersayhii).into(profile);

                    String statusText= Objects.requireNonNull(dataSnapshot.child("Status").getValue()).toString();
                    status.setText(statusText);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            mRef.child("Friends").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final long totalFriends = dataSnapshot.getChildrenCount();
                    friends.setText(totalFriends + "");


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mRef.child("Posts").orderByChild("From").equalTo(user).addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long totalPosts = dataSnapshot.getChildrenCount();
                    posts.setText(totalPosts + "");
                    int totalLikes = 0;

                    for (DataSnapshot d : dataSnapshot.getChildren())
                        totalLikes += Integer.parseInt(Objects.requireNonNull(d.child("Likes").getValue()).toString());
                    likes.setText(totalLikes + "");



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            checkProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        Intent goToProfileActivity=new Intent(getApplicationContext(),UserProfileActivity.class);
                        goToProfileActivity.putExtra("UserID",i.getStringExtra("from"));
                        startActivity(goToProfileActivity);
                }
            });

            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent goToChatActivity=new Intent(getApplicationContext(),ChatActivity.class);
                    goToChatActivity.putExtra("UserID",i.getStringExtra("from"));
                    startActivity(goToChatActivity);
                }
            });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] download={"Save Image To Gallery"};
                AlertDialog.Builder builder= new AlertDialog.Builder(TimelineActivity.this);
                builder.setItems(download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                      //  ImageView postImage=holder.profile;
                        profile.setDrawingCacheEnabled(true);
                        Bitmap b = profile.getDrawingCache();
                        if(MediaStore.Images.Media.insertImage(Objects.requireNonNull(getApplicationContext()).getContentResolver(),
                                b,getSaltString(),"") != null);
                        Toast.makeText(getApplicationContext(),"Image saved to Gallery\\Camera.",Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });

        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToFriendsActivity=new Intent(getApplicationContext(),FriendsActivity.class);
                goToFriendsActivity.putExtra("UserID",user);
                startActivity(goToFriendsActivity);
            }
        });

        findViewById(R.id.friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToFriendsActivity=new Intent(getApplicationContext(),FriendsActivity.class);
                goToFriendsActivity.putExtra("UserID",user);
                startActivity(goToFriendsActivity);
            }
        });


        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        allPosts.setLayoutManager(manager);
        Query q=mRef.child("Posts").orderByChild("From").equalTo(user);
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(q,Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> adapter= new FirebaseRecyclerAdapter
                <Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, final int position, @NonNull final Posts model) {
                keyPosition.add(getRef(position).getKey());
                String caption = model.getCaption();
                final String image = model.getImage();
                int likes = model.getLikes();
                String time = model.getTime()+"";
                String name = model.getName();

                holder.setCaption(caption);
                holder.setImage(image, getApplicationContext());
                holder.setLikes(likes, currUser,getRef(position).getKey());
                holder.setTime(time);
                holder.setName(name);
                holder.setDislikes(model.getDislikes(), currUser,getRef(position).getKey());
                holder.setComments(getRef(position).getKey());

                FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(model.getFrom()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String imageURL= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                        holder.setProfile(imageURL,getApplicationContext());



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




                holder.v.findViewById(R.id.postLike).setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        final Firebase check=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+getRef(position)
                                .getKey());

                        check.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                                if(!dataSnapshot.hasChild(currUser))
                                {


                                    holder.v.findViewById(R.id.postLike).setBackground(getResources()
                                            .getDrawable(R.drawable.layout_rounded));




                                    final Firebase dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+getRef(position).getKey());

                                    dbDislikes.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                        @Override
                                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild(currUser)) {
                                                dbDislikes.removeValue();
                                                int dislikes= Integer.parseInt( holder.d.getText().toString().substring(0,1));
                                                if(dislikes > 0)
                                                    dislikes-=1;

                                                int likes=Integer.parseInt( holder.l.getText().toString().substring(0,1));
                                                likes+=1;

                                                holder.l.setText(likes+"");
                                                holder.d.setText(dislikes+"");

                                                Firebase dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+
                                                        getRef(position).getKey()+"/"+currUser);
                                                dbDislikes.removeValue();



                                                Firebase dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+
                                                        getRef(position).getKey()+"/"+currUser+"/Time");
                                                dbLikes.setValue(ServerValue.TIMESTAMP);

                                                dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+
                                                        getRef(position).getKey());

                                                Firebase users=new Firebase("https://sayhi-6935e.firebaseio.com/Users/"+currUser);
                                                users.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                                        String name=dataSnapshot.child("Name").getValue().toString();
                                                        String status=dataSnapshot.child("Status").getValue().toString();
                                                        String imageURL=dataSnapshot.child("Image").getValue().toString();

                                                        Firebase likes=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+getRef(position).getKey()+"/"+currUser);

                                                        likes.child("Name")
                                                                .setValue(name);
                                                        likes.child("Status")
                                                                .setValue(status);
                                                        likes.child("Image")
                                                                .setValue(imageURL);
                                                    }

                                                    @Override
                                                    public void onCancelled(FirebaseError firebaseError) {

                                                    }
                                                });

                                                dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+getRef(position)
                                                        .getKey());

                                                dbDislikes.child("Dislikes").setValue(dislikes);

                                                dbLikes.child("Likes").setValue(likes);
                                            }

                                            else{
                                                int likes=Integer.parseInt( holder.l.getText().toString().substring(0,1));
                                                likes+=1;

                                                holder.l.setText(likes+"");




                                                Firebase dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+
                                                        getRef(position).getKey()+"/"+currUser+"/Time");
                                                dbLikes.setValue(ServerValue.TIMESTAMP);


                                                Firebase users=new Firebase("https://sayhi-6935e.firebaseio.com/Users/"+currUser);
                                                users.addValueEventListener(new com.firebase.client.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                                        String name=dataSnapshot.child("Name").getValue().toString();
                                                        String status=dataSnapshot.child("Status").getValue().toString();
                                                        String imageURL=dataSnapshot.child("Image").getValue().toString();

                                                        Firebase likes=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+getRef(position).getKey()+"/"+currUser);

                                                        likes.child("Name")
                                                                .setValue(name);
                                                        likes.child("Status")
                                                                .setValue(status);
                                                        likes.child("Image")
                                                                .setValue(imageURL);
                                                    }

                                                    @Override
                                                    public void onCancelled(FirebaseError firebaseError) {

                                                    }
                                                });

                                                dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+getRef(position)
                                                        .getKey());

                                                dbLikes.child("Likes").setValue(likes);

                                            }



                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });


                                }

                                else{
                                    final Firebase check=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+
                                            getRef(position)
                                                    .getKey());

                                    check.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(currUser))
                                            {



                                                holder.v.findViewById(R.id.postLike).setBackgroundColor(Color.WHITE);


                                                int likes= Integer.parseInt( holder.d.getText().toString().substring(0,1));
                                                if(likes>0)
                                                    likes-=1;

                                                holder.d.setText(likes+"");

                                                Firebase dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+
                                                        getRef(position).getKey()+"/"+currUser);

                                                dbLikes.removeValue();


                                                dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+
                                                        getRef(position)
                                                                .getKey());

                                                dbLikes.child("Likes").setValue(likes);





                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });




                    }
                });

                holder.v.findViewById(R.id.postDislike).setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {

                        final Firebase check=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+getRef(position)
                                .getKey());

                        check.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.hasChild(currUser))
                                {

                                    holder.v.findViewById(R.id.postDislike).setBackground(getResources()
                                            .getDrawable(R.drawable.layout_rounded));

                                    Firebase dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+getRef(position).getKey());

                                    dbLikes.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                        @Override
                                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild(currUser))
                                            {
                                                int dislikes= Integer.parseInt(holder.d.getText().toString().substring(0,1));
                                                int likes=Integer.parseInt(holder.l.getText().toString().substring(0,1));

                                                if(likes > 0)
                                                    likes-=1;


                                                dislikes+=1;

                                                holder.l.setText(likes+"");
                                                holder.d.setText(dislikes+"");

                                                Firebase dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+
                                                        getRef(position).getKey()+"/"+currUser+"/Time");

                                                dbDislikes.setValue(ServerValue.TIMESTAMP);

                                                Firebase users=new Firebase("https://sayhi-6935e.firebaseio.com/Users/"+currUser);
                                                users.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                                        String name=dataSnapshot.child("Name").getValue().toString();
                                                        String status=dataSnapshot.child("Status").getValue().toString();
                                                        String imageURL=dataSnapshot.child("Image").getValue().toString();

                                                        Firebase likes=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+getRef(position).getKey()+"/"+currUser);
                                                        likes.child("Name")
                                                                .setValue(name);
                                                        likes.child("Status")
                                                                .setValue(status);
                                                        likes.child("Image")
                                                                .setValue(imageURL);
                                                    }

                                                    @Override
                                                    public void onCancelled(FirebaseError firebaseError) {

                                                    }
                                                });


                                                Firebase dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Likes/"+
                                                        getRef(position).getKey()+"/"+currUser);
                                                dbLikes.removeValue();

                                                dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+
                                                        getRef(position).getKey());

                                                dbLikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+getRef(position)
                                                        .getKey());

                                                dbDislikes.child("Dislikes").setValue(dislikes);

                                                dbLikes.child("Likes").setValue(likes);

                                            }

                                            else{
                                                int dislikes= Integer.parseInt(holder.d.getText().toString().substring(0,1));


                                                dislikes+=1;

                                                holder.d.setText(dislikes+"");

                                                Firebase dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+
                                                        getRef(position).getKey()+"/"+currUser+"/Time");

                                                dbDislikes.setValue(ServerValue.TIMESTAMP);

                                                Firebase users=new Firebase("https://sayhi-6935e.firebaseio.com/Users/"+currUser);
                                                users.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                                        String name=dataSnapshot.child("Name").getValue().toString();
                                                        String status=dataSnapshot.child("Status").getValue().toString();
                                                        String imageURL=dataSnapshot.child("Image").getValue().toString();

                                                        Firebase likes=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+getRef(position).getKey()+"/"+currUser);
                                                        likes.child("Name")
                                                                .setValue(name);
                                                        likes.child("Status")
                                                                .setValue(status);
                                                        likes.child("Image")
                                                                .setValue(imageURL);
                                                    }

                                                    @Override
                                                    public void onCancelled(FirebaseError firebaseError) {

                                                    }
                                                });




                                                dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+
                                                        getRef(position).getKey());


                                                dbDislikes.child("Dislikes").setValue(dislikes);


                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });


                                }

                                else{
                                    final Firebase check=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+
                                            getRef(position)
                                                    .getKey());

                                    check.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(currUser))
                                            {

                                                holder.v.findViewById(R.id.postDislike).setBackgroundColor(Color.WHITE);




                                                int dislikes= Integer.parseInt(holder.d.getText().toString().substring(0,1));
                                                if(dislikes>0)
                                                    dislikes-=1;

                                                holder.d.setText(dislikes+"");

                                                Firebase dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Dislikes/"+
                                                        getRef(position).getKey()+"/"+currUser);
                                                dbDislikes.removeValue();


                                                dbDislikes=new Firebase("https://sayhi-6935e.firebaseio.com/Posts/"+
                                                        getRef(position)
                                                                .getKey());

                                                dbDislikes.child("Dislikes").setValue(dislikes);


                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                    }
                });





                FirebaseDatabase.getInstance().getReference().child("Likes").child(Objects.requireNonNull(getRef(position)
                        .getKey())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild(currUser)){
                            holder.v.findViewById(R.id.postLike).setBackgroundColor(Color.WHITE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                FirebaseDatabase.getInstance().getReference().child("Dislikes").child(Objects.requireNonNull(getRef(position)
                        .getKey())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild(currUser)){
                            holder.v.findViewById(R.id.postDislike).setBackgroundColor(Color.WHITE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




                holder.v.findViewById(R.id.numberOfDislikes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent seeDislikes=new Intent(getApplicationContext(),LikesDislikesActivity.class);
                        seeDislikes.putExtra("key",getRef(position).getKey());
                        seeDislikes.putExtra("type","Dislikes");
                        seeDislikes.putExtra("where","Timeline");
                        seeDislikes.putExtra("from",model.getFrom());
                        startActivity(seeDislikes);
                    }
                });

                holder.v.findViewById(R.id.numberOfLikes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent seeLikes=new Intent(getApplicationContext(),LikesDislikesActivity.class);
                        seeLikes.putExtra("key",getRef(position).getKey());
                        seeLikes.putExtra("type","Likes");
                        seeLikes.putExtra("where","Timeline");
                        seeLikes.putExtra("from",model.getFrom());
                        startActivity(seeLikes);
                    }
                });

                holder.v.findViewById(R.id.postImage).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence[] download={"Save Image To Gallery"};
                        AlertDialog.Builder builder= new AlertDialog.Builder(TimelineActivity.this);
                        builder.setItems(download, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ImageView postImage=holder.v.findViewById(R.id.postImage);
                                postImage.setDrawingCacheEnabled(true);
                                Bitmap b = postImage.getDrawingCache();
                                if(MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),b,getSaltString(),"")!=null);
                                Toast.makeText(getApplicationContext(),"Image saved in Gallery\\Camera.",Toast.LENGTH_SHORT).show();
                            }
                        });

                        AlertDialog dialog=builder.create();
                        dialog.show();
                    }
                });

                holder.v.findViewById(R.id.commentPost).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent goToCommentsActivity=new Intent(getApplicationContext(),CommentsActivity.class);
                        goToCommentsActivity.putExtra("PostKey",getRef(position).getKey());
                        goToCommentsActivity.putExtra("From","Timeline");
                        goToCommentsActivity.putExtra("User",user);
                        startActivity(goToCommentsActivity);
                    }
                });

                holder.v.findViewById(R.id.numberOfComments).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent goToCommentsActivity=new Intent(getApplicationContext(),CommentsActivity.class);
                        goToCommentsActivity.putExtra("PostKey",getRef(position).getKey());
                        goToCommentsActivity.putExtra("From","NewsFeed");
                        startActivity(goToCommentsActivity);
                    }
                });






            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_structure, parent, false);

                return new PostsViewHolder(view);
            }
        };


        allPosts.setAdapter(adapter);
        adapter.startListening();

        if(user.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            ItemTouchHelper.SimpleCallback touch = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

                    final String key=keyPosition.get((keyPosition.size()-1)-viewHolder.getAdapterPosition());
                    AlertDialog.Builder builder=new AlertDialog.Builder(TimelineActivity.this);
                    Log.i("WhichKey",key);


                    builder.setTitle("Delete Post")
                            .setMessage("Are you sure you want to delete this post?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    final ProgressDialog progressDialog=new ProgressDialog(TimelineActivity.this);
                                    progressDialog.setMessage("Deleting post...");
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();
                                    Firebase posts=new Firebase("https://sayhi-6935e.firebaseio.com/Posts");
                                    posts.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                                        @Override
                                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                            String imageURL= Objects.requireNonNull(dataSnapshot.child(key).child("Image").getValue()).toString();
                                            StorageReference imagePath= FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
                                            imagePath.delete();
                                            Firebase comments=new Firebase("https://sayhi-6935e.firebaseio.com/Comments");
                                            comments.child(key).removeValue();
                                            Firebase ref=new Firebase("https://sayhi-6935e.firebaseio.com/");
                                            LinearLayout layout=findViewById(R.id.layout);
                                            ref.child("Posts").child(key).removeValue();
                                            Snackbar.make(layout,"Post has been deleted.",Snackbar.LENGTH_LONG).show();
                                            progressDialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });



                                                }
                                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent restart=getIntent();
                            finish();
                            startActivity(restart);

                        }
                    });

                    final AlertDialog alert=builder.create();

                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                        }
                    });
                    try {
                        alert.show();
                        int dividerId = alert.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
                        View divider = alert.findViewById(dividerId);
                        divider.setBackgroundColor(Objects.requireNonNull(getApplicationContext()).getResources().getColor(R.color.colorAccent));

                        int textViewId = alert.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                        TextView tv = alert.findViewById(textViewId);
                        tv.setTextColor(getApplicationContext().getResources().getColor(R.color.colorAccent));
                    }catch(Exception e){
                        e.printStackTrace();

                    }






                }
            };

            new ItemTouchHelper(touch).attachToRecyclerView(allPosts);
        }




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logOut) {

            FirebaseAuth.getInstance().signOut();
                Firebase mRef=new Firebase("https://sayhi-6935e.firebaseio.com/Users");
                mRef.child(Objects.requireNonNull(currUser))
                        .child("Online").setValue(ServerValue.TIMESTAMP);
                mRef.child(currUser).child("Device Token").setValue("none");
                Intent goToStartActivity = new Intent(getApplicationContext(), StartActivity.class);
                goToStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToStartActivity);
            }


        else{
            if(item.getItemId()==R.id.settings) {
                Intent goToSettingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(goToSettingsActivity);
            }

            if(item.getItemId()==R.id.users){
                Intent goToUsersActivity = new Intent(getApplicationContext(), UsersActivity.class);
                startActivity(goToUsersActivity);
            }

            else{

                if(item.getItemId()==R.id.starMessages)
                {
                    Intent goToStarMessagesActivity = new Intent(getApplicationContext(), StarMessagesActivity.class);
                    startActivity(goToStarMessagesActivity);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null)
        {
            mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mRef.child("Online").setValue("true");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuth.getCurrentUser()!=null)
        {
            mRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

            mRef.child("Online").setValue(ServerValue.TIMESTAMP);
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goToNewsFeed=new Intent(getApplicationContext(),NewsFeed.class);
        goToNewsFeed.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToNewsFeed);
    }


    public class PostsViewHolder extends RecyclerView.ViewHolder{
        View v;
        TextView d,l;
        PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            v=itemView;
            d=v.findViewById(R.id.numberOfDislikes);
            l=v.findViewById(R.id.numberOfLikes);

        }

        void setTime(String time) {
            TextView t=v.findViewById(R.id.postTime);

            String timePosted = GetTimeAgo.getTimeAgo(Long.parseLong(time), getApplicationContext());
            t.setText(timePosted);
        }

        @SuppressLint("SetTextI18n")
        void setLikes(int likes, final String currUser,String postKey) {
            FirebaseDatabase.getInstance().getReference().child("Likes")
                    .child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(currUser)){
                        v.findViewById(R.id.postLike).setBackground(getResources()
                                .getDrawable(R.drawable.layout_rounded));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            l.setText(likes+" Likes ");
        }

        public void setName(String userName) {

            TextView n=v.findViewById(R.id.postUserName);
            n.setText(userName);
        }

        void setCaption(String caption) {
            TextView c=v.findViewById(R.id.postCaption);
            if(!caption.equals("none"))
                c.setText(caption);
            else
                c.setText("");
        }

        void setImage(String image, Context c) {
            ImageView i=v.findViewById(R.id.postImage);
            Picasso.with(c).load(image).into(i);
        }

        @SuppressLint("SetTextI18n")
        public void setDislikes(int dislikes,final String currUser,String postKey) {
            FirebaseDatabase.getInstance().getReference().child("Dislikes")
                    .child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(currUser)){
                        v.findViewById(R.id.postDislike).setBackground(getResources()
                                .getDrawable(R.drawable.layout_rounded));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            d.setText(dislikes+" Dislikes ");
        }

        public void setProfile(String imageURL, Context c) {
            CircleImageView profile=v.findViewById(R.id.profile);
            Picasso.with(c).load(imageURL).placeholder(R.drawable.usersayhii).into(profile);
        }

        public void setStatus(String status) {
            TextView s=findViewById(R.id.status);
            s.setText(status);
        }

        public void setComments(final String key) {
          //  DatabaseReference mComments= mRef.child("Comments").child(key).getRef();
            Firebase mComments=new Firebase("https://sayhi-6935e.firebaseio.com/Comments");
            final TextView c=v.findViewById(R.id.numberOfComments);

            mComments.addValueEventListener(new com.firebase.client.ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                    long totalComments=dataSnapshot.child(key).getChildrenCount();
                    c.setText(totalComments + " Comments");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }
    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 20) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();

    }


}
