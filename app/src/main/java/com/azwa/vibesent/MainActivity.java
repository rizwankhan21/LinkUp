package com.azwa.vibesent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView mainUserRecyclerView;
    UserAdapter adapter;
    FirebaseDatabase database;
    ArrayList<Users> usersArrayList;

    ImageView cBut, callBut;

    // Fetch Last Messages for Users
    private void fetchLastMessages() {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats");
        String senderUid = FirebaseAuth.getInstance().getUid();

        if (senderUid == null || usersArrayList.isEmpty()) return;

        final int[] fetchedCount = {0};

        for (Users user : usersArrayList) {
            String receiverUid = user.getUserId();

            // Consistent room key by sorting user IDs lexicographically
            String roomId = senderUid.compareTo(receiverUid) < 0
                    ? senderUid + receiverUid
                    : receiverUid + senderUid;

            chatRef.child(roomId).child("messages").limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String lastMsg = "";
                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                lastMsg = chatSnapshot.child("message").getValue(String.class);
                            }

                            user.setLastMessage(lastMsg != null ? lastMsg : "");
                            fetchedCount[0]++;
                            if (fetchedCount[0] == usersArrayList.size()) {
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            fetchedCount[0]++;
                            if (fetchedCount[0] == usersArrayList.size()) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }


    // Fetch the last message from receiver's side
    private void fetchReceiverMessage(String receiverRoom, Users user, int[] fetchedCount) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats");

        chatRef.child(receiverRoom).child("messages").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lastMsg = "";

                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            lastMsg = chatSnapshot.child("message").getValue(String.class);
                        }

                        if (lastMsg != null && !lastMsg.equals(user.getLastMessage())) {
                            user.setLastMessage(lastMsg); // Update if different
                        }

                        fetchedCount[0]++;

                        // Only refresh UI once all last messages are fetched
                        if (fetchedCount[0] == usersArrayList.size()) {
                            adapter.notifyDataSetChanged();
                            android.util.Log.d("LAST_MSG", "All last messages fetched. Adapter refreshed.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        fetchedCount[0]++;
                        if (fetchedCount[0] == usersArrayList.size()) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // Status Bar Configuration
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Set white background for status bar
        //getWindow().setStatusBarColor(getResources().getColor(R.color.white));

        // Set dark status bar icons (Android 6.0+)
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Toolbar Configuration
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LinkUp");
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_ATOP);
        }

        // Firebase Initialization
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        cBut = findViewById(R.id.camBut);
        callBut = findViewById(R.id.call_icon);

        // Load Users
        usersArrayList = new ArrayList<>();
        DatabaseReference reference = database.getReference().child("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null && !user.getUserId().equals(auth.getCurrentUser().getUid())) {
                        usersArrayList.add(user);
                    }
                }

                // Notify adapter that data is loaded
                adapter.notifyDataSetChanged();

                // Now fetch the last messages
                fetchLastMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // RecyclerView setup
        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(MainActivity.this, usersArrayList);
        mainUserRecyclerView.setAdapter(adapter);

        // Button Actions for Camera
        cBut.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 10);
        });

        // Redirect if not logged in
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
        }
    }

    // Creating menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Code for menu showing options to settings and logout
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            Dialog dialog = new Dialog(MainActivity.this, R.style.dialog);
            dialog.setContentView(R.layout.dialog_layout);
            Button no = dialog.findViewById(R.id.nobtn);
            Button yes = dialog.findViewById(R.id.yesbtn);
            yes.setOnClickListener(v1 -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            });
            no.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
            return true;
        }
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(MainActivity.this, setting.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}
