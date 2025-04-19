package com.azwa.vibesent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {
    TextView loginBtn;
    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageURI;
    String imageuri;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your account...");
        progressDialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        rg_signup = findViewById(R.id.signUpBtnOnLogin);
        rg_username = findViewById(R.id.rgUserName);
        rg_email = findViewById(R.id.rgEmail);
        rg_password = findViewById(R.id.rgPassword);
        rg_repassword = findViewById(R.id.rgRePassword);
        loginBtn = findViewById(R.id.loginBut);
        rg_profileImg = findViewById(R.id.profilerg0);

        loginBtn.setOnClickListener(v -> {
            startActivity(new Intent(registration.this, login.class));
            finish();
        });

        rg_signup.setOnClickListener(v -> {
            String namee = rg_username.getText().toString();
            String emaill = rg_email.getText().toString();
            String Password = rg_password.getText().toString();
            String cPassword = rg_repassword.getText().toString();
            String status = "Hey, I'm using this application!";

            if (TextUtils.isEmpty(namee) || TextUtils.isEmpty(emaill) || TextUtils.isEmpty(Password) || TextUtils.isEmpty(cPassword)) {
                Toast.makeText(registration.this, "Please enter valid information", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!emaill.matches(emailPattern)) {
                rg_email.setError("Enter a valid email");
                return;
            }

            if (Password.length() < 6) {
                rg_password.setError("Password must be at least 6 characters");
                return;
            }

            if (!Password.equals(cPassword)) {
                rg_repassword.setError("Passwords do not match");
                return;
            }

            // ✅ Show progress dialog BEFORE creating the account
            progressDialog.show();

            auth.createUserWithEmailAndPassword(emaill, Password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String id = user.getUid();
                                DatabaseReference reference = database.getReference().child("user").child(id);
                                StorageReference storageReference = storage.getReference().child("Upload").child(id);

                                if (imageURI != null) {
                                    storageReference.putFile(imageURI)
                                            .addOnCompleteListener(uploadTask -> {
                                                if (uploadTask.isSuccessful()) {
                                                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        imageuri = uri.toString();
                                                        saveUserToDatabase(reference, id, namee, emaill, Password, imageuri, status);
                                                    });
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(registration.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    imageuri = "https://firebasestorage.googleapis.com/v0/b/vibe-sent.firebasestorage.app/o/man.png?alt=media&token=f82ca100-b99a-4937-8803-0d8f5c9eaeee";
                                    saveUserToDatabase(reference, id, namee, emaill, Password, imageuri, status);
                                }
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(registration.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });
    }

    private void saveUserToDatabase(DatabaseReference reference, String id, String name, String email, String password, String image, String status) {
        Users users = new Users(id, name, email, password, image, status);
        reference.setValue(users).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(registration.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(registration.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(registration.this, "Error saving user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }
    }
}
