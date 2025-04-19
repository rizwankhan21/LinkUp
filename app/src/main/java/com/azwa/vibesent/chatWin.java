package com.azwa.vibesent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatWin extends AppCompatActivity {

    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView receiverNName;
    CardView sendbtn;
    EditText textmsg;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    public static String senderImg;
    public static String receiverImg;
    String senderRoom, reciverRoom;
    RecyclerView mmessagesAdpter;
    ArrayList<msgModelClass> messagessArrayList;
    messagesAdpter messagesAdpter;

    public static boolean isChatOpen = false; // 👈 Prevent notifications when chat is open

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);

        // Set white background for status bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));

// Set dark status bar icons (Android 6.0+)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");
        SenderUID = firebaseAuth.getUid();

        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        messagessArrayList = new ArrayList<>();

        profile = findViewById(R.id.profileimgg);
        receiverNName = findViewById(R.id.receiverName);
        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        mmessagesAdpter = findViewById(R.id.msgadpter);

        Picasso.get().load(reciverimg).into(profile);
        receiverNName.setText(reciverName);

        mmessagesAdpter.setLayoutManager(new LinearLayoutManager(this));
        messagesAdpter = new messagesAdpter(messagessArrayList, this);
        mmessagesAdpter.setAdapter(messagesAdpter);

        // 🔔 Create notification channel (only once)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "msg_channel", "Message Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        DatabaseReference senderRef = database.getReference().child("chats").child(senderRoom).child("messages");
        senderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagessArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelClass message = dataSnapshot.getValue(msgModelClass.class);
                    messagessArrayList.add(message);

                    // 🔔 Show notification if message is from receiver and chat screen is not open
                    if (message != null && !message.getSenderid().equals(SenderUID) && !isChatOpen) {
                        showNotification(reciverName, message.getMessage());
                    }
                }
                messagesAdpter.notifyDataSetChanged();
                mmessagesAdpter.scrollToPosition(messagessArrayList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderImg = snapshot.child("profilepic").getValue(String.class);
                receiverImg = reciverimg;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        sendbtn.setOnClickListener(v -> {
            String message = textmsg.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(chatWin.this, "Enter the message first", Toast.LENGTH_SHORT).show();
                return;
            }

            textmsg.setText("");
            Date date = new Date();
            msgModelClass messagess = new msgModelClass(message, SenderUID, date.getTime());

            database.getReference().child("chats").child(senderRoom).child("messages").push()
                    .setValue(messagess).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            database.getReference().child("chats").child(reciverRoom).child("messages").push()
                                    .setValue(messagess);
                        }
                    });
        });
    }

    // 🔁 Track chat screen visibility to prevent notification
    @Override
    protected void onResume() {
        super.onResume();
        isChatOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isChatOpen = false;
    }

    // 🔔 Notification method
    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "msg_channel")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.splash_icon) // 👈 Replace with your actual drawable
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.notify(new Random().nextInt(), builder.build());
    }
}
