package com.example.carefinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        Button buttonGetStarted = findViewById(R.id.buttonGetStarted);

        // Navigate to the Login page on button click
        buttonGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(GetStartedActivity.this, LoginActivity.class));
            finish(); // Close GetStartedActivity so users can't go back to it
        });
    }
}