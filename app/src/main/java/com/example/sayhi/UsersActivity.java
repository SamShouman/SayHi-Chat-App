package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private DatabaseReference mRef;
    private RecyclerView recView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        recView = findViewById(R.id.recView);
        mRef= FirebaseDatabase.getInstance().getReference().child("Users");
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Users> users=new FirebaseRecyclerOptions.Builder<Users>().setQuery(mRef,Users.class).build();
        FirebaseRecyclerAdapter<Users,UsersViewHolder> adapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(users) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull Users model) {

                    holder.setUserName(model.getName());
                    holder.setUserStatus(model.getStatus());
                    holder.setUserImage(model.getImage(), getApplicationContext());

                    final String userID = getRef(position).getKey();

                    holder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent goToUserProfileActivity = new Intent(UsersActivity.this, UserProfileActivity.class);
                            goToUserProfileActivity.putExtra("UserID", userID);
                            startActivity(goToUserProfileActivity);

                        }
                    });


                }

                @NonNull
                @Override
                public UsersViewHolder onCreateViewHolder (@NonNull ViewGroup parent,int viewType){
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.user_layout, parent, false);

                    return new UsersViewHolder(view);
                }
             };


        recView.setAdapter(adapter);
        adapter.startListening();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Search For...");
        searchView.setOnQueryTextListener(this);

        searchView.animate();
        searchView.setIconifiedByDefault(true);
        //to change search view hint color
        int id =  searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = searchView.findViewById(id);
        textView.setHintTextColor(Color.parseColor("#FFFFFF"));
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        mRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("Online").setValue("true");

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!newText.equals(""))
            firebaseSearch(newText);

        else
            {

                FirebaseRecyclerOptions<Users> users=new FirebaseRecyclerOptions.Builder<Users>().setQuery(mRef,Users.class).build();
                FirebaseRecyclerAdapter<Users,UsersViewHolder> adapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(users) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull Users model) {

                        holder.setUserName(model.getName());
                        holder.setUserStatus(model.getStatus());
                        holder.setUserImage(model.getImage(), getApplicationContext());

                        final String userID = getRef(position).getKey();

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent goToUserProfileActivity = new Intent(UsersActivity.this, UserProfileActivity.class);
                                goToUserProfileActivity.putExtra("UserID", userID);
                                startActivity(goToUserProfileActivity);

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public UsersViewHolder onCreateViewHolder (@NonNull ViewGroup parent,int viewType){
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.user_layout, parent, false);

                        return new UsersViewHolder(view);
                    }
                };


                recView.setAdapter(adapter);
                adapter.startListening();

        }
        return  false;
    }

    private void firebaseSearch(String newText) {

        Query allUsers = mRef
                .orderByChild("Name")
                .startAt(newText)
                .endAt("\uf0ff");

        FirebaseRecyclerOptions<Users> users=new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(allUsers,Users.class)
                .build();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> adapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(users) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull Users model) {

                holder.setUserName(model.getName());
                holder.setUserStatus(model.getStatus());
                holder.setUserImage(model.getImage(), getApplicationContext());

                final String userID = getRef(position).getKey();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent goToUserProfileActivity = new Intent(UsersActivity.this, UserProfileActivity.class);
                        goToUserProfileActivity.putExtra("UserID", userID);
                        startActivity(goToUserProfileActivity);

                    }
                });


            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder (@NonNull ViewGroup parent,int viewType){
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_layout, parent, false);

                return new UsersViewHolder(view);
            }
        };


        recView.setAdapter(adapter);
        adapter.startListening();


    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View view;
        CircleImageView userImageView;
        UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            view=itemView;
            userImageView=view.findViewById(R.id.profile);
        }

        void setUserName(String name){
            TextView userNameView=view.findViewById(R.id.name);
            userNameView.setText(name);

        }

        void setUserStatus(String status){
            TextView userStatusView=view.findViewById(R.id.status);
            userStatusView.setText(status);

        }

        void setUserImage(String image, Context c){
             userImageView=view.findViewById(R.id.profile);

            Picasso.with(c).load(image).placeholder(R.drawable.usersayhii).into(userImageView);

        }
    }
}
