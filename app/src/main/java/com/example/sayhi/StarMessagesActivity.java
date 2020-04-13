package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class StarMessagesActivity extends AppCompatActivity {
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private ArrayAdapter adapter;
    private ArrayList<String> friendsNames=new ArrayList<>();
    private HashMap<String,String> friends=new HashMap<>();
    private String currUser;
    private Spinner starFriends;
    private RecyclerView recView;
    private ArrayList<Integer> positions=new ArrayList<>();
    private ArrayList<String> keys=new ArrayList<>();
    private String friendKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_messages);
        Firebase.setAndroidContext(this);
        mRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        currUser=mAuth.getCurrentUser().getUid();

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        starFriends=findViewById(R.id.starFriends);
        recView=findViewById(R.id.recView);

        setSupportActionBar(myToolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Star Messages");




        loadFriendsNames();

        removeWhenSwiped();

            starFriends.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int i=0;

                for(String key: friends.keySet()) {
                    if (i == position)
                    {   friendKey=key;

                        viewStarMessages(key);
                        break;
                    }
                    i++;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {



            }
        });
    }

    private void viewStarMessages(final String key)
    {


        LinearLayoutManager manager=new LinearLayoutManager(this);
        recView.setLayoutManager(manager);

        Query starMessages=mRef.child("Star Messages").child(currUser).child(key).orderByChild("Time");

        FirebaseRecyclerOptions<Messages> options=new FirebaseRecyclerOptions.Builder<Messages>()
              .setQuery(starMessages,Messages.class)
                .build();

        FirebaseRecyclerAdapter<Messages, MessagesAdapter.MessageViewHolder> adapter=new FirebaseRecyclerAdapter<Messages, MessagesAdapter.MessageViewHolder>
                (options) {
            @Override
            protected void onBindViewHolder(@NonNull final MessagesAdapter.MessageViewHolder holder, final int position, @NonNull final Messages model) {

                  String friendKey= Objects.requireNonNull(getRef(position).getParent()).getKey();
                assert friendKey != null;
                Firebase check=new Firebase("https://sayhi-6935e.firebaseio.com/");
                check.child("Messages").child(currUser).child(friendKey)
                          .child(Objects.requireNonNull(getRef(position).getKey())).addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                      @SuppressLint("SetTextI18n")
                      @Override
                      public void onDataChange(@NonNull com.firebase.client.DataSnapshot dataSnapshot) {
                          String type= Objects.requireNonNull(dataSnapshot.child("Type").getValue()).toString();

                          if(type.equals("image")) {
                              holder.text.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      String url = model.getMessage();
                                      Intent showMessage = new Intent(getApplicationContext(), ShowImageMessage.class);
                                      showMessage.putExtra("Url", url);
                                      showMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                      getApplicationContext().startActivity(showMessage);
                                  }
                              });

                              holder.text.setText("Click Me To View Image.");
                              holder.text.setTypeface(null, Typeface.BOLD);
                          }

                          else
                              holder.text.setText(model.getMessage());
                      }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });







        holder.star.setVisibility(View.VISIBLE);


                check.child("Users").addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final com.firebase.client.DataSnapshot d) {

                        final String[] image = new String[1];
                        final String messageKey=getRef(position).getKey();

                        mRef.child("Messages").child(currUser).child(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        String from= Objects.requireNonNull(dataSnapshot.child(messageKey).child("From").getValue()).toString();
                                        if(!from.equals(currUser))
                                            image[0] =Objects.requireNonNull(d.child(key).child("Image").getValue()).toString();
                                        else
                                            image[0] = Objects.requireNonNull(d.child(currUser).child("Image").getValue()).toString();

                                        holder.setProfile(image[0],getApplicationContext());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                    }

                    @Override
                    public void onCancelled(@NonNull FirebaseError databaseError) {

                    }
                });

                holder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent goToChatActivity=new Intent(getApplicationContext(),ChatActivity.class);
                        goToChatActivity.putExtra("UserID",key);
                        startActivity(goToChatActivity);
                    }
                });



                positions.add(position);
                keys.add(getRef(position).getKey());



        }



            @NonNull
            @Override
            public MessagesAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_single_layout, parent, false);

                return new MessagesAdapter.MessageViewHolder(view);
            }
        };

        recView.setAdapter(adapter);
        adapter.startListening();



    }

    private void removeWhenSwiped() {


        final ItemTouchHelper.SimpleCallback touch = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final DatabaseReference mStar = FirebaseDatabase.getInstance().getReference()
                        .child("Star Messages");


                mRef.child("Messages").child(currUser)
                        .child(friendKey).child(keys.get(viewHolder.getAdapterPosition()))
                        .child("Star").setValue("false");

                mStar.child(currUser).child(friendKey)
                        .child(keys.get(viewHolder.getAdapterPosition())).removeValue();

                Intent restart = getIntent();
                finish();
                startActivity(restart);

            }
        };

        new ItemTouchHelper(touch).attachToRecyclerView(recView);
    }


















    private void loadFriendsNames() {



        mRef.child("Star Messages").child(currUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren())
                {   for(DataSnapshot d: dataSnapshot.getChildren()) {

                    final String friendKey = d.getKey();

                    mRef.child("Users").child(friendKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String friendName= Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                            friendsNames.add(friendName);
                            friends.put(friendKey,friendName);


                            adapter=new ArrayAdapter(getApplicationContext(),R.layout.spinner_items,friendsNames);
                            starFriends.setAdapter(adapter);

                            setNameAuto();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }






                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        friendsNames.clear();
    }

    private void setNameAuto() {

        String from=getIntent().getStringExtra("from");

        if(from != null)
        {   int i=0;

            for(String key: friends.keySet())
            {
                if (key.equals(from))
                    starFriends.setSelection(i);
                i++;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mAuth.getCurrentUser()!=null)
        {
            mRef.child("Users").child(currUser).child("Online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRef.child("Users").child(currUser).child("Online").setValue("true");
    }
}
