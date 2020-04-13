package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.Objects;

public class LikesDislikesActivity extends AppCompatActivity {
    private RecyclerView recView;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private Toolbar myToolbar;
    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes_dislikes);

        myToolbar=findViewById(R.id.myToolbar);
        recView=findViewById(R.id.recView);
        mRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();

        setSupportActionBar(myToolbar);

         i=getIntent();

        if(Objects.equals(i.getStringExtra("type"), "Likes"))
            Objects.requireNonNull(getSupportActionBar()).setTitle("Likes");

        else
            Objects.requireNonNull(getSupportActionBar()).setTitle("Dislikes");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayUsers(i.getStringExtra("type"),i.getStringExtra("key"));
    }

    private void displayUsers(final String type, final String key) {

        LinearLayoutManager manager= new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        recView.setLayoutManager(manager);

        FirebaseRecyclerOptions<Users> options=new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mRef.child(type).child(key).orderByChild("Time"),Users.class).build();

        final FirebaseRecyclerAdapter<Users, UsersActivity.UsersViewHolder> adapter=new FirebaseRecyclerAdapter<Users, UsersActivity.UsersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final UsersActivity.UsersViewHolder holder, final int position, @NonNull final Users model) {


                            holder.setUserName(model.getName());
                            holder.setUserStatus(model.getStatus());
                            holder.setUserImage(model.getImage(), getApplicationContext());

                            final String userID = getRef(position).getKey();

                            holder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent goToUserProfileActivity = new Intent(LikesDislikesActivity.this, UserProfileActivity.class);
                                    goToUserProfileActivity.putExtra("UserID", userID);
                                    startActivity(goToUserProfileActivity);

                                }
                            });





            }

            @NonNull
            @Override
            public UsersActivity.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_layout, parent, false);

                return new UsersActivity.UsersViewHolder(view);
            }
        };

        recView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(i.getStringExtra("where").equals("NewsFeed"))
        {
            Intent goToNewsFeed=new Intent(getApplicationContext(),NewsFeed.class);
            goToNewsFeed.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToNewsFeed);
        }

        if(i.getStringExtra("where").equals("Timeline"))
            {
            Intent goToTimeline=new Intent(getApplicationContext(), TimelineActivity.class);
           // goToTimeline.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.i("nkjnkje",i.getStringExtra("from"));
            goToTimeline.putExtra("from",getIntent().getStringExtra("from"));
            startActivity(goToTimeline);
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
}
