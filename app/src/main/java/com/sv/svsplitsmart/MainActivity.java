package com.sv.svsplitsmart;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.sv.svsplitsmart.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up FAB action
        binding.appBarMain.fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab)
                        .show()
        );

        // Set up DrawerLayout and NavigationView
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configure AppBar and Navigation Controller
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Fetch and display user info
        displayUserInfo();

        // Set up logout button
        Log.d("MainActivity", "77 Setting up logout button...");
        setupLogoutButton();
        Log.d("MainActivity", "79 Setting up logout button...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Setting up logout button...");
        setupLogoutButton(); // Ensure the logout button setup happens every time the activity resumes
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void displayUserInfo() {
        String userId = FirebaseAuth.getInstance().getUid(); // Get the current user's UID
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance

        if (userId != null) {
            // Reference to the user's document
            DocumentReference docRef = db.collection("users").document(userId);

            // Fetch the document
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Retrieve fields from the document
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String contact = document.getString("contact");
                        long profileImage = document.getLong("profileImage"); // Assuming profileImage is a number

                        // Update the navigation header UI
                        NavigationView navigationView = binding.navView;
                        View headerView = navigationView.getHeaderView(0);
                        TextView nameTextView = headerView.findViewById(R.id.nav_header_name);
                        TextView emailTextView = headerView.findViewById(R.id.nav_header_email);
                        TextView contactTextView = headerView.findViewById(R.id.nav_header_contact);
                        ImageView profileImageView = headerView.findViewById(R.id.nav_header_image);

                        if (nameTextView != null) {
                            nameTextView.setText(name != null ? name : "No Name");
                        }
                        if (emailTextView != null) {
                            emailTextView.setText(email != null ? email : "No Email");
                        }
                        if (contactTextView != null) {
                            contactTextView.setText(contact != null ? contact : "No Contact");
                        }
                        if (profileImageView != null) {
                            profileImageView.setImageResource((int) profileImage); // Cast long to int
                        }
                    } else {
                        Log.d("Firestore", "No such document exists.");
                    }
                } else {
                    Log.e("Firestore", "Error fetching document: ", task.getException());
                }
            });
        } else {
            Log.e("FirebaseAuth", "User ID is null.");
        }
    }

    private void setupLogoutButton() {
        // Find the logout button directly in the main layout
        Log.d("MainActivity", "146 Setting up logout button...");
        Button logoutButton = findViewById(R.id.logout_button); // Ensure correct ID

        Log.d("MainActivity", "150 before Setting up logout button...");

        if (logoutButton != null) {
            logoutButton.setOnClickListener(view -> {
                Log.d("MainActivity", "Logout button clicked");

                // Perform logout
                performLogout();
            });
        } else {
            Log.e("MainActivity", "Logout button is null.");
        }
    }

    private void performLogout() {
        Log.d("MainActivity", "Performing logout...");

        // Firebase logout
        mAuth.signOut();

        // Show confirmation or redirect to login
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close MainActivity
    }
}
