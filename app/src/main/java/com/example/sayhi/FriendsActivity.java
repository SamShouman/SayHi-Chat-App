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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class FriendsActivity extends AppCompatActivity {
    Toolbar myToolbar;
    RecyclerView recView;
    DatabaseReference mRef;
    FirebaseAuth mAuth;
    String currUser,user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        myToolbar=findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recView=findViewById(R.id.recView);

        mAuth=FirebaseAuth.getInstance();
        mRef= FirebaseDatabase.getInstance().getReference();

        currUser= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        user=getIntent().getStringExtra("UserID");

        mRef.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String[] name= Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString().split(" ");
                String userFName=name[0];
                Objects.requireNonNull(getSupportActionBar()).setTitle(userFName+"'s Friends");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        showFriends();



    }

    private void showFriends() {
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        recView.setLayoutManager(manager);

        final Query q= mRef.child("Friends").child(user);

        final ArrayList<String> friendsKeys=new ArrayList<>();
        final ArrayList<String> friendsNames=new ArrayList<>();
        final ArrayList<String> friendsStatus=new ArrayList<>();
        final ArrayList<String> friendsImages=new ArrayList<>();

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(final DataSnapshot d: dataSnapshot.getChildren()) {
                    friendsKeys.add(d.getKey());

                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(Objects.requireNonNull(d.getKey()))
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String name = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                                    String status = Objects.requireNonNull(dataSnapshot.child("Status").getValue()).toString();
                                    String imageURL= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                                    friendsNames.add(name);
                                    friendsStatus.add(status);
                                    friendsImages.add(imageURL);


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

                FirebaseRecyclerOptions<Users> options= new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(q,Users.class)
                        .build();

                FirebaseRecyclerAdapter<Users, UsersActivity.UsersViewHolder> adapter=new FirebaseRecyclerAdapter<Users, UsersActivity.UsersViewHolder>
                        (options) {
                    @Override
                    protected void onBindViewHolder(@NonNull UsersActivity.UsersViewHolder holder, int position, @NonNull Users model) {
                        Log.i("fdfdfdf",friendsNames+"");
                        Log.i("fdfdfdf",friendsStatus+"");
                        holder.setUserName(friendsNames.get(position));
                        holder.setUserStatus(friendsStatus.get(position));
                        holder.setUserImage(friendsImages.get(position), getApplicationContext());

                        final String userID = getRef(position).getKey();

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent goToUserProfileActivity = new Intent(FriendsActivity.this, UserProfileActivity.class);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
        Intent goToTimeline=new Intent(getApplicationContext(),TimelineActivity.class);
        goToTimeline.putExtra("from",user);
        startActivity(goToTimeline);
        return super.onOptionsItemSelected(item);
    }
}
