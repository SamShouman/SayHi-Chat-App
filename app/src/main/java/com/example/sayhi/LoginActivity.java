package com.example.sayhi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private Toolbar myToolbar;
    private Button signin;
    private TextInputEditText email,password;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private ConstraintLayout layout;
    private DatabaseReference mRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myToolbar=findViewById(R.id.myToolbar);
        signin=findViewById(R.id.signin);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        layout=findViewById(R.id.layout);

        mAuth=FirebaseAuth.getInstance();
        mRef= FirebaseDatabase.getInstance().getReference().child("Users");

        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Log In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag=true;
                String userEmail=email.getText().toString();
                String userPassword=password.getText().toString();

                if(userEmail.equals("")){
                    flag=false;
                    email.setError("Please enter your email");
                }

                if(userPassword.equals("")){
                    flag=false;
                    password.setError("Please enter your password");
                }

                if(flag){
                    progressDialog=new ProgressDialog(LoginActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Logging in...");
                    progressDialog.show();

                    loginUser(userEmail,userPassword);

                }

            }
        });
    }

    private void loginUser(String userEmail, String userPassword) {

        mAuth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){
                progressDialog.dismiss();
                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                String currUser=FirebaseAuth.getInstance().getCurrentUser().getUid();
                mRef.child(currUser).child("Device Token").setValue(deviceToken);
                Intent goToMainActivity=new Intent(getApplicationContext(),MainActivity.class);
                goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(goToMainActivity);
                finish();
            }
            else{
                progressDialog.dismiss();
                Snackbar.make(layout,"User doesn't exist.",Snackbar.LENGTH_SHORT).show();
            }
            }
        });

    }
}
