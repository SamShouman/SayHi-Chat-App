package com.example.sayhi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment implements SearchView.OnQueryTextListener{

    private RecyclerView chatsRecView;
    private DatabaseReference users,conversations,messages;
    private String currUser;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ChatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);
        setHasOptionsMenu(true);
        chatsRecView= v.findViewById(R.id.recViewChats);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            currUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            conversations = FirebaseDatabase.getInstance().getReference().child("Chat").child(currUser);
            conversations.keepSynced(true);
            users = FirebaseDatabase.getInstance().getReference().child("Users");
            messages = FirebaseDatabase.getInstance().getReference().child("Messages").child(currUser);


            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            manager.setReverseLayout(true);
            manager.setStackFromEnd(true);

            chatsRecView.setLayoutManager(manager);

            Query query = conversations.orderByChild("TimeStamp");


            FirebaseRecyclerOptions<Conversations> convs = new FirebaseRecyclerOptions.Builder<Conversations>()
                    .setQuery(query, Conversations.class)
                    .build();

            FirebaseRecyclerAdapter<Conversations, ConversationsViewHolder> adapter = new FirebaseRecyclerAdapter<Conversations, ConversationsViewHolder>(convs) {
                @Override
                protected void onBindViewHolder(@NonNull final ConversationsViewHolder holder, int position, @NonNull final Conversations model) {

                    final String senderUserID = getRef(position).getKey();

                    assert senderUserID != null;
                    Query query = messages.child(senderUserID).limitToLast(1);

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            if (dataSnapshot.child("Message").getValue() != null ) {
                                String messageReceived = Objects.requireNonNull(dataSnapshot.child("Message")
                                        .getValue()).toString();

                                if(Objects.requireNonNull(dataSnapshot.child("Type")
                                        .getValue()).toString().equals("image"))
                                    messageReceived="Click Me To View Image.";

                                holder.setLastMessage(messageReceived, senderUserID);
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





                    users.child(senderUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChildren()) {
                                String userName = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                                String image = Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                                String userOnline = Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString();
                                holder.setUserOnline(userOnline);
                                holder.setProfile(image, getContext());
                                holder.setName(userName);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    holder.v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent goToChatActivity = new Intent(getActivity(), ChatActivity.class);
                            goToChatActivity.putExtra("UserID", senderUserID);
                            startActivity(goToChatActivity);
                        }
                    });

                    holder.v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final String[] friendName = new String[1];
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            friendName[0] = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();

                                            String friendNameInBold="<b>"+friendName[0]+"</b>";

                                            AlertDialog.Builder builder=new AlertDialog.Builder(holder.v.getContext());


                                            builder.setTitle("Delete Chat")
                                                    .setMessage("Are you sure you want to delete the chat with " +Html.fromHtml(friendNameInBold) + "?")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            ProgressDialog progressDialog=new ProgressDialog(getContext());
                                                            progressDialog.setMessage("Deleting chat...");
                                                            progressDialog.setCancelable(false);
                                                            progressDialog.show();

                                                            FirebaseDatabase.getInstance().getReference()
                                                                    .child("Star Messages")
                                                                    .child(currUser)
                                                                    .child(senderUserID)
                                                                    .removeValue();

                                                            FirebaseDatabase.getInstance().getReference()
                                                                    .child("Messages")
                                                                    .child(currUser)
                                                                    .child(senderUserID)
                                                                    .removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid)
                                                                {

                                                                    FirebaseDatabase.getInstance().getReference()
                                                                            .child("Chat").child(currUser)
                                                                            .child(senderUserID)
                                                                            .removeValue();
                                                                }
                                                            });



                                                            progressDialog.dismiss();






                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

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
                                                divider.setBackgroundColor(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.colorAccent));

                                                int textViewId = alert.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                                                TextView tv = alert.findViewById(textViewId);
                                                tv.setTextColor(getActivity().getResources().getColor(R.color.colorAccent));
                                            }catch(Exception e){
                                                e.printStackTrace();

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                            return  true;
                        }
                    });

                    holder.profile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CharSequence[] download={"Save Image To Gallery"};
                            AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                            builder.setItems(download, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    ImageView postImage=holder.profile;
                                    postImage.setDrawingCacheEnabled(true);
                                    Bitmap b = postImage.getDrawingCache();
                                    if(MediaStore.Images.Media.insertImage(Objects.requireNonNull(getActivity()).getContentResolver(),
                                            b,getSaltString(),"") != null);
                                    Toast.makeText(getActivity(),"Image saved to Gallery\\Camera.",Toast.LENGTH_SHORT).show();
                                }
                            });

                            AlertDialog dialog=builder.create();
                            dialog.show();
                        }
                    });

                }

                @NonNull
                @Override
                public ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.user_layout, parent, false);

                    return new ConversationsViewHolder(view);
                }
            };


            chatsRecView.setAdapter(adapter);
            adapter.startListening();



            return v;

        }



        return  null;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Search Chats...");
        searchView.setOnQueryTextListener(this);

        searchView.animate();

        searchView.setIconifiedByDefault(true);
        //to change search view hint color
        int id =  searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = searchView.findViewById(id);
        textView.setHintTextColor(Color.parseColor("#FFFFFF"));

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
        else{
            Query query = conversations.orderByChild("TimeStamp");

            FirebaseRecyclerOptions<Conversations> convs = new FirebaseRecyclerOptions.Builder<Conversations>()
                    .setQuery(query, Conversations.class)
                    .build();

            FirebaseRecyclerAdapter<Conversations, ConversationsViewHolder> adapter = new FirebaseRecyclerAdapter<Conversations, ConversationsViewHolder>(convs) {
                @Override
                protected void onBindViewHolder(@NonNull final ConversationsViewHolder holder, int position, @NonNull final Conversations model) {

                    final String senderUserID = getRef(position).getKey();
                    assert senderUserID != null;
                    Query query = messages.child(senderUserID).limitToLast(1);



                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            if (!Objects.requireNonNull(dataSnapshot.child("Message").getValue()).toString().equals("")) {
                                String messageReceived = Objects.requireNonNull(dataSnapshot.child("Message").getValue()).toString();
                                holder.setLastMessage(messageReceived, senderUserID);
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



                    users.child(senderUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()) {
                                String userName = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                                String image = Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                                String userOnline = Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString();
                                holder.setUserOnline(userOnline);
                                holder.setProfile(image, getContext());
                                holder.setName(userName);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    holder.v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent goToChatActivity = new Intent(getActivity(), ChatActivity.class);
                            goToChatActivity.putExtra("UserID", senderUserID);
                            startActivity(goToChatActivity);
                        }
                    });

                }

                @NonNull
                @Override
                public ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.user_layout, parent, false);

                    return new ConversationsViewHolder(view);
                }
            };


            chatsRecView.setAdapter(adapter);
            adapter.startListening();
        }


        return false;
    }

    private void firebaseSearch(String searchText)
    {

        final ArrayList<String> chatFriends=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Chat").child(currUser)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d:dataSnapshot.getChildren()){
                    chatFriends.add(d.getKey());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Query search=users
                .orderByChild("Name").startAt(searchText)
                .endAt(searchText+"\uf0ff");


        FirebaseRecyclerOptions<Conversations> convs = new FirebaseRecyclerOptions.Builder<Conversations>()
                .setQuery(search, Conversations.class)
                .build();

        FirebaseRecyclerAdapter<Conversations, ConversationsViewHolder> adapter = new FirebaseRecyclerAdapter<Conversations, ConversationsViewHolder>(convs) {
            @Override
            protected void onBindViewHolder(@NonNull final ConversationsViewHolder holder, int position, @NonNull final Conversations model) {

                final String senderUserID = getRef(position).getKey();
                if(chatFriends.contains(senderUserID)) {
                    assert senderUserID != null;
                    Query query = messages.child(senderUserID).limitToLast(1);

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            if (!Objects.requireNonNull(dataSnapshot.child("Message").getValue()).toString().equals("")) {
                                String messageReceived = Objects.requireNonNull(dataSnapshot.child("Message").getValue()).toString();
                                holder.setLastMessage(messageReceived, senderUserID);
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




                    users.child(senderUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                String userName = Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString();
                                String image = Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                                String userOnline = Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString();
                                holder.setUserOnline(userOnline);
                                holder.setProfile(image, getContext());
                                holder.setName(userName);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    holder.v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent goToChatActivity = new Intent(getActivity(), ChatActivity.class);
                            goToChatActivity.putExtra("UserID", senderUserID);
                            startActivity(goToChatActivity);
                        }
                    });

                }

                else{
                    holder.itemView.findViewById(R.id.status).setVisibility(View.GONE);
                    holder.itemView.findViewById(R.id.name).setVisibility(View.GONE);
                    holder.itemView.findViewById(R.id.profile).setVisibility(View.GONE);

                    holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                    holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                    holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));

                    holder.itemView.findViewById(R.id.profile).setEnabled(false);
                    holder.itemView.findViewById(R.id.name).setEnabled(false);
                    holder.itemView.findViewById(R.id.status).setEnabled(false);

                }



            }

            @NonNull
            @Override
            public ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_layout, parent, false);

                return new ConversationsViewHolder(view);
            }
        };


        chatsRecView.setAdapter(adapter);
        adapter.startListening();



    }

    public static class ConversationsViewHolder extends RecyclerView.ViewHolder{

        public View v;
        CircleImageView profile;
        String[] lastMessageKey =new String[1];
        String[] seen = new String[1];

        ConversationsViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
            profile=v.findViewById(R.id.profile);





        }

        //this annotation ignores the warning
        @SuppressLint("SetTextI18n")
        void setLastMessage(final String content, final String senderID){
            final TextView lastMessage=v.findViewById(R.id.status);
        final DatabaseReference mRef=FirebaseDatabase.getInstance().getReference().child("Messages")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(senderID);

        Query q=mRef.orderByKey().limitToLast(1);


        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        lastMessageKey[0] = d.getKey();

                        mRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (lastMessageKey[0] != null && dataSnapshot.hasChildren()) {
                                    Log.i("LASTMESSAGE", lastMessageKey[0]);
                                    // Log.i("LASTMESSAGE",seen[0]);
                                    try {
                                        seen[0] = Objects.requireNonNull(dataSnapshot.child(lastMessageKey[0]).child("Seen").getValue()).toString();


                                        if (seen[0].equals("true")) {

                                            lastMessage.setTextColor(Color.parseColor("#f54300"));
                                        } else {
                                            lastMessage.setTextColor(Color.BLACK);
                                        }


                                    } catch (Exception e) {
                                                e.printStackTrace();
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        break;

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



            if(content.length()>30)
            {
                lastMessage.setText(content.substring(0,30)+"...");
            }

            else {
                lastMessage.setText(content);
            }





        }

        void setProfile(String image, Context c){

            Picasso.with(c).load(image).placeholder(R.drawable.usersayhii).into(profile);
        }

        public void setName(String userName){
            TextView  name=v.findViewById(R.id.name);
            name.setText(userName);
        }

        void setUserOnline(String Online){
            ImageView online=v.findViewById(R.id.online);
            if(Online.equals("true")){
                online.setVisibility(View.VISIBLE);
            }

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
