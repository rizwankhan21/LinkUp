package com.azwa.vibesent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class splash extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Your splash screen layout

        // Set white background for status bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));

// Set dark status bar icons (Android 6.0+)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        auth = FirebaseAuth.getInstance();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true); //for caching


        // Adding a delay to show the splash screen for a while (e.g., 2 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (auth.getCurrentUser() == null) {
                // User is not logged in, go to LoginActivity
                startActivity(new Intent(splash.this, login.class));
            } else {
                // User is logged in, go to MainActivity (Chat/Home Screen)
                startActivity(new Intent(splash.this, MainActivity.class));
            }
            finish(); // Close SplashActivity so it doesn't stay in the back stack
        }, 2000); // 2-second delay
    }
}
