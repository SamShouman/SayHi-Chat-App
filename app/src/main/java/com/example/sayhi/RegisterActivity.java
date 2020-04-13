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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText name,email,password;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        Button createAccount = findViewById(R.id.createAccount);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth=FirebaseAuth.getInstance();

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean data=true;
               final String userName= Objects.requireNonNull(name.getText()).toString().trim();
                String userEmail= Objects.requireNonNull(email.getText()).toString().trim();
                String userPassword= Objects.requireNonNull(password.getText()).toString();

                if(!userName.trim().matches("[a-zA-Z\\s]+") || userName.length()<3){
                    data=false;
                    name.setError("Please enter a valid name");
                }

                if(userName.trim().equals("")) {
                    data=false;
                    name.setError("Please enter your name");
                }


                if(userName.trim().split(" ").length!=2){
                    data=false;
                    name.setError("You must enter only your first name and last name");
                }

                if(!isValid(userEmail)){
                    data=false;
                    email.setError("Please enter a valid email");
                }

                if(userEmail.trim().equals("")){
                    data=false;
                    email.setError("Please enter your Email");
                }

                if(userPassword.length()<8) {
                    data = false;
                    password.setError("Password must be >= 8 characters");
                }

                if(userPassword.equals("")){
                    data=false;
                    password.setError("Please enter a password");
                }





                if(data) {
                    String[] name=userName.split(" ");
                    String fName=name[0].substring(0,1).toUpperCase() + name[0].substring(1).toLowerCase();
                    String lName=name[1].substring(0,1).toUpperCase() + name[1].substring(1).toLowerCase();
                    String fullName=fName+ " " + lName;
                    registerUser(fullName, userEmail, userPassword);
                }


            }
        });

    }

    private void registerUser(final String userName, final String userEmail, String userPassword) {
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                          //  FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                            assert user != null;
                            String userID=user.getUid();

                            database=FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                            HashMap<String,String> userData=new HashMap<>();
                            userData.put("Name",userName);
                            userData.put("Status","Hi there, I'm using SayHi.");
                            userData.put("Image","default");
                            userData.put("Device Token", FirebaseInstanceId.getInstance().getToken());
                            userData.put("InChatActivity","false");
                            userData.put("Wallpaper","default");
                            //kermel ne5d low level resolution pic eza a5dna lhigh level l app 7a ytwl la y3ml load
                            userData.put("Thumb","default");

                            database.setValue(userData);

                            progressDialog.dismiss();
                            Intent goToMainActivity=new Intent(getApplicationContext(),MainActivity.class);
                            goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(goToMainActivity);
                            finish();
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Email already exists.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}
