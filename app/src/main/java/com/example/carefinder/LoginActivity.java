package com.example.carefinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private DatabaseReference mDatabase;
    private android.util.Log Log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegisterLink = findViewById(R.id.textViewRegisterLink); // Register link

        buttonLogin.setOnClickListener(v -> loginUser());

        // Handle click on "Register here" text
        textViewRegisterLink.setOnClickListener(v -> navigateToRegisterPage());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Enter email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Enter password");
            editTextPassword.requestFocus();
            return;
        }

        // Capture the User-Agent when login is initiated
        String userAgent = System.getProperty("http.agent");
        Toast.makeText(this, "User-Agent: " + userAgent, Toast.LENGTH_SHORT).show();  // Debugging log for user-agent

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Once login is successful, store the user-agent in Firebase under user data
                        String userId = mAuth.getCurrentUser().getUid();
                        storeUserAgent(userId, userAgent);

                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeUserAgent(String userId, String userAgent) {
        // Store the user-agent in Firebase Realtime Database under the user data
        mDatabase.child("users").child(userId).child("userAgent").setValue(userAgent)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("UserAgent", "User-Agent stored successfully.");
                    } else {
                        Log.d("UserAgent", "Failed to store User-Agent: " + task.getException().getMessage());
                    }
                });
    }

    private void navigateToRegisterPage() {
        // Navigate to the RegisterActivity
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}
