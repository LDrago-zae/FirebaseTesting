package com.example.firebasetesting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;

import java.util.concurrent.TimeUnit;

public class PhoneNumberActivity extends AppCompatActivity {

    private EditText editTextPhone, editTextOTP;
    private Button buttonVerifyOTP;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_signup); // make sure your XML file matches this name

        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextOTP = findViewById(R.id.editTextOTP);
        Button buttonSendOTP = findViewById(R.id.buttonSendOTP);
        buttonVerifyOTP = findViewById(R.id.buttonVerifyOTP);
        progressBar = findViewById(R.id.progressBar);

        buttonSendOTP.setOnClickListener(v -> sendOTP());
        buttonVerifyOTP.setOnClickListener(v -> verifyOTP());
    }

    private void sendOTP() {
        String phone = editTextPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            editTextPhone.setError("Enter a valid phone number");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phone).setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(callbacks).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            signInWithCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(PhoneNumberActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(id, token);
            verificationId = id;
            progressBar.setVisibility(View.GONE);
            editTextOTP.setVisibility(View.VISIBLE);
            buttonVerifyOTP.setVisibility(View.VISIBLE);
            Toast.makeText(PhoneNumberActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // If user is logged in, go to HomeActivity directly
            startActivity(new Intent(PhoneNumberActivity.this, HomeActivity.class));
            finish();
        }
    }

    private void verifyOTP() {
        String code = editTextOTP.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            editTextOTP.setError("Enter OTP");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Phone number verified!", Toast.LENGTH_SHORT).show();
                // Redirect to LoginActivity
                startActivity(new Intent(PhoneNumberActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid OTP or Verification Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
