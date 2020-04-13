package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsFeed extends AppCompatActivity {
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    public String currUser;
    private Activity c;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        overridePendingTransition(R.anim.left_to_right,R.anim.right_to_left);
        setContentView(R.layout.activity_news_feed);
        ProgressDialog p=new ProgressDialog(NewsFeed.this);
        p.setCancelable(false);
        p.setMessage("Preparing news feed...");
        p.show();
        c=this;
        Firebase.setAndroidContext(getApplicationContext());
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("News Feed");

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currUser= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        final RecyclerView posts = findViewById(R.id.allPosts);

        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        mRef.keepSynced(true);

        posts.setLayoutManager(manager);

        final ArrayList<String> friendsList=new ArrayList<>();

        final DatabaseReference friends=FirebaseDatabase.getInstance().getReference().child("Friends").child(currUser);
        friends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {   posts.scrollToPosition( (int) dataSnapshot.getChildrenCount());
                    for (DataSnapshot d : dataSnapshot.getChildren())
                    {
                        final String userKey = d.getKey();
                        friendsList.add(userKey);


                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final Query q=mRef.child("Posts").orderByChild("Time");

        FirebaseRecyclerOptions<Posts> options= new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(q,Posts.class)
                .build();

        final FirebaseRecyclerAdapter<Posts,PostsViewHolder> adapter= new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, final int position, @NonNull final Posts model) {



                if(friendsList.size()>0) {

                    if (friendsList.contains(model.getFrom()))
                    {
                        String caption = model.getCaption();
                        String image = model.getImage();
                        int likes = model.getLikes();
                        String time = model.getTime()+"";
                        String name = model.getName();

                        holder.setCaption(caption);
                        holder.setImage(image, getApplicationContext());
                        holder.setLikes(likes,currUser,getRef(position).getKey());
                        holder.setTime(time);
                        holder.setName(name);
                        holder.setDislikes(model.getDislikes(),currUser,getRef(position).getKey());

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
                    }
                    else {

                      //  getRef(position).removeValue();
                        holder.v.setVisibility(View.GONE);
                        holder.v.setEnabled(false);
                        holder.v.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                    }


                }

                else
                    {
                        holder.v.setVisibility(View.GONE);
                        holder.v.setEnabled(false);
                        holder.v.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                }

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


                                    //checkIfInDislike(getRef(position).getKey());

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
                                                users.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener()
                                                {
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

                                else
                                    {  //remove dislike

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

                holder.v.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(Build.VERSION.SDK_INT>20)
                        {
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(c,
                                    holder.v.findViewById(R.id.profile),
                                    "profile");
                            Intent goToTimeline = new Intent(getApplicationContext(), TimelineActivity.class);
                            goToTimeline.putExtra("from", model.getFrom());
                            startActivity(goToTimeline, options.toBundle());

                        }

                        else{
                            Intent goToTimeline = new Intent(getApplicationContext(), TimelineActivity.class);
                            goToTimeline.putExtra("from", model.getFrom());
                            startActivity(goToTimeline);
                        }
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
                        seeDislikes.putExtra("where","NewsFeed");
                        startActivity(seeDislikes);
                    }
                });

                holder.v.findViewById(R.id.numberOfLikes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent seeLikes=new Intent(getApplicationContext(),LikesDislikesActivity.class);
                        seeLikes.putExtra("key",getRef(position).getKey());
                        seeLikes.putExtra("type","Likes");
                        seeLikes.putExtra("where","NewsFeed");
                        startActivity(seeLikes);
                    }
                });

                holder.v.findViewById(R.id.commentPost).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent goToCommentsActivity=new Intent(getApplicationContext(),CommentsActivity.class);
                        goToCommentsActivity.putExtra("PostKey",getRef(position).getKey());
                        goToCommentsActivity.putExtra("From","NewsFeed");
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



            holder.v.findViewById(R.id.postImage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharSequence[] download={"Save Image To Gallery"};
                    AlertDialog.Builder builder= new AlertDialog.Builder(NewsFeed.this);
                    builder.setItems(download, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ImageView postImage=holder.v.findViewById(R.id.postImage);
                            postImage.setDrawingCacheEnabled(true);
                            Bitmap b = postImage.getDrawingCache();
                           if(MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),b,getSaltString(),"")!=null);
                            Toast.makeText(getApplicationContext(),"Image saved to Gallery\\Camera.",Toast.LENGTH_SHORT).show();
                        }
                    });

                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
            });

            holder.setComments(getRef(position).getKey());

            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_structure, parent, false);

                return new PostsViewHolder(view);
            }
        };

        posts.setAdapter(adapter);
        adapter.startListening();


        p.dismiss();




    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.new_post,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.newPost) {
            Intent createNewPost = new Intent(getApplicationContext(), CreatePostActivity.class);
            startActivity(createNewPost);
        }

       if(item.getItemId()==R.id.myTimeline) {
            Intent goToTimeline=new Intent(getApplicationContext(), TimelineActivity.class);
            startActivity(goToTimeline);
        }

        if(item.getItemId()==R.id.home){
            Intent goToMain=new Intent(getApplicationContext(), MainActivity.class);
            goToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToMain);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth.getCurrentUser() != null)
        {
            mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mRef.child("Online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRef= FirebaseDatabase.getInstance().getReference().child("Users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        mRef.child("Online").setValue("true");
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

            d.setText(dislikes+" Dislikes");
        }

        public void setComments(final String key) {

            final TextView c=v.findViewById(R.id.numberOfComments);

            Firebase mComments=new Firebase("https://sayhi-6935e.firebaseio.com/Comments");


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

        public void setProfile(String imageURL, Context c) {
            CircleImageView profile=v.findViewById(R.id.profile);
            Picasso.with(c).load(imageURL).placeholder(R.drawable.usersayhii).into(profile);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent main=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(main);
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


