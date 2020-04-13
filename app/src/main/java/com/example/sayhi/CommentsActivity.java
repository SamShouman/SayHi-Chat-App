package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {
    private Toolbar myToolbar;
    private RecyclerView recView;
    private EditText comment;
    private ImageView submitComment;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private String currUser;
    private String postKey;
    private ArrayList<String> friendsKeys=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        myToolbar=findViewById(R.id.myToolbar);
        recView=findViewById(R.id.recView);
        comment=findViewById(R.id.comment);
        submitComment=findViewById(R.id.submitComment);

        mRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();

        currUser= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        postKey=getIntent().getStringExtra("PostKey");

        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadComments();

        submitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setComment();
            }
        });


    }

    private void setComment() {
        String commentText=comment.getText().toString();

        if(!commentText.trim().equals(""))
        {
            assert postKey != null;
            DatabaseReference mComments= mRef.child("Comments").child(postKey).push();

            mComments.child("From").setValue(currUser);
            mComments.child("Comment").setValue(commentText);
            mComments.child("Time").setValue(ServerValue.TIMESTAMP);

            comment.setText("");
        }
    }

    private void loadComments() {

        mRef.child("Friends").child(currUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d :dataSnapshot.getChildren())
                    friendsKeys.add(d.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        recView.setLayoutManager(manager);

        final Query q=mRef.child("Comments").child(postKey).orderByChild("Time");

        mRef.child("Comments").child(postKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                FirebaseRecyclerOptions<Comments> options=new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(q,Comments.class)
                        .build();

                FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter=new FirebaseRecyclerAdapter<Comments,CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final CommentsViewHolder holder, final int position, @NonNull final Comments model) {

                        holder.setComment(model.getComment());

                        long time=model.getTime();

                        String timeString=GetTimeAgo.getTimeAgo(time,getApplicationContext());
                        holder.setTime(timeString);

                        mRef.child("Users").child(model.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String imageURL= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                                String name= Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();

                                holder.setProfile(imageURL,getApplicationContext());
                                holder.setName(name);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        holder.v.findViewById(R.id.comment).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(currUser.equals(model.getFrom()))
                                {
                                    CharSequence[] deleteComment = {"Delete Comment"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CommentsActivity.this);
                                    builder.setItems(deleteComment, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ProgressDialog p = new ProgressDialog(CommentsActivity.this);
                                            p.setMessage("Deleting comment...");
                                            p.setCancelable(false);
                                            p.show();

                                            String commentKey = getRef(position).getKey();
                                            assert commentKey != null;
                                            mRef.child("Comments").child(postKey).child(commentKey).removeValue();
                                            p.dismiss();

                                        }
                                    });
                                    AlertDialog dialog = builder.create();

                                    dialog.show();
                                }
                            }

                        });

                        holder.v.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(friendsKeys.contains(model.getFrom())) {
                                    Intent goToTimeline = new Intent(getApplicationContext(), TimelineActivity.class);
                                    goToTimeline.putExtra("from", model.getFrom());
                                    startActivity(goToTimeline);
                                }
                                else{
                                    Intent goToUserProfile = new Intent(getApplicationContext(), UserProfileActivity.class);
                                    goToUserProfile.putExtra("UserID", model.getFrom());
                                    startActivity(goToUserProfile);
                                }

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View v= LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.comments_layout,parent,false);
                        return new CommentsViewHolder(v);
                    }
                };

                recView.setAdapter(adapter);
                adapter.startListening();




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



    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View v;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
        }

        public void setProfile(String imageURL, Context applicationContext) {
            CircleImageView profile=v.findViewById(R.id.profile);
            Picasso.with(applicationContext).load(imageURL).placeholder(R.drawable.usersayhii).into(profile);
        }

        public void setComment(String comment)
        {
            TextView messageChat=v.findViewById(R.id.comment);
            messageChat.setText(comment);
        }

        public void setName(String name)
        {
            TextView n=v.findViewById(R.id.name);
            n.setText(name);
        }

        public void setTime(String time)
        {
            TextView t=v.findViewById(R.id.time);
            t.setText(time);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(getIntent().getStringExtra("From").equals("Timeline"))
        {
            Intent goToTimeline=new Intent(getApplicationContext(),TimelineActivity.class);

            if(getIntent().hasExtra("User"));
                 goToTimeline.putExtra("from",getIntent().getStringExtra("User"));

            startActivity(goToTimeline);
            finishAffinity();
            finish();
        }

        else{
            Intent goToNewsFeed=new Intent(getApplicationContext(),NewsFeed.class);
            startActivity(goToNewsFeed);
            finishAffinity();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
