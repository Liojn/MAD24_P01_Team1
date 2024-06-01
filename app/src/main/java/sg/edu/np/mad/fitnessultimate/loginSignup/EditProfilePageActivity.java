package sg.edu.np.mad.fitnessultimate.loginSignup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import sg.edu.np.mad.fitnessultimate.R;

public class EditProfilePageActivity extends AppCompatActivity {
    ImageView leftArrow;
    ImageView changeProfilePic;
    EditText resetUsername;
    EditText resetEmail;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private ActivityResultLauncher<String> galleryLauncher;

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

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the Main page when the left arrow button is clicked
                Intent intent = new Intent(EditProfilePageActivity.this, ProfilePageActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the gallery launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        // Handle the picked image URI here
                        if (result != null) {
                            // Do something with the picked image URI
                            changeProfilePic.setImageURI(result);
                        }
                    }
                });

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery using the activity result launcher
                galleryLauncher.launch("image/*");
            }
        });
    }
}