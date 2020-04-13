package com.example.sayhi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ShowImageMessage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_message);

        String url=getIntent().getStringExtra("Url");

        ImageView image=findViewById(R.id.imageMessage);
        Picasso.with(getApplicationContext()).load(url).placeholder(R.drawable.usersayhii).into(image);
    }
}
