package com.example.carefinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;  // Import ImageButton for logout icon
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView textViewGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textViewGreeting = findViewById(R.id.textViewGreeting); // Initialize textView

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in. Redirecting to login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Fetch the user's name
        fetchUserName();

        // Find buttons by ID
        Button buttonMap = findViewById(R.id.buttonMap);
        Button buttonAboutUs = findViewById(R.id.buttonAboutUs);
        Button buttonProfile = findViewById(R.id.buttonProfile); // Add Profile button
        ImageButton logoutButton = findViewById(R.id.logoutButton);  // Initialize logout button

        // Navigate to MainActivity (Google Maps) when "Find Hospital" button is clicked
        buttonMap.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class); // MainActivity contains Google Maps
            startActivity(intent);
        });

        // Navigate to AboutUsActivity when "About Us" button is clicked
        buttonAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });

        // Navigate to ProfileActivity when "Profile" button is clicked
        buttonProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class); // Navigate to ProfileActivity
            startActivity(intent);
        });

        // Handle Logout functionality with confirmation
        logoutButton.setOnClickListener(v -> {
            // Show confirmation dialog before logging out
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to log out?")
                    .setCancelable(false) // Prevent dismissing dialog by tapping outside
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked Yes, log out
                            mAuth.signOut();
                            Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                            finish();  // Close the HomeActivity
                        }
                    })
                    .setNegativeButton("No", null)  // No button dismisses the dialog
                    .show();
        });
    }

    private void fetchUserName() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String userName = task.getResult().child("name").getValue(String.class);
                textViewGreeting.setText("Hello, " + (userName != null ? userName : "User") + "!");
            } else {
                textViewGreeting.setText("Hello, User!");
            }
        }).addOnFailureListener(e -> {
            textViewGreeting.setText("Hello, User!");
            Toast.makeText(HomeActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
        });
    }
}
