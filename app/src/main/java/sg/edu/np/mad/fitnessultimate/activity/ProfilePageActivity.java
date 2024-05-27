package sg.edu.np.mad.fitnessultimate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        String userId = fAuth.getCurrentUser().getUid();

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