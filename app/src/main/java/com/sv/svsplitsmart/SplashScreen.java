package com.sv.svsplitsmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        // Delay for 2 seconds
        new Handler().postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // User already logged in, go to MainActivity
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
            } else {
                // User not logged in, go to LoginActivity
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
            }
            finish();
        }, 2000); // Splash screen duration (2 seconds)
    }
}