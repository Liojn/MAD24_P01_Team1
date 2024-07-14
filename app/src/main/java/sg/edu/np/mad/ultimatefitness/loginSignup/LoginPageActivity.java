package sg.edu.np.mad.ultimatefitness.loginSignup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import sg.edu.np.mad.ultimatefitness.MainActivity;
import sg.edu.np.mad.ultimatefitness.R;
import sg.edu.np.mad.ultimatefitness.calendarPage.BaseActivity;


public class LoginPageActivity extends BaseActivity {

    //public static final int GOOGLE_SIGN_IN_CODE = 1234;
    public static GoogleSignInClient client; // Declare client as static

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText mEmail = findViewById(R.id.inputEmail);
        EditText mPassword = findViewById(R.id.inputLoginPassword);
        Button mSignInBtn = findViewById(R.id.button_signIn);
        TextView mForgotPassword = findViewById(R.id.forgotPassword);

        TextInputLayout mtextInputLoginPassword = findViewById(R.id.textInputLoginPassword);

        ProgressBar mLoginProgressBar = findViewById(R.id.loginProgressBar);
        FirebaseAuth fAuthLogin = FirebaseAuth.getInstance();

        ImageView btnArrow1 = findViewById(R.id.arrow_left1);
        TextView registerNow = findViewById(R.id.registerNow);


        btnArrow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the Main page when the left arrow button is clicked
                Intent intent = new Intent(LoginPageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        registerNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the Main page when the left arrow button is clicked
                Intent intent = new Intent(LoginPageActivity.this, SignUpPageActivity.class);
                startActivity(intent);
            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                View dialogView = inflater.inflate(R.layout.reset_password_dialog, null);

                EditText resetMail = dialogView.findViewById(R.id.resetMail);

                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setView(dialogView);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        if (!mail.isEmpty()) {
                            fAuthLogin.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(LoginPageActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginPageActivity.this, "Error! Reset Link is Not Sent. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(LoginPageActivity.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });

                passwordResetDialog.create().show();
            }
        });

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUsername = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(emailUsername)) {
                    mEmail.setError("Username is Required.");
                    return;
                }

                // Check if the input is a valid email address
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailUsername).matches()) {
                    mEmail.setError("Invalid Email Address");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mtextInputLoginPassword.setEndIconMode(TextInputLayout.END_ICON_NONE); // Hide the end icon
                    mPassword.setError("Password is Required.");
                    return;
                } else {
                    // If there is no error, make the end icon visible
                    mtextInputLoginPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                }


                mLoginProgressBar.setVisibility(View.VISIBLE);

                // authenticate the user
                fAuthLogin.signInWithEmailAndPassword(emailUsername, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginPageActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(LoginPageActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mLoginProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        SignInButton googleSignIn = findViewById(R.id.googleLogo);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this, options);
        googleSignIn.setOnClickListener(view -> {
            // Intent intent = client.getSignInIntent();

            resultLauncher.launch(new Intent(client.getSignInIntent()));
            // startActivityForResult(intent, 1234);
        });
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(LoginPageActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginPageActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}