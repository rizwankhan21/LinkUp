package com.azwa.vibesent;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    Button button;
    TextView signUp;
    EditText email,password;
    FirebaseAuth auth;

    android.app.ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(false);
        auth = FirebaseAuth.getInstance();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        button = findViewById(R.id.LogButton);
        email = findViewById(R.id.editTextLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        signUp = findViewById(R.id.signUpBtnOnLogin);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString();
                String pass = password.getText().toString();

                if((TextUtils.isEmpty(Email))){
                    progressDialog.dismiss();
                    Toast.makeText(login.this,"Enter the email",Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(pass)){
                    progressDialog.dismiss();
                    Toast.makeText(login.this,"Enter the password",Toast.LENGTH_SHORT).show();
                } else if (!Email.matches(emailPattern)) {
                    progressDialog.dismiss();
                    email.setError("Give proper email address.");
                } else if (password.length()<6) {
                    progressDialog.dismiss();
                    password.setError("More than 6 characters.");
                    Toast.makeText(login.this,"Password need to be more than six characters.", Toast.LENGTH_SHORT).show();
                }else {
                    auth.signInWithEmailAndPassword(Email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                progressDialog.show();
                                try {
                                    Intent intent = new Intent(login.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    Toast.makeText(login.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(login.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this,registration.class);
                startActivity(intent);
                finish();
            }
        });

    }
}