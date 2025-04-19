package com.example.firebasetesting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextPhone, editTextOtp;
    private Button loginButton, verifyOtpButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private String verificationId; // Store verification ID for OTP

    private enum LoginMethod { NONE, EMAIL_PASSWORD, PHONE }
    private LoginMethod currentLoginMethod = LoginMethod.NONE;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            goToHomeActivity();
        }

        // Initialize Views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextOtp = findViewById(R.id.editTextOtp);
        loginButton = findViewById(R.id.buttonLogin);
        verifyOtpButton = findViewById(R.id.buttonVerifyOtp);
        progressBar = findViewById(R.id.progressBar);


        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonUseEmail = findViewById(R.id.buttonSwitchToEmail);
        Button buttonUsePhone = findViewById(R.id.buttonSwitchToPhone);
        Button googleLogin = findViewById(R.id.googleLogin);

        // Set initial visibility
        editTextEmail.setVisibility(View.VISIBLE);
        editTextPassword.setVisibility(View.VISIBLE);
        editTextPhone.setVisibility(View.GONE);
        editTextOtp.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
        setLoginMethodVisibility(View.VISIBLE, View.VISIBLE, View.GONE);
        currentLoginMethod = LoginMethod.EMAIL_PASSWORD;
        // Set click listeners



        buttonUseEmail.setOnClickListener(v -> {
            currentLoginMethod = LoginMethod.EMAIL_PASSWORD;
            setLoginMethodVisibility(View.VISIBLE, View.VISIBLE, View.GONE);
            editTextOtp.setVisibility(View.GONE);
            verifyOtpButton.setVisibility(View.GONE);
        });

        buttonUsePhone.setOnClickListener(v -> {
            currentLoginMethod = LoginMethod.PHONE;
            setLoginMethodVisibility(View.GONE, View.GONE, View.VISIBLE);
            editTextOtp.setVisibility(View.GONE);
            verifyOtpButton.setVisibility(View.GONE);
        });

        loginButton.setOnClickListener(v -> {
            if (currentLoginMethod == LoginMethod.EMAIL_PASSWORD) {
                loginUserWithEmail();
            } else if (currentLoginMethod == LoginMethod.PHONE) {
                initiatePhoneVerification();
            }
        });

        verifyOtpButton.setOnClickListener(v -> verifyPhoneOtp());

        buttonRegister.setOnClickListener(v -> goToSignUpActivity());

        credentialManager = CredentialManager.create(this);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest googleSignInRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        googleLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            CancellationSignal cancellationSignal = new CancellationSignal();
            credentialManager.getCredentialAsync(
                    this,
                    googleSignInRequest,
                    cancellationSignal,
                    Executors.newSingleThreadExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse response) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                try {
                                    Credential credential = response.getCredential();
                                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                                    firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
                                } catch (Exception e) {
                                    Log.e("LoginActivity", "Google Sign-In error", e);
                                    Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e("LoginActivity", "Google Sign-In error", e);
                                Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
            );
        });
    }

    private void setLoginMethodVisibility(int emailVisibility, int passwordVisibility, int phoneVisibility) {
        editTextEmail.setVisibility(emailVisibility);
        editTextPassword.setVisibility(passwordVisibility);
        editTextPhone.setVisibility(phoneVisibility);
        loginButton.setVisibility(View.VISIBLE);
    }

    private void loginUserWithEmail() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                goToHomeActivity();
            } else {
                Toast.makeText(LoginActivity.this, "Login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initiatePhoneVerification() {
        String phone = editTextPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        // Basic phone number validation (e.g., ensure it starts with "+" and has enough digits)
        if (!phone.startsWith("+") || phone.length() < 10) {
            editTextPhone.setError("Enter a valid phone number with country code (e.g., +1234567890)");
            editTextPhone.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Initiate phone authentication
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto-verification or instant verification completed
                        progressBar.setVisibility(View.GONE);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Phone verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("LoginActivity", "Phone verification failed", e);
                        });
                    }

                    @Override
                    public void onCodeSent(@NonNull String verId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            verificationId = verId;
                            editTextOtp.setVisibility(View.VISIBLE);
                            verifyOtpButton.setVisibility(View.VISIBLE);
                            loginButton.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "OTP sent to " + phone, Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneOtp() {
        String otp = editTextOtp.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            editTextOtp.setError("OTP is required");
            editTextOtp.requestFocus();
            return;
        }

        if (otp.length() != 6) {
            editTextOtp.setError("Enter a valid 6-digit OTP");
            editTextOtp.requestFocus();
            return;
        }

        if (verificationId == null) {
            Toast.makeText(this, "Verification ID not found. Please request OTP again.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Phone login successful", Toast.LENGTH_SHORT).show();
                goToHomeActivity();
            } else {
                Toast.makeText(LoginActivity.this, "Phone login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Phone login failed", task.getException());
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show();
                goToHomeActivity();
            } else {
                Toast.makeText(this, "Firebase Authentication failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Google Sign-In failed", task.getException());
            }
        }));
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToSignUpActivity() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}