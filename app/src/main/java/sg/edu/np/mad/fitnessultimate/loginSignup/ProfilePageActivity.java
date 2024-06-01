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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import sg.edu.np.mad.fitnessultimate.MainActivity;
import sg.edu.np.mad.fitnessultimate.R;

public class ProfilePageActivity extends AppCompatActivity {

    private TextView username;
    private TextView email;
    private ImageView backArrow;
    private ImageView profilePicture;
    private RelativeLayout resetPassword;
    private Button editProfileBtn;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private FirebaseUser user;
    private String userId;

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

        // Initialize views
        username = findViewById(R.id.yourUsername);
        email = findViewById(R.id.yourEmailAddress);
        backArrow = findViewById(R.id.arrow_left1);
        profilePicture = findViewById(R.id.profilePic);
        resetPassword = findViewById(R.id.changePassword);
        editProfileBtn = findViewById(R.id.editProfile);

        // Initialize Firebase instances
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        userId = user != null ? user.getUid() : null;

        // Retrieve and display the profile image
        retrieveProfileImage();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null && value.exists()) { // Check if the document exists
                    String usernameText = value.getString("uName");
                    String emailText = value.getString("email");
                    username.setText(usernameText);
                    email.setText(emailText);
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
                // Retrieve the current username and email
                String currentUsername = username.getText().toString();
                String currentEmail = email.getText().toString();

                // Start EditProfilePageActivity and pass the current username and email as extras
                Intent intent = new Intent(ProfilePageActivity.this, EditProfilePageActivity.class);
                intent.putExtra("username", currentUsername);
                intent.putExtra("email", currentEmail);
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
                        if (newPassword.length() < 6) {
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
    private void retrieveProfileImage() {
        if (user != null) {
            // Retrieve the user document from Firestore
            DocumentReference userRef = fStore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Extract the image URL from the user document
                    String imageUrl = documentSnapshot.getString("profileImageUrl");

                    // Load the image into ImageView using Glide
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image while loading
                            .fitCenter()
                            .transform(new CircleCrop())
                            .into(profilePicture);
                } else {
                    // Handle case where user document does not exist
                    Toast.makeText(ProfilePageActivity.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                // Handle any errors that occur while fetching user document
                Toast.makeText(ProfilePageActivity.this, "Failed to retrieve profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Method to retrieve user information from Firestore
    private void retrieveUserInfoFromFirestore(String email) {
        fStore.collection("users").whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Retrieve user information from Firestore
                        String username = document.getString("username");
                        String profilePictureUrl = document.getString("profilePictureUrl");

                        // Update UI with retrieved information
                        updateUI(username, email, profilePictureUrl);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    // Method to update UI with user information
    private void updateUI(String usernameText, String emailText, String profilePictureUrl) {
        // Update TextViews for username and email
        username.setText(usernameText);
        email.setText(emailText);

        // Load profile picture using Glide or any other image loading library
        Glide.with(this)
                .load(profilePictureUrl)
                .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image while loading
                .into(profilePicture);
    }

    // Call retrieveUserInfoFromFirestore method in the login process
    private void login(String email, String password) {
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, retrieve user information from Firestore
                        retrieveUserInfoFromFirestore(email);
                    } else {
                        // Handle login failure
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}