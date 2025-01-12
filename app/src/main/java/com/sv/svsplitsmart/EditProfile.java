package com.sv.svsplitsmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class EditProfile extends AppCompatActivity {

    private EditText nameEditText, emailEditText, contactEditText;
    private ImageView profileImageView;
    private Button saveButton, deleteButton, backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private String userId;

    // Uri to store the selected image
    private Uri imageUri;

    // Request code for selecting image
    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Get the current user ID
        userId = mAuth.getUid();

        // Set up the UI elements
        nameEditText = findViewById(R.id.edit_name);
        emailEditText = findViewById(R.id.edit_email);
        contactEditText = findViewById(R.id.edit_contact);
        profileImageView = findViewById(R.id.edit_profile_image);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        backButton = findViewById(R.id.back_button);

        // Load current user info from Firestore
        loadUserInfo();

        // Handle save button click
        saveButton.setOnClickListener(v -> {
            saveUserInfo();
        });

        // Handle profile image click to change image
        profileImageView.setOnClickListener(v -> openImagePicker());

        // Delete button functionality
        deleteButton.setOnClickListener(view -> deleteAccount());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserInfo() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String contact = documentSnapshot.getString("contact");

                        // Get profileImage as an integer
                        Long profileImageResource = documentSnapshot.getLong("profileImage"); // profileImage stored as a number

                        // Set the user info to the EditText fields
                        nameEditText.setText(name);
                        emailEditText.setText(email);
                        contactEditText.setText(contact);

                        // Load the profile image if it exists
                        if (profileImageResource != null) {
                            int imageResourceId = profileImageResource.intValue();
                            profileImageView.setImageResource(imageResourceId); // Set image from resources
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Error loading user info.", Toast.LENGTH_SHORT).show());
    }


    private void loadImageFromUrl(String imageUrl) {
        StorageReference profileImageRef = storageReference.child("profile_images/" + imageUrl);
        profileImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> profileImageView.setImageURI(uri))
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Error loading profile image", Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onBackPressed() {
        // You can customize this to show a confirmation dialog before exiting
        super.onBackPressed();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveUserInfo() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String contact = contactEditText.getText().toString();

        // If a new image was selected, upload it
        if (imageUri != null) {
            uploadImageToStorage();
        } else {
            // If no new image, proceed to save user info without changing image
            saveProfileData(name, email, contact, null);
        }
    }

    private void uploadImageToStorage() {
        // Create a unique name for the image using the user ID and timestamp (to avoid name conflicts)
        String imageName = "profileImage" + System.currentTimeMillis();  // "profileImage<unique_id>"
        StorageReference fileReference = storageReference.child("profile_images/" + imageName);

        // Convert the image URI to a byte array
        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to Firebase Storage
        fileReference.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the image URL after successful upload
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveProfileData(nameEditText.getText().toString(), emailEditText.getText().toString(), contactEditText.getText().toString(), imageUrl);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Error uploading image", Toast.LENGTH_SHORT).show());
    }

    private void saveProfileData(String name, String email, String contact, String profileImageUrl) {
        // If the profileImageUrl is not null, update it in Firestore, otherwise leave it as is
        if (profileImageUrl != null) {
            db.collection("users").document(userId)
                    .update("name", name, "email", email, "contact", contact, "profileImage", profileImageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        // After saving, navigate to MainActivity
                        Intent intent = new Intent(EditProfile.this, MainActivity.class);
                        startActivity(intent);
                        finish();  // Close the EditProfile activity
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Error updating profile", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("users").document(userId)
                    .update("name", name, "email", email, "contact", contact)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        // After saving, navigate to MainActivity
                        Intent intent = new Intent(EditProfile.this, MainActivity.class);
                        startActivity(intent);
                        finish();  // Close the EditProfile activity
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Error updating profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteAccount() {
        mAuth.getCurrentUser().delete()
                .addOnSuccessListener(aVoid -> {
                    // Delete user data from Firestore
                    db.collection("users").document(userId).delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(EditProfile.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditProfile.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Error deleting account", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Error deleting account", Toast.LENGTH_SHORT).show());
    }
}
