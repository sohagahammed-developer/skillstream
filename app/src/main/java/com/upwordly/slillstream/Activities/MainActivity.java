package com.upwordly.slillstream.Activities;

import android.content.Intent;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.GetCredentialException;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.upwordly.slillstream.R;

public class MainActivity extends AppCompatActivity {


     CredentialManager manager;
     GetCredentialRequest request;
     FirebaseAuth mAuth;
     GetSignInWithGoogleOption googleOption;
    TextInputEditText nameEditText;
    TextInputLayout textlayout;
    TextInputEditText emailEditText;
    TextInputEditText passwordEditText;
    Button btn_action;
    TextView loginButton;
    TextView button_authentication, titleText, txt_alreadyHave;
    LinearLayout layout;
    ImageView continewWithGoogle;
    ProgressBar progressBar;
    TextView forgot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameEditText = findViewById(R.id.name_txt);
        emailEditText = findViewById(R.id.email_txt);
        passwordEditText = findViewById(R.id.password_txt);
        btn_action = findViewById(R.id.btn_action);
        loginButton = findViewById(R.id.button_authentication);
        button_authentication = findViewById(R.id.button_authentication);
        titleText = findViewById(R.id.title);
        textlayout = findViewById(R.id.textlayout);
        txt_alreadyHave = findViewById(R.id.txt_alreadyHave);
        layout = findViewById(R.id.layout);
        continewWithGoogle = findViewById(R.id.continewWithGoogle);
        progressBar = findViewById(R.id.progressBar);
        forgot = findViewById(R.id.forgot);

        manager = CredentialManager.create(this);
        mAuth = FirebaseAuth.getInstance();

        button_authentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (textlayout.getVisibility() == View.VISIBLE) {
                    // Switch to Login
                    textlayout.setVisibility(View.GONE);
                    titleText.setText("Welcome Back");
                    btn_action.setText("Login");
                    txt_alreadyHave.setText("Don't have an account?");
                    button_authentication.setText("Sign Up");

                } else {
                    // Switch to Sign Up
                    textlayout.setVisibility(View.VISIBLE);
                    titleText.setText("Create Your Account");
                    btn_action.setText("Create Account");
                    txt_alreadyHave.setText("Already have an account?");
                    button_authentication.setText("Login");
                }

                _TransitionManager(layout, 500);
            }
        });

        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (password.length() < 6) {
                    Toast.makeText(MainActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (textlayout.getVisibility() == View.VISIBLE) {
                    SignUp(name,email,password);
                } else {
                    Login(email,password);
                }
            }
        });

        googleOption = new GetSignInWithGoogleOption.Builder(getString(R.string.ClientId))
                .setNonce(java.util.UUID.randomUUID().toString())
                .build();

        request = new androidx.credentials.GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();
        continewWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGoogleLogin();
            }
        });

        forgot.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }


    void SignUp ( String name,String email,String password) {


        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null) {

                            // Save name
                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                            user.updateProfile(profileUpdates);

                            // Send verification email
                            user.sendEmailVerification()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(
                                                MainActivity.this,
                                                "Verification email sent. Please verify before login.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        FirebaseAuth.getInstance().signOut();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(
                                                    MainActivity.this,
                                                    "Failed to send verification email",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                        }
                    }
                    else {
                        // Check if email already exists
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(MainActivity.this, "Email already exists. Please login.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void Login (String email,String password) {


        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null && user.isEmailVerified()) {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(
                                    MainActivity.this,
                                    "Please verify your email first",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                    else {
                        // Login failed
                        Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void requestGoogleLogin () {

        progressBar.setVisibility(View.VISIBLE);

        manager.getCredentialAsync(this, request, null, Runnable::run,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        Credential credential = getCredentialResponse.getCredential();
                        GoogleIdTokenCredential idTokenCredential =
                                GoogleIdTokenCredential.createFrom(credential.getData());
                        String idToken = idTokenCredential.getIdToken();

                        if (idToken == null) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Id" +
                                    " Token is null", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);

                        mAuth.signInWithCredential(authCredential)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                        finish();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "Sign In Failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onError(@NonNull androidx.credentials.exceptions.GetCredentialException e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Google Sign-In Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your registered email");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordReset(email);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void sendPasswordReset(String email) {
        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset link sent to your email.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    public void _TransitionManager(final View _view, final double _duration) {
        LinearLayout viewgroup = (LinearLayout) _view;

        android.transition.AutoTransition autoTransition = new android.transition.AutoTransition();
        autoTransition.setDuration((long) _duration);
        android.transition.TransitionManager.beginDelayedTransition(viewgroup, autoTransition);
    }
}