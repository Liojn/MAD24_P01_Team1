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
import sg.edu.np.mad.fitnessultimate.foodtracker.FoodTracker;
import sg.edu.np.mad.fitnessultimate.loginSignup.LoginOrSignUpOption;
import sg.edu.np.mad.fitnessultimate.loginSignup.ProfilePageActivity;
import sg.edu.np.mad.fitnessultimate.training.TrainingMenuActivity;
import sg.edu.np.mad.fitnessultimate.waterTracker.water.WaterTrackingActivity;

import androidx.recyclerview.widget.PagerSnapHelper;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private FirebaseUser user;
    private String userId;
    private ImageButton profileBtn;
    private ImageView changeProfilePic;
    private TextView welcomeTextView;
    private RecyclerView bannerRecyclerView;
    private BannerAdapter bannerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge UI mode for a more immersive experience
        EdgeToEdge.enable(this);

        // Set the content view to the activity's main layout
        setContentView(R.layout.activity_main);

        // Adjust window insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase authentication and Firestore instances
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Get the currently logged in user
        user = fAuth.getCurrentUser();
        userId = user != null ? user.getUid() : null;

        // Initialize views
        welcomeTextView = findViewById(R.id.welcomeTextView);
        profileBtn = findViewById(R.id.profileButton);
        bannerRecyclerView = findViewById(R.id.bannerRecyclerView);
        changeProfilePic = findViewById(R.id.profileButton);

        // Check if the user is not logged in and redirect to login/signup page if needed
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginOrSignUpOption.class);
            startActivity(intent);
            finish();
            return;
        }

        // Retrieve and set the user's username in the welcome message
        retrieveAndSetUsername();

        // Load the profile image from shared preferences or Firebase
        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        String profileImageUri = prefs.getString("profileImageUri", null);
        if (profileImageUri != null) {
            changeProfilePic.setImageURI(Uri.parse(profileImageUri));
        } else {
            retrieveProfileImage();
        }

        // Update the profile image if a new one is provided through the intent
        if (getIntent().hasExtra("profileImageUri")) {
            String updatedProfileImageUri = getIntent().getStringExtra("profileImageUri");
            changeProfilePic.setImageURI(Uri.parse(updatedProfileImageUri));
        }

        // Set click listener for profile icon to navigate to the profile page
        ImageView profileIcon = findViewById(R.id.profileButton);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfilePageActivity.class);
            startActivity(intent);
        });

        // Set click listeners for navigation buttons to navigate to respective activities
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

        findViewById(R.id.nav_full_width).setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, WaterTrackingActivity.class);
            startActivity(intent);
        });

        // Initialize banner items for the banner RecyclerView
        List<BannerItem> itemList = Arrays.asList(
                new BannerItem(R.drawable.discover_excersies, "Discover Exercises", null, TrainingMenuActivity.class),
                new BannerItem(R.drawable.why_excersie, "Benefits of Exercise", null, null),
                new BannerItem(R.drawable.water_facts, "Water Facts", null, null)
        );

        // Set up the banner RecyclerView with the banner adapter and layout manager
        bannerAdapter = new BannerAdapter(this, itemList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bannerRecyclerView.setLayoutManager(layoutManager);
        bannerRecyclerView.setAdapter(bannerAdapter);

        // Attach a PagerSnapHelper to the RecyclerView for snap scrolling
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(bannerRecyclerView);
    }

    // Method to retrieve the user's profile image from Firestore
    private void retrieveProfileImage() {
        if (user != null) {
            DocumentReference userRef = fStore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("profileImageUrl");
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.baseline_account_circle_24) // Placeholder image while loading
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

    // Method to retrieve and set the user's username in the welcome message
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
