package com.example.sayhi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;



import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements SearchView.OnQueryTextListener {
    private View v;
    private FirebaseAuth mAuth;
    private RecyclerView recView;
    private String currUser;
    private DatabaseReference mRef,mUsersDatabase;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        v=inflater.inflate(R.layout.fragment_friends, container, false);
         setHasOptionsMenu(true);
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager
                .LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        recView=v.findViewById(R.id.recView);
      //  recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        currUser= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(currUser);
        mRef.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        FirebaseRecyclerOptions<Friends> friends=new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mRef,Friends.class).build();
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> adapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friends) {


            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull  Friends model) {
                holder.setDate(model.getDate());
                final String userID=getRef(position).getKey();

                assert userID != null;
                mUsersDatabase.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name= Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                        final String image= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                        String ifOnline= Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString();

                        holder.setName(name);
                        holder.setImage(image,getContext());
                        holder.checkIfOnline(ifOnline);



                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence[] options =new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                builder.setTitle("Select An Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {

                                        if(i==0){
                                            Intent goToProfileActivity=new Intent(getContext(),UserProfileActivity.class);
                                            goToProfileActivity.putExtra("UserID",userID);
                                            startActivity(goToProfileActivity);
                                        }

                                        if(i==1){
                                            Intent goToChatActivity=new Intent(getContext(),ChatActivity.class);
                                            goToChatActivity.putExtra("UserID",userID);
                                            startActivity(goToChatActivity);
                                        }

                                    }
                                });

                                builder.show();

                            }
                        });

                        holder.view.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                IncreaseImageActivity dialog = new IncreaseImageActivity(image,name);
                                assert getFragmentManager() != null;
                                dialog.show(getFragmentManager(),name);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_layout, parent, false);

                return new FriendsViewHolder(view);
            }
        };


        recView.setAdapter(adapter);
        adapter.startListening();


        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu,menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Search Friends...");
        searchView.setOnQueryTextListener(this);

        searchView.setIconifiedByDefault(true);
        searchView.animate();
        searchView.clearFocus();

        int id =  searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = searchView.findViewById(id);
        textView.setHintTextColor(Color.parseColor("#FFFFFF"));


    }

    private void firebaseSearch(final String search){
        final ArrayList<String> friendsList=new ArrayList<>();
        friendsList.clear();
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot d:dataSnapshot.getChildren()){
                        friendsList.add(d.getKey());
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query getSearchedFriends=FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .orderByChild("Name")
                .startAt(search)
                .endAt(search+"\uf0ff");

        FirebaseRecyclerOptions<Friends> friends=new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(getSearchedFriends,Friends.class)
                .build();
        final FirebaseRecyclerAdapter<Friends,FriendsViewHolder> adapter=new FirebaseRecyclerAdapter
                <Friends, FriendsViewHolder>(friends) {



            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull  Friends model) {



                final String userID=getRef(position).getKey();

                if(friendsList.contains(userID)) {



                    assert userID != null;
                    mUsersDatabase.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final String name = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                            final String image = Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                            String ifOnline = Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString();
                            FirebaseDatabase.getInstance().getReference()
                                            .child("Friends")
                                            .child(currUser).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    holder.setDate(Objects.requireNonNull(dataSnapshot.child(userID).child("Date").getValue()).toString());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            holder.setName(name);
                            holder.setImage(image, getContext());
                            holder.checkIfOnline(ifOnline);


                            holder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence[] options = new CharSequence[]{"Open Profile", "Send Message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Select An Option");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {

                                            if (i == 0) {
                                                Intent goToProfileActivity = new Intent(getContext(), UserProfileActivity.class);
                                                goToProfileActivity.putExtra("UserID", userID);
                                                startActivity(goToProfileActivity);
                                            }

                                            if (i == 1) {
                                                Intent goToChatActivity = new Intent(getContext(), ChatActivity.class);
                                                goToChatActivity.putExtra("UserID", userID);
                                                startActivity(goToChatActivity);
                                            }

                                        }
                                    });

                                    AlertDialog alert=builder.create();

                                    alert.show();



                                }
                            });

                            holder.view.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    IncreaseImageActivity dialog = new IncreaseImageActivity(image, name);
                                    assert getFragmentManager() != null;
                                    dialog.show(getFragmentManager(), name);

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                else
                    {
                    holder.view.findViewById(R.id.status).setVisibility(View.INVISIBLE);
                    holder.view.findViewById(R.id.name).setVisibility(View.INVISIBLE);
                    holder.view.findViewById(R.id.profile).setVisibility(View.INVISIBLE);

                        holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                        holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                        holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));

                    holder.view.findViewById(R.id.profile).setEnabled(false);
                    holder.view.findViewById(R.id.name).setEnabled(false);
                    holder.view.findViewById(R.id.status).setEnabled(false);




                }
            }


            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_layout, parent, false);

                return new FriendsViewHolder(view);
            }
        };


        recView.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        firebaseSearch(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!newText.equals(""))
        firebaseSearch(newText);
        else
        {
            FirebaseRecyclerOptions<Friends> friends=new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mRef,Friends.class).build();
            FirebaseRecyclerAdapter<Friends,FriendsViewHolder> adapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friends) {


                @Override
                protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull  Friends model) {
                    holder.setDate(model.getDate());
                    final String userID=getRef(position).getKey();

                    assert userID != null;
                    mUsersDatabase.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final String name= Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                            final String image= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                            String ifOnline= Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString();

                            holder.setName(name);
                            holder.setImage(image,getContext());
                            holder.checkIfOnline(ifOnline);



                            holder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence[] options =new CharSequence[]{"Open Profile","Send Message"};
                                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                    builder.setTitle("Select An Option");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {

                                            if(i==0){
                                                Intent goToProfileActivity=new Intent(getContext(),UserProfileActivity.class);
                                                goToProfileActivity.putExtra("UserID",userID);
                                                startActivity(goToProfileActivity);
                                            }

                                            if(i==1){
                                                Intent goToChatActivity=new Intent(getContext(),ChatActivity.class);
                                                goToChatActivity.putExtra("UserID",userID);
                                                startActivity(goToChatActivity);
                                            }

                                        }
                                    });

                                    builder.show();

                                }
                            });

                            holder.view.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    IncreaseImageActivity dialog = new IncreaseImageActivity(image,name);
                                    assert getFragmentManager() != null;
                                    dialog.show(getFragmentManager(),name);

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


                @NonNull
                @Override
                public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.user_layout, parent, false);

                    return new FriendsViewHolder(view);
                }
            };


            recView.setAdapter(adapter);
            adapter.startListening();
        }


        return false;
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View view;
        FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            view=itemView;

        }

        @SuppressLint("SetTextI18n")
        void setDate(String date){
            Log.i("DATE",date+"");
            TextView userStatusView=view.findViewById(R.id.status);
            userStatusView.setText("Friends since " + date);

        }

        public void setName(String name){
            TextView userName=view.findViewById(R.id.name);
            userName.setText(name);
        }

        void setImage(String image, Context c){
            CircleImageView userImage=view.findViewById(R.id.profile);
            Picasso.with(c).load(image).placeholder(R.drawable.usersayhii).into(userImage);


        }

        void checkIfOnline(@org.jetbrains.annotations.NotNull final String online){
            final ImageView iv=view.findViewById(R.id.online);

                    if(online.equals("true")){
                        iv.setVisibility(View.VISIBLE);
                    }

                    else{
                        iv.setVisibility(View.INVISIBLE);

                    }


        }

    }


}
