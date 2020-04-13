package com.example.sayhi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    private DatabaseReference mUsers;
    private ArrayList<String> names=new ArrayList<>();
    private ArrayList<String> images=new ArrayList<>();

    public RequestsFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_requests, container, false);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests").child(currUser);
            mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            RecyclerView requests = v.findViewById(R.id.requests);
            requests.setLayoutManager(new LinearLayoutManager(getContext()));

            mRef.keepSynced(true);


            FirebaseRecyclerOptions<Requests> options = new FirebaseRecyclerOptions.Builder<Requests>()
                    .setQuery(mRef, Requests.class)
                    .build();

            FirebaseRecyclerAdapter<Requests, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>
                    (options) {
                @Override
                protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, final int position, @NonNull Requests model) {
                    final String userID = getRef(position).getKey();
                    final String[] requestType = {model.getType()};


                    assert userID != null;
                    mUsers.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // String requestType=dataSnapshot.child("Type").getValue().toString();


                                names.add(Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString());
                                images.add(Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString());

                                holder.setName(Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString());
                                holder.setProfile(Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString(), getActivity());
                                holder.setOnline(Objects.requireNonNull(dataSnapshot.child("Online").getValue()).toString());
                                holder.setType(requestType[0]);

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    holder.v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent goToProfile = new Intent(getActivity(), UserProfileActivity.class);
                            goToProfile.putExtra("UserID", userID);
                            startActivity(goToProfile);
                        }
                    });

                    holder.v.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            IncreaseImageActivity dialog = new IncreaseImageActivity(images.get(position), names.get(position));
                            assert getFragmentManager() != null;
                            dialog.show(getFragmentManager(), names.get(position));
                        }
                    });


                }

                @NonNull
                @Override
                public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    Log.i("OnCreateViewHolder", "CALLED");
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.user_layout, parent, false);

                    return new RequestsViewHolder(view);
                }
            };


            requests.setAdapter(adapter);
            adapter.startListening();


            return v;


        }
            return  null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        View v;
        RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
         //   v.findViewById(R.id.status).setVisibility(View.INVISIBLE);
        }

        void setProfile(String imageProfile, Context c){

            CircleImageView profile=v.findViewById(R.id.profile);
            Picasso.with(c).load(imageProfile).placeholder(R.drawable.usersayhii).into(profile);

        }

        public void setName(String userName){
            TextView name=v.findViewById(R.id.name);
            name.setText(userName);
        }

        public void setOnline(String ifOnline){
            ImageView online=v.findViewById(R.id.online);
            if(ifOnline.equals("true")){
                online.setVisibility(View.VISIBLE);
            }
        }

        @SuppressLint("SetTextI18n")
        void setType(String type) {
            final String reqType = type.substring(0, 1).toUpperCase() + type.substring(1);
            if(type.equals("sent")) {
                TextView status = v.findViewById(R.id.status);
                status.setText(reqType);
            }

            else{
                TextView status = v.findViewById(R.id.status);
                status.setTextColor(Color.GREEN);
                status.setText(reqType);
            }
        }
    }






}
