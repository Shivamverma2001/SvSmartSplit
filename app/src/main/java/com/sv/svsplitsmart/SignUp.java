package com.sv.svsplitsmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private ImageView profileImageView;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");

        // Initialize UI elements
        EditText nameInput = findViewById(R.id.signupNameInput);
        EditText emailInput = findViewById(R.id.signupEmailInput);
        EditText contactInput = findViewById(R.id.signupContactInput);
        EditText passwordInput = findViewById(R.id.signupPasswordInput);
        EditText confirmPasswordInput = findViewById(R.id.signupConfirmPasswordInput);
        Button signupButton = findViewById(R.id.signupButton);
        Button uploadImageButton = findViewById(R.id.signupImageButton);
        profileImageView = findViewById(R.id.signupImageView);
        TextView loginRedirect = findViewById(R.id.loginRedirect);

        // Image upload functionality
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        profileImageView.setImageURI(imageUri);
                    }
                });

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Sign-Up functionality
        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String contact = contactInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (validateForm(name, email, contact, password, confirmPassword)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    saveUserData(user.getUid(), name, email, contact);
                                }
                            } else {
                                Toast.makeText(SignUp.this, "Sign-Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Redirect to Login
        loginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateForm(String name, String email, String contact, String password, String confirmPassword) {
        if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserData(String uid, String name, String email, String contact) {
        if (imageUri != null) {
            // Upload profile image to Firebase Storage
            StorageReference fileReference = storageReference.child(uid + ".jpg");

            try {
                // Get InputStream from the image URI
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    // Upload the image from the InputStream
                    fileReference.putStream(inputStream)
                            .addOnSuccessListener(taskSnapshot -> {
                                // Get the download URL of the image after successful upload
                                fileReference.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            // Store user data with image URL
                                            saveUserToFirestore(uid, name, email, contact, uri.toString());
                                        })
                                        .addOnFailureListener(e -> {
                                            // Upload dummy image if the user's image upload fails
                                            uploadDummyImage(uid, name, email, contact);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                // Upload dummy image if upload fails
                                uploadDummyImage(uid, name, email, contact);
                            });
                }
            } catch (Exception e) {
                // Upload dummy image if an error occurs
                uploadDummyImage(uid, name, email, contact);
            }
        } else {
            // Store user data without image URL
            uploadDummyImage(uid, name, email, contact);
        }
    }

    private void uploadDummyImage(String uid, String name, String email, String contact) {
        // Load the dummy image from drawable resources
        @SuppressLint("ResourceType") InputStream dummyImageStream = getResources().openRawResource(R.drawable.smartsplit_drak_mode);

        // Upload dummy image to Firebase Storage
        StorageReference fileReference = storageReference.child(uid + "smartsplit.jpg");

        fileReference.putStream(dummyImageStream)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the dummy image
                    fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Store user data with the dummy image URL
                                saveUserToFirestore(uid, name, email, contact, uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                // Handle error retrieving download URL
                                Toast.makeText(SignUp.this, "Failed to retrieve dummy image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                saveUserToFirestore(uid, name, email, contact, null);
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle failure during dummy image upload
                    Toast.makeText(SignUp.this, "Failed to upload dummy image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveUserToFirestore(uid, name, email, contact, null);
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String contact, String imageUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("contact", contact);

        // Use the image URL if available, otherwise, use the dummy image URL
        if (imageUrl != null) {
            user.put("profileImage", imageUrl);
        } else {
            user.put("profileImage", R.drawable.smartsplit_drak_mode);
        }

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(SignUp.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUp.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to save user data: " + e.getMessage());
                    Toast.makeText(SignUp.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                });
    }
}
