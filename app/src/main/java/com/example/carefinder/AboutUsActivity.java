package com.example.carefinder;

import android.content.Intent;
import android.net.Uri; // Import Uri class
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow); // Set back icon
        }

        // Handle Back Button Click - Navigates to HomeActivity
        toolbar.setNavigationOnClickListener(v -> navigateToHome());
    }

    // Custom Method to Navigate to HomeActivity
    private void navigateToHome() {
        Intent intent = new Intent(AboutUsActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Ensures no duplicate instances
        startActivity(intent);
        finish(); // Close current activity
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHome(); // Call the custom method when back is pressed
    }

    // Method to open the GitHub page when the link is clicked
    public void openGitHubPage(android.view.View view) {
        Uri uri = Uri.parse("https://github.com/Nursuhaeka/CareFinder.git");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent); // Open the GitHub page
    }
}
