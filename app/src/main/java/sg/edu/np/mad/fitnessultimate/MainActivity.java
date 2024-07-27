package sg.edu.np.mad.fitnessultimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.HomePage.BannerAdapter;
import sg.edu.np.mad.fitnessultimate.HomePage.BannerItem;
import sg.edu.np.mad.fitnessultimate.calendarPage.CalendarActivity;
import sg.edu.np.mad.fitnessultimate.chatbot.activity.ChatbotActivity;
import sg.edu.np.mad.fitnessultimate.foodtracker.*;
import sg.edu.np.mad.fitnessultimate.loginSignup.LoginOrSignUpOption;
import sg.edu.np.mad.fitnessultimate.loginSignup.ProfilePageActivity;
import sg.edu.np.mad.fitnessultimate.training.TrainingMenuActivity;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private FirebaseUser user;
    private String userId;
    private ImageButton profileBtn;
    ImageView changeProfilePic;
    private TextView welcomeTextView;

    private RecyclerView bannerRecyclerView;
    private BannerAdapter bannerAdapter;


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

        bannerRecyclerView = findViewById(R.id.bannerRecyclerView);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        userId = user != null ? user.getUid() : null;

        welcomeTextView = findViewById(R.id.welcomeTextView);

        profileBtn = findViewById(R.id.profileButton);

        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginOrSignUpOption.class);
            startActivity(intent);
            finish();
            return;
        }

        retrieveAndSetUsername();

        changeProfilePic = findViewById(R.id.profileButton);

        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        String profileImageUri = prefs.getString("profileImageUri", null);
        if (profileImageUri != null) {
            changeProfilePic.setImageURI(Uri.parse(profileImageUri));
        } else {
            retrieveProfileImage();
        }

        if (getIntent().hasExtra("profileImageUri")) {
            String updatedProfileImageUri = getIntent().getStringExtra("profileImageUri");
            changeProfilePic.setImageURI(Uri.parse(updatedProfileImageUri));
        }

        ImageView profileIcon = findViewById(R.id.profileButton);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfilePageActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_fitness).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainingMenuActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.chat_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_calendar).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_food).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FoodTracker.class);
            startActivity(intent);
        });

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        List<BannerItem> itemList = Arrays.asList(
                new BannerItem(R.drawable.discover_excersies, "Discover Exercises", "Click Here!", TrainingMenuActivity.class),
                new BannerItem(R.drawable.benefits_excerise, null, null, null)
        );

        bannerAdapter = new BannerAdapter(this, itemList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        bannerRecyclerView.setLayoutManager(layoutManager);
        bannerRecyclerView.setAdapter(bannerAdapter);
    }

    private void retrieveProfileImage() {
        if (user != null) {
            DocumentReference userRef = fStore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("profileImageUrl");
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .fitCenter()
                            .transform(new CircleCrop())
                            .into(profileBtn);
                } else {
                    Toast.makeText(MainActivity.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Failed to retrieve profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void retrieveAndSetUsername() {
        if (user != null) {
            DocumentReference userRef = fStore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("uName");
                    if (username != null) {
                        String welcomeText = "Welcome back, " + username + "!\nWhat would you like to do today?";
                        welcomeTextView.setText(welcomeText);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Failed to retrieve username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}



