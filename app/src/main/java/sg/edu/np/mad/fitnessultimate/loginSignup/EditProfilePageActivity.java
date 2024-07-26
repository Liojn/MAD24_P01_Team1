package sg.edu.np.mad.fitnessultimate.loginSignup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.fitnessultimate.calendarPage.BaseActivity;
import sg.edu.np.mad.fitnessultimate.R;

public class EditProfilePageActivity extends BaseActivity {
    ImageView leftArrow;
    ImageView changeProfilePic;
    EditText resetUsername;
    EditText resetEmail;

    Button saveButton;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;
    Uri imageUri;

    private ActivityResultLauncher<String> galleryLauncher;
    // Declare global variables to store original values
    private String originalUsername;
    private String originalEmail;

    // Method to retrieve the profile image URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views and Firebase instances after setting the content view
        leftArrow = findViewById(R.id.arrow_left1);
        changeProfilePic = findViewById(R.id.editProfilePic);
        resetUsername = findViewById(R.id.inputUsername);
        resetEmail = findViewById(R.id.inputEmail);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference();
        saveButton = findViewById(R.id.saveBtn);

        Intent data = getIntent();
        String username = data.getStringExtra("username");
        String email = data.getStringExtra("email");

        resetUsername.setText(username);
        resetEmail.setText(email);
        // Retrieve image URL from Firestore and load image into ImageView
        retrieveProfileImage();

        // Retrieve original values of username and email
        originalUsername = resetUsername.getText().toString();
        originalEmail = resetEmail.getText().toString();

        // Disable save button initially if no changes
        saveButton.setEnabled(false);
        saveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabledButtonColor));

        // Add TextChangedListener to EditText fields to enable/disable save button based on changes
        resetUsername.addTextChangedListener(textWatcher);
        resetEmail.addTextChangedListener(textWatcher);

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the Main page when the left arrow button is clicked
                Intent intent = new Intent(EditProfilePageActivity.this, ProfilePageActivity.class);
                startActivity(intent);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve text from EditText fields
                String usernameText = resetUsername.getText().toString();
                String emailText = resetEmail.getText().toString();

                // Check if any field is empty
                if (TextUtils.isEmpty(usernameText) || TextUtils.isEmpty(emailText)) {
                    Toast.makeText(EditProfilePageActivity.this, "One or Many Fields are Empty.", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution
                }

                // Check if the new email is different from the current email
                boolean isEmailEdited = !user.getEmail().equals(emailText);

                if (isEmailEdited) {
                    user.verifyBeforeUpdateEmail(emailText).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> emailTask) {
                            if (emailTask.isSuccessful()) {
                                // Email verification sent to new email
                                Toast.makeText(EditProfilePageActivity.this, "Verification email sent to " + emailText + ". Please verify to complete the update.", Toast.LENGTH_SHORT).show();
                                // No need to call sendEmailVerification() here

                                // Inform the user to check their new email and complete the verification
                                // The actual email update will be handled by Firebase upon email verification
                                updateProfile(usernameText, emailText, true);
                                startActivity(new Intent(EditProfilePageActivity.this, ProfilePageActivity.class));
                                finish(); // Finish current activity
                            } else {
                                // Error sending verification email
                                Toast.makeText(EditProfilePageActivity.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    updateProfile(usernameText, emailText, true);
                    startActivity(new Intent(EditProfilePageActivity.this, ProfilePageActivity.class));
                    finish(); // Finish current activity
                }
            }
        });


        // Initialize the ActivityResultLauncher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    // Handle the selected image URI
                    changeProfilePic.setImageURI(result);
                    Toast.makeText(EditProfilePageActivity.this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();

                    // Load the new image with Glide and apply circleCrop transformation
                    Glide.with(EditProfilePageActivity.this)
                            .load(result)
                            .circleCrop()
                            .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image
                            .into(changeProfilePic);

                    // Upload the new image to Firebase Storage and update profile in Firestore
                    uploadImageToFirebase(result);
                }
            }
        });

        // Set OnClickListener for changing profile picture
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery to select a profile picture
                galleryLauncher.launch("image/*");
            }
        });
    }


    // TextWatcher to listen for changes in EditText fields
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Check if EditText fields are modified
            if (!resetUsername.getText().toString().equals(originalUsername) ||
                    !resetEmail.getText().toString().equals(originalEmail)) {
                // Enable save button if there are changes and set blue background
                saveButton.setEnabled(true);
                saveButton.setBackgroundColor(ContextCompat.getColor(EditProfilePageActivity.this, R.color.enabledButtonColor));
            } else {
                // Disable save button if no changes and set grey background
                saveButton.setEnabled(false);
                saveButton.setBackgroundColor(ContextCompat.getColor(EditProfilePageActivity.this, R.color.disabledButtonColor));
            }
        }

    };
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Update the user profile in Firestore with the image URL
                        updateProfileImageInFirestore(downloadUri.toString());
                        Toast.makeText(EditProfilePageActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(EditProfilePageActivity.this, "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfilePageActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void updateProfileImageInFirestore(String imageUrl) {
        // Update the 'profileImageUrl' field in Firestore with the image URL
        DocumentReference docRef = fStore.collection("users").document(user.getUid());
        docRef.update("profileImageUrl", imageUrl)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Profile image URL updated successfully in Firestore
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update profile image URL in Firestore
                    }
                });
    }

    private void retrieveProfileImage() {
        if (user != null) {
            // Retrieve the user document from Firestore
            DocumentReference userRef = fStore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Extract the image URL and email from the user document
                    String imageUrl = documentSnapshot.getString("profileImageUrl");
                    String email = documentSnapshot.getString("email");
                    String username = documentSnapshot.getString("uName");

                    // Load the image into ImageView using Glide
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image while loading
                            .fitCenter()
                            .transform(new CircleCrop())
                            .into(changeProfilePic);

                    // Update the EditText fields with the retrieved values
                    resetUsername.setText(username);
                    resetEmail.setText(email);
                }
            });
        }
    }
    //updating user information in Firestore
    private void updateEmailInFirestore(String newEmail, boolean isEmailEdited) {
        if (isEmailEdited) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
                userRef.update("email", newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Email updated successfully in Firestore
                        } else {
                            // Error updating email in Firestore
                        }
                    }
                });
            }
        }
    }

    private void updateProfile(String usernameText, String emailText, boolean isEmailEdited) {
        DocumentReference docRef = fStore.collection("users").document(user.getUid());
        Map<String, Object> edited = new HashMap<>();
        edited.put("email", emailText);
        edited.put("uName", usernameText);
        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(EditProfilePageActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), ProfilePageActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfilePageActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}