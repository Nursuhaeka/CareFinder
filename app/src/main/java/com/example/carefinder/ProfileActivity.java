package com.example.carefinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, birthdateTextView;
    private EditText nameEditText, birthdateEditText;
    private ImageView profileImageView;
    private Button uploadImageButton, saveChangesButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Handle result from the image picker
                    Uri imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                    uploadProfileImage(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        nameEditText = findViewById(R.id.nameEditText);  // Initialize the EditText for name
        emailTextView = findViewById(R.id.emailTextView);
        birthdateTextView = findViewById(R.id.birthdateTextView);
        profileImageView = findViewById(R.id.profileImageView);
        birthdateEditText = findViewById(R.id.birthdateEditText);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Enable back button
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);

        // Fetch and display the user data from Firebase
        loadUserProfile();

        // Handle image upload button click
        uploadImageButton.setOnClickListener(v -> openImagePicker());

        // Handle save button click (to save profile changes like name and birthdate)
        saveChangesButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String birthdate = dataSnapshot.child("birthdate").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageBase64").getValue(String.class);

                    // Fetch email directly from Firebase Authentication
                    String email = mAuth.getCurrentUser().getEmail();

                    nameTextView.setText(name);
                    nameEditText.setText(name);  // Set the name in the EditText (for editing)
                    emailTextView.setText(email);  // Display email
                    birthdateTextView.setText(birthdate);  // Display birthdate in TextView
                    birthdateEditText.setText(birthdate);  // Set birthdate in EditText (for editing)

                    // Load profile image
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        // Decode Base64 and set it to the ImageView
                        byte[] imageBytes = android.util.Base64.decode(profileImageUrl, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profileImageView.setImageBitmap(decodedBitmap);
                    } else {
                        profileImageView.setImageResource(R.drawable.default_profile_image);
                    }
                }
            }
        });
    }

    private void openImagePicker() {
        // Open gallery to pick an image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        String userId = mAuth.getCurrentUser().getUid();

        try {
            // Convert the selected image to a Base64 string
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);

            // Save the Base64 string in Firebase Realtime Database
            mDatabase.child("users").child(userId).child("profileImageBase64").setValue(base64Image)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ProfileActivity.this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileChanges() {
        String userId = mAuth.getCurrentUser().getUid();
        String newName = nameEditText.getText().toString().trim();  // Get the new name from EditText
        String newBirthdate = birthdateEditText.getText().toString().trim();  // Get the birthdate from EditText

        // Update the user's name in the database
        if (!newName.isEmpty()) {
            mDatabase.child("users").child(userId).child("name").setValue(newName).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    nameTextView.setText(newName);  // Update displayed name
                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Update the user's birthdate if it has been changed
        if (!newBirthdate.isEmpty()) {
            mDatabase.child("users").child(userId).child("birthdate").setValue(newBirthdate).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    birthdateTextView.setText(newBirthdate);  // Update displayed birthdate
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update birthdate", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
























