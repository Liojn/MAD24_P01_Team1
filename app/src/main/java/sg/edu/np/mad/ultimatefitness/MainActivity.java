package sg.edu.np.mad.ultimatefitness;

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

import sg.edu.np.mad.ultimatefitness.HomePage.BannerAdapter;
import sg.edu.np.mad.ultimatefitness.HomePage.BannerItem;
import sg.edu.np.mad.ultimatefitness.calendarPage.CalendarActivity;
import sg.edu.np.mad.ultimatefitness.chatbot.activity.ChatbotActivity;
import sg.edu.np.mad.ultimatefitness.foodtracker.*;
import sg.edu.np.mad.ultimatefitness.loginSignup.LoginOrSignUpOption;
import sg.edu.np.mad.ultimatefitness.loginSignup.ProfilePageActivity;
import sg.edu.np.mad.ultimatefitness.training.TrainingMenuActivity;


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
            // User is not logged in, navigate to LoginOrSignUpOption
            Intent intent = new Intent(MainActivity.this, LoginOrSignUpOption.class);
            startActivity(intent);
            finish();
            return; // Early return to prevent further execution
        }

        // Retrieve and set the username
        retrieveAndSetUsername();

        changeProfilePic = findViewById(R.id.profileButton);

        // Retrieve the profile image URI from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        String profileImageUri = prefs.getString("profileImageUri", null);
        if (profileImageUri != null) {
            // Set profile picture using URI
            changeProfilePic.setImageURI(Uri.parse(profileImageUri));
        } else {
            // If no profile image URI is found in SharedPreferences, retrieve it from Firestore
            retrieveProfileImage();
        }

        // Handle the result from EditProfilePageActivity
        if (getIntent().hasExtra("profileImageUri")) {
            // Get the updated profile picture URI from the result intent
            String updatedProfileImageUri = getIntent().getStringExtra("profileImageUri");
            // Set the profile picture using the updated URI
            changeProfilePic.setImageURI(Uri.parse(updatedProfileImageUri));
        }

        // Set OnClickListener for the profile icon to navigate to ProfilePageActivity
        ImageView profileIcon = findViewById(R.id.profileButton);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfilePageActivity.class);
                startActivity(intent);
            }
        });

        //For Onclick for Training Schedule
        findViewById(R.id.nav_fitness).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainingMenuActivity.class);
            startActivity(intent);
        });
        //For Onclick for Chatbot
        findViewById(R.id.chat_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });
        //For Onclick for Calendar
        findViewById(R.id.nav_calendar).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
        //For Onclick for FoodTracker
        findViewById(R.id.nav_food).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FoodTracker.class);
            startActivity(intent);
        });

        //For Onclick for HomePage
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        //


        // Recycler View inside Banner for homepage
        bannerRecyclerView = findViewById(R.id.bannerRecyclerView);
        //List of banner images + title + subtitles
        List<BannerItem> itemList = Arrays.asList(
                new BannerItem(R.drawable.discover_excersies, "Discover Exercises", "Click Here!", TrainingMenuActivity.class),
                new BannerItem(R.drawable.benefits_excerise, null, null   , null)
        );

        bannerAdapter = new BannerAdapter(this, itemList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        bannerRecyclerView.setLayoutManager(layoutManager);
        bannerRecyclerView.setAdapter(bannerAdapter);

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
                            .into(profileBtn);
                } else {
                    // Handle case where user document does not exist
                    Toast.makeText(MainActivity.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                // Handle any errors that occur while fetching user document
                Toast.makeText(MainActivity.this, "Failed to retrieve profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void retrieveAndSetUsername() {
        if (user != null) {
            DocumentReference userRef = fStore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Extract the username from the user document
                    String username = documentSnapshot.getString("uName");
                    if (username != null) {
                        // Update the welcome text with the retrieved username
                        String welcomeText = "Welcome back, " + username + "!\nWhat would you like to do today?";
                        welcomeTextView.setText(welcomeText);
                    }
                } else {
                    // Handle case where user document does not exist
                    Toast.makeText(MainActivity.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                // Handle any errors that occur while fetching user document
                Toast.makeText(MainActivity.this, "Failed to retrieve username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}



