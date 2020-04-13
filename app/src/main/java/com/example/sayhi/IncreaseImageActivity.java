package com.example.sayhi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class IncreaseImageActivity extends AppCompatDialogFragment {

    private String imageURL;
    private String friendName;
    private CircleImageView profile;

    IncreaseImageActivity(String url, String name) {

        imageURL=url;
        friendName=name;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        @SuppressLint("InflateParams") View v= Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.increase_image,null);

        profile=v.findViewById(R.id.profile);

        Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.usersayhii).into(profile);

        builder.setView(v).setTitle(friendName).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });



        return  builder.create();
    }
}
