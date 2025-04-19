package com.azwa.vibesent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddUserActivity extends AppCompatActivity {

    private EditText addUserEmailEditText;
    private Button addUserButton;
    private TextView errorMessage;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize UI elements
        addUserEmailEditText = findViewById(R.id.addUserEmailEditText);
        addUserButton = findViewById(R.id.addUserButton);
        errorMessage = findViewById(R.id.errorMessage);

        // Set up button click listener to add user
        addUserButton.setOnClickListener(v -> {
            String email = addUserEmailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(AddUserActivity.this, "Please enter an email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email exists in database
            checkAndAddUserByEmail(email);
        });
    }

    private void checkAndAddUserByEmail(String email) {
        DatabaseReference userRef = database.getReference().child("user");

        // Query to check if the user exists by email
        userRef.orderByChild("mail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User exists, proceed to add to current user's contact list
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        if (user != null) {
                            addUserToContactList(user);
                        }
                    }
                } else {
                    // Display error message if user is not found
                    errorMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddUserActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUserToContactList(Users user) {
        String currentUserId = auth.getUid();
        DatabaseReference contactsRef = database.getReference().child("contacts").child(currentUserId);

        // Add the found user to the current user's contact list
        contactsRef.child(user.getUserId()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddUserActivity.this, "User added to contacts", Toast.LENGTH_SHORT).show();
                finish(); // Close the current activity and return to the previous one
            } else {
                Toast.makeText(AddUserActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
