package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class StatusActivity extends AppCompatActivity {
    private TextInputEditText status;
    private DatabaseReference mRef;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Toolbar myToolbar = findViewById(R.id.appBarLayout);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        status=findViewById(R.id.newStatus);
        Button change = findViewById(R.id.change);

        mRef= FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()+"");

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog=new ProgressDialog(StatusActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Saving Changes...");
                progressDialog.show();
                String newStatus= Objects.requireNonNull(status.getText()).toString();

                if(!newStatus.trim().equals("") && newStatus.length()<=50) {
                    mRef.child("Status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Intent returnToSettingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(returnToSettingsActivity);
                                finishAffinity();
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                else{
                    if(newStatus.equals("")) {
                        status.setError("Please enter a new status");
                        progressDialog.dismiss();
                    }

                    else{
                        status.setError("Status must be less than 50 characters");
                        progressDialog.dismiss();
                    }
                }


            }
        });

    }
}
