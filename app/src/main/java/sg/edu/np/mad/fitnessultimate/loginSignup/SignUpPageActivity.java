package sg.edu.np.mad.fitnessultimate.loginSignup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


import sg.edu.np.mad.fitnessultimate.MainActivity;
import sg.edu.np.mad.fitnessultimate.R;

public class SignUpPageActivity extends AppCompatActivity {
    private static final String TAG = "SignUpPageActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText mUsername = findViewById(R.id.inputUsername);
        EditText mEmail = findViewById(R.id.inputEmail);
        EditText mPassword = findViewById(R.id.inputPassword);
        EditText mConfirmPassword = findViewById(R.id.inputConfirmPassword);
        Button mSignUpButton = findViewById(R.id.button_signUp2);

        ProgressBar mprogressBar = findViewById(R.id.progressBar);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        TextInputLayout mtextInputPassword = findViewById(R.id.textInputPassword);
        TextInputLayout mtextInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);


        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), ProfilePageActivity.class));
            finish();
        }

        ImageView btnArrow2 = findViewById(R.id.arrow_left2);
        TextView login = findViewById(R.id.login);

        btnArrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the Main page when the left arrow button is clicked
                Intent intent = new Intent(SignUpPageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the Main page when the left arrow button is clicked
                Intent intent = new Intent(SignUpPageActivity.this, LoginPageActivity.class);
                startActivity(intent);
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String confirmPassword = mConfirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(username)) {
                    mUsername.setError("Username is Required.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mtextInputPassword.setEndIconMode(TextInputLayout.END_ICON_NONE); // Hide the end icon
                    mPassword.setError("Password is Required.");
                    return;
                }
                else {
                    // If there is no error, make the end icon visible
                    mtextInputPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                }

                if (password.length() < 6) {
                    mtextInputPassword.setEndIconMode(TextInputLayout.END_ICON_NONE); // Hide the end icon
                    mPassword.setError("Password must be more than 6 characters");
                    return;
                } else {
                    // If there is no error, make the end icon visible
                    mtextInputPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                }

                if (!password.equals(confirmPassword)) {
                    mtextInputConfirmPassword.setEndIconMode(TextInputLayout.END_ICON_NONE); // Hide the end icon
                    mConfirmPassword.setError("Password do not match");
                    return;
                }

                mprogressBar.setVisibility(View.VISIBLE);

                // register the user in Firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpPageActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                            String userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("uName",username);
                            user.put("email", email);
                            documentReference.set(user).addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "onSuccess: userProfile is created for " + userID);
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });

                            startActivity(new Intent(getApplicationContext(), ProfilePageActivity.class));
                        }
                        else {
                            Toast.makeText(SignUpPageActivity.this, "Error! " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            mprogressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}