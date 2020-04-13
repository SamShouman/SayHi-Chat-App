package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = findViewById(R.id.viewPager);
        mAuth=FirebaseAuth.getInstance();
        Toolbar myToolbar = findViewById(R.id.mainPageToolbar);
        TabLayout tabs = findViewById(R.id.tabs);
        FloatingActionButton addPost=findViewById(R.id.addPost);
        tabs.setupWithViewPager(viewPager);

        setSupportActionBar(myToolbar);


        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent goToNewsFeed=new Intent(getApplicationContext(),NewsFeed.class);
                startActivity(goToNewsFeed);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuth.getCurrentUser()!=null) {
            mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mRef.child("Online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if the user is logged in
        FirebaseUser currentUser=mAuth.getCurrentUser();

        //if user not logged in
        if(currentUser==null){

            Intent goToStartActivity=new Intent(getApplicationContext(),StartActivity.class);
            startActivity(goToStartActivity);
        }
        else{
            mRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mRef.child("Online").setValue("true");

            final String[] name = new String[1];
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            name[0] = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                            Objects.requireNonNull(getSupportActionBar()).setTitle(name[0]);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logOut) {

            FirebaseAuth.getInstance().signOut();
            mRef.child("Online").setValue(ServerValue.TIMESTAMP);
            mRef.child("Device Token").setValue("none");
            Intent goToStartActivity=new Intent(getApplicationContext(),StartActivity.class);
            goToStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToStartActivity);
        }

        else{
            if(item.getItemId()==R.id.settings) {
                Intent goToSettingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(goToSettingsActivity);
            }

            else{
                if(item.getItemId()==R.id.users) {
                    Intent goToUsersActivity = new Intent(getApplicationContext(), UsersActivity.class);
                    startActivity(goToUsersActivity);
                }

                else{
                    Intent goToStarMessagesActivity = new Intent(getApplicationContext(), StarMessagesActivity.class);
                    startActivity(goToStarMessagesActivity);
                }
            }
        }



        return super.onOptionsItemSelected(item);
    }



}
