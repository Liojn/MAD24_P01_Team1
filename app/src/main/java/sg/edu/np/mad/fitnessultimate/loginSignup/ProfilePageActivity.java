package sg.edu.np.mad.fitnessultimate.loginSignup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import sg.edu.np.mad.fitnessultimate.MainActivity;
import sg.edu.np.mad.fitnessultimate.R;

public class ProfilePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView username = findViewById(R.id.yourUsername);
        TextView email = findViewById(R.id.yourEmailAddress);
        ImageView backArrow = findViewById(R.id.arrow_left1);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        String userId = fAuth.getCurrentUser().getUid();

        // Change Password
        RelativeLayout resetPassword = findViewById(R.id.changePassword);
        FirebaseUser user = fAuth.getCurrentUser();

        // Edit Profile
        Button editProfileBtn = findViewById(R.id.editProfile);

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) { // Check if the document exists
                    username.setText(value.getString("uName"));
                    email.setText(value.getString("email"));
                }
            }
        });

        // Go back to home page
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Edit Profile
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePageActivity.this, EditProfilePageActivity.class);
                startActivity(intent);
            }
        });

        // Change Password
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the custom layout
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                View dialogView = inflater.inflate(R.layout.change_password_dialog, null);

                // Get the EditText from the custom layout
                final EditText resetPassword = dialogView.findViewById(R.id.dialog_input);

                // Create the AlertDialog and set the custom view
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                //passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setView(dialogView);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Extract the new password and attempt to update it
                        String newPassword = resetPassword.getText().toString();
                        if (newPassword.length() <= 6) {
                            Toast.makeText(ProfilePageActivity.this, "Password must be longer than 6 characters.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfilePageActivity.this, "Password Reset Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfilePageActivity.this, "Password Reset Failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the dialog
                    }
                });

                passwordResetDialog.create().show();
            }
        });
    }
    public void logout(View view) {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Sign out from Google
        if (LoginPageActivity.client != null) {
            // Clear any existing sessions or cached credentials
            LoginPageActivity.client.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // After revoking access, sign out from Google
                        LoginPageActivity.client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // After signing out from Google, navigate to login page
                                startActivity(new Intent(ProfilePageActivity.this, MainActivity.class));
                                finish();
                            }
                        });
                    } else {
                        // Handle case where revoking access failed
                        // Redirect to login page
                        startActivity(new Intent(ProfilePageActivity.this, LoginPageActivity.class));
                        finish();
                    }
                }
            });
        } else {
            // Handle case where GoogleSignInClient is not initialized
            // This may happen if the user did not sign in with Google
            // Redirect to login page
            startActivity(new Intent(ProfilePageActivity.this, LoginPageActivity.class));
            finish();
        }
    }

}