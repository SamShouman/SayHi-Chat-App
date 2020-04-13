package com.example.sayhi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.UrlQuerySanitizer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;



public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    public static List<Messages> list;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private Context context;


    MessagesAdapter(List<Messages> list, Context c) {
        this.list = list;
        context=c;


    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth=FirebaseAuth.getInstance();
        mRef=FirebaseDatabase.getInstance().getReference();
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        final String currUser= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        final Messages m=list.get(position);



        String messageType=m.getType();
        if(list.size()>0)
            try{
        if(messageType.equals("image")) {
            holder.text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = m.getMessage();
                    Intent showMessage = new Intent(context, ShowImageMessage.class);
                    showMessage.putExtra("Url", url);
                    showMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(showMessage);
                }
            });
        }
            }
            catch(Exception e){
                    e.printStackTrace();
                }


        final String from= m.getFrom();

        holder.text.setText(m.getMessage());

        if(m.getType().equals("image"))
        {
            holder.text.setText("Click Me To View Image.");
            holder.text.setTypeface(null, Typeface.BOLD);
        }

        if(m.getStar().equals("true"))
            holder.star.setVisibility(View.VISIBLE);
        else
            holder.star.setVisibility(View.INVISIBLE);

        if (from.equals(currUser)) {

        holder.text.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onLongClick(View v) {



                AlertDialog.Builder builder=new AlertDialog.Builder(holder.text.getContext());


                builder.setTitle("Unsend Message")
                        .setMessage("Are you sure you want to unsend this message?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String[] inChatActivity = new String[1];
                                if(m.getType().equals("image"))
                                {
                                    String imageURL=m.getMessage();
                                    StorageReference messagePath= FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
                                    messagePath.delete();
                                }
                                mRef.child("Users").child(currUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        inChatActivity[0] = Objects.requireNonNull(dataSnapshot.child("InChatActivity")
                                                .getValue()).toString();

                                        mRef.child("Messages").child(currUser)
                                                .child(inChatActivity[0])
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        int i=0;
                                                        for(DataSnapshot d: dataSnapshot.getChildren())
                                                        {
                                                            if(i==position)
                                                            {   final String messageKey=d.getKey();

                                                                mRef.child("Messages").child(currUser)
                                                                        .child(inChatActivity[0])
                                                                        .child(Objects.requireNonNull(d.getRef().getKey()))
                                                                        .getRef()
                                                                        .removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mRef.child("Messages").child(currUser)
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        if(!dataSnapshot.child(inChatActivity[0])
                                                                                                .hasChildren())
                                                                                        {
                                                                                            FirebaseDatabase.getInstance().getReference()
                                                                                                    .child("Chat")
                                                                                                    .child(currUser)
                                                                                                    .child(inChatActivity[0])
                                                                                                    .removeValue();
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                        mRef.child("Star Messages").child(currUser)
                                                                                .child(inChatActivity[0])
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        assert messageKey != null;
                                                                                        if(dataSnapshot.hasChild(messageKey))
                                                                                            mRef.child("Star Messages").child(currUser)
                                                                                                    .child(inChatActivity[0])
                                                                                                    .child(messageKey)
                                                                                                    .removeValue();
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                    }
                                                                                });
                                                                    }
                                                                });




                                                                mRef.child("Messages").child(inChatActivity[0])
                                                                        .child(currUser)
                                                                        .child(d.getRef().getKey()).getRef()
                                                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mRef.child("Messages").child(inChatActivity[0])
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        if(!dataSnapshot.child(currUser)
                                                                                                .hasChildren()){
                                                                                            FirebaseDatabase.getInstance().getReference()
                                                                                                    .child("Chat")
                                                                                                    .child(inChatActivity[0])
                                                                                                    .child(currUser)
                                                                                                    .removeValue();
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                    }
                                                                                });
                                                                    }
                                                                });



                                                                list.remove(position);

                                                                Toast.makeText(holder.text.getContext(),
                                                                        "Message deleted, please restart the chat.",
                                                                        Toast.LENGTH_SHORT).show();





                                                                break;
                                                            }
                                                            i++;







                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });



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
                alert.show();
                int dividerId = alert.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
                View divider = alert.findViewById(dividerId);

                if(divider!=null)
                divider.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));

                int textViewId = alert.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                TextView tv =  alert.findViewById(textViewId);
                tv.setTextColor(context.getResources().getColor(R.color.colorAccent));
                return true;
            }
        });

        }





        mRef.child("Users").child(from).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image= Objects.requireNonNull(dataSnapshot.child("Image").getValue()).toString();
                holder.setProfile(image,context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





        holder.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToStarMessages=new Intent(context,StarMessagesActivity.class);
                goToStarMessages.putExtra("from",from);
                goToStarMessages.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(goToStarMessages);
            }
        });





    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView text;
        public CircleImageView profile;
        public View v;
        public ImageView star;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
            text=itemView.findViewById(R.id.messageChat);
            profile=itemView.findViewById(R.id.profileChat);
            star=itemView.findViewById(R.id.star);

        }

        void setProfile(String url, Context c){
            Picasso.with(c).load(url).placeholder(R.drawable.usersayhii).into(profile);

        }



    }






}
