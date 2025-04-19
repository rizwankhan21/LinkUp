package com.azwa.vibesent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class setting extends AppCompatActivity {

    ImageView setprofile;
    EditText setname, setstatus;
    Button donebut;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String email, password;
    Uri setImageUri;
    ImageView chatBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebut);
        chatBut = findViewById(R.id.chatBut);

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    email = snapshot.child("mail").getValue(String.class);
                    password = snapshot.child("password").getValue(String.class);
                    String name = snapshot.child("userName").getValue(String.class);
                    String profile = snapshot.child("profilepic").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    setname.setText(name);
                    setstatus.setText(status);
                    Picasso.get().load(profile).into(setprofile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        setprofile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        donebut.setOnClickListener(v -> {
            String name = setname.getText().toString();
            String status = setstatus.getText().toString();

            if (setImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), setImageUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos); // Compress to 50% quality
                    byte[] compressedData = baos.toByteArray();

                    storageReference.putBytes(compressedData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String finalimageUri = uri.toString();
                                saveUserData(reference, name, status, finalimageUri);
                            });
                        } else {
                            Toast.makeText(setting.this, "Image upload failed...", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(setting.this, "Image processing failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String finalImageUri = uri.toString();
                    saveUserData(reference, name, status, finalImageUri);
                });
            }
        });
        chatBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(setting.this,MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void saveUserData(DatabaseReference reference, String name, String status, String imageUri) {
        Users users = new Users(auth.getUid(), name, email, password, imageUri, status);
        reference.setValue(users).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(setting.this, "Data is saved", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(setting.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(setting.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);
        }
    }


}